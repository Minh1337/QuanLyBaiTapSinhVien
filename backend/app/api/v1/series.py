from typing import List, Optional
from datetime import datetime, timezone
from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlalchemy.orm import Session
from slugify import slugify

from app.core.database import get_db
from app.models.series import Series, SeriesItem
from app.models.post import Post, PostStatus
from app.models.user import User
from app.schemas.series import SeriesCreate, SeriesUpdate, SeriesResponse, SeriesPostItemResponse
from app.schemas.user import AuthorSummary
from app.api.deps import get_current_active_user, get_current_user_optional

router = APIRouter(prefix="/series", tags=["Series"])


def format_series_response(series: Series) -> SeriesResponse:
    items_resp = []
    for it in series.items:
        items_resp.append(SeriesPostItemResponse(
            id=it.id,
            series_id=it.series_id,
            post_id=it.post_id,
            order_index=it.order_index,
            post_title=it.post.title if it.post else "Bài viết không xác định",
            post_slug=it.post.slug if it.post else "",
            post_read_time=it.post.read_time if it.post else "5 phút đọc",
            post_views=it.post.views if it.post else 0
        ))
    
    return SeriesResponse(
        id=series.id,
        title=series.title,
        slug=series.slug,
        description=series.description,
        cover_image=series.cover_image,
        author=AuthorSummary.model_validate(series.author),
        posts_count=len(series.items),
        items=items_resp,
        created_at=series.created_at,
        updated_at=series.updated_at
    )


@router.get("", response_model=List[SeriesResponse])
def get_series_list(
    author_id: Optional[int] = Query(None),
    search: Optional[str] = Query(None),
    limit: int = Query(50, ge=1, le=100),
    db: Session = Depends(get_db)
):
    """
    Get all article series with post counts and author details.
    """
    query = db.query(Series)
    if author_id:
        query = query.filter(Series.author_id == author_id)
    if search:
        s = f"%{search.strip().lower()}%"
        query = query.filter(Series.title.ilike(s) | Series.description.ilike(s))
    
    series_list = query.order_by(Series.created_at.desc()).limit(limit).all()
    return [format_series_response(s) for s in series_list]


@router.get("/{slug_or_id}", response_model=SeriesResponse)
def get_series_detail(slug_or_id: str, db: Session = Depends(get_db)):
    """
    Get detailed series with full ordered post items.
    """
    series = None
    if slug_or_id.isdigit():
        series = db.query(Series).filter(Series.id == int(slug_or_id)).first()
    if not series:
        series = db.query(Series).filter(Series.slug == slug_or_id).first()
    
    if not series:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Không tìm thấy chuỗi bài viết này."
        )
    return format_series_response(series)


@router.get("/by-post/{post_id}")
def get_series_by_post(post_id: int, db: Session = Depends(get_db)):
    """
    Find which series this post belongs to, with prev/next navigation.
    """
    item = db.query(SeriesItem).filter(SeriesItem.post_id == post_id).first()
    if not item:
        return {"has_series": False, "series": None}
    
    series = db.query(Series).filter(Series.id == item.series_id).first()
    if not series:
        return {"has_series": False, "series": None}
    
    # Sort items by order_index
    sorted_items = sorted(series.items, key=lambda x: x.order_index)
    curr_idx = -1
    for idx, it in enumerate(sorted_items):
        if it.post_id == post_id:
            curr_idx = idx
            break
    
    prev_post = None
    next_post = None
    if curr_idx > 0:
        p = sorted_items[curr_idx - 1].post
        prev_post = {"id": p.id, "title": p.title, "slug": p.slug} if p else None
    if curr_idx != -1 and curr_idx < len(sorted_items) - 1:
        n = sorted_items[curr_idx + 1].post
        next_post = {"id": n.id, "title": n.title, "slug": n.slug} if n else None

    return {
        "has_series": True,
        "series": {
            "id": series.id,
            "title": series.title,
            "slug": series.slug,
            "current_index": curr_idx + 1,
            "total_posts": len(sorted_items),
            "prev_post": prev_post,
            "next_post": next_post,
            "all_posts": [
                {
                    "id": it.post.id,
                    "title": it.post.title,
                    "slug": it.post.slug,
                    "order_index": it.order_index,
                    "is_current": it.post_id == post_id
                }
                for it in sorted_items if it.post
            ]
        }
    }


@router.post("", response_model=SeriesResponse, status_code=status.HTTP_201_CREATED)
def create_series(
    data: SeriesCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_active_user)
):
    """
    Create a new article series. Users can group multiple published posts into a playlist/series.
    """
    clean_title = data.title.strip()
    if not clean_title:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Tiêu đề chuỗi bài viết không được để trống."
        )
    
    base_slug = slugify(data.slug.strip() if data.slug else clean_title)
    if not base_slug:
        base_slug = f"series-{int(datetime.now().timestamp())}"
    
    unique_slug = base_slug
    idx = 1
    while db.query(Series).filter(Series.slug == unique_slug).first():
        unique_slug = f"{base_slug}-{idx}"
        idx += 1
    
    new_series = Series(
        title=clean_title,
        slug=unique_slug,
        description=data.description.strip() if data.description else None,
        cover_image=data.cover_image,
        author_id=current_user.id
    )
    db.add(new_series)
    db.flush()

    # Add posts to series
    for order, p_id in enumerate(data.post_ids):
        post = db.query(Post).filter(Post.id == p_id).first()
        if post:
            item = SeriesItem(series_id=new_series.id, post_id=post.id, order_index=order + 1)
            db.add(item)
    
    db.commit()
    db.refresh(new_series)
    return format_series_response(new_series)


@router.put("/{series_id}", response_model=SeriesResponse)
def update_series(
    series_id: int,
    data: SeriesUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_active_user)
):
    """
    Update a series and reorder or add/remove posts.
    """
    series = db.query(Series).filter(Series.id == series_id).first()
    if not series:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Không tìm thấy chuỗi bài viết."
        )
    
    # Check permissions
    is_admin = current_user.is_superuser or any(r.name in ["admin", "moderator"] for r in current_user.roles)
    if series.author_id != current_user.id and not is_admin:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Bạn không có quyền chỉnh sửa chuỗi bài viết của người khác."
        )
    
    if data.title is not None and data.title.strip():
        series.title = data.title.strip()
    if data.description is not None:
        series.description = data.description.strip()
    if data.cover_image is not None:
        series.cover_image = data.cover_image
    
    if data.post_ids is not None:
        # Re-sync series items
        db.query(SeriesItem).filter(SeriesItem.series_id == series.id).delete()
        for order, p_id in enumerate(data.post_ids):
            post = db.query(Post).filter(Post.id == p_id).first()
            if post:
                item = SeriesItem(series_id=series.id, post_id=post.id, order_index=order + 1)
                db.add(item)
                
    db.commit()
    db.refresh(series)
    return format_series_response(series)


@router.delete("/{series_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_series(
    series_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_active_user)
):
    """
    Delete a series (original posts remain intact).
    """
    series = db.query(Series).filter(Series.id == series_id).first()
    if not series:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Không tìm thấy chuỗi bài viết."
        )
    
    is_admin = current_user.is_superuser or any(r.name in ["admin", "moderator"] for r in current_user.roles)
    if series.author_id != current_user.id and not is_admin:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Bạn không có quyền xóa chuỗi bài viết của người khác."
        )
    
    db.delete(series)
    db.commit()
    return None
