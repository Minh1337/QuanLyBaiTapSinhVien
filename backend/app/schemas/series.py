from datetime import datetime
from typing import List, Optional
from pydantic import BaseModel, ConfigDict
from app.schemas.user import AuthorSummary


class SeriesPostItemResponse(BaseModel):
    id: int
    series_id: int
    post_id: int
    order_index: int
    post_title: Optional[str] = None
    post_slug: Optional[str] = None
    post_read_time: Optional[str] = "5 phút đọc"
    post_views: Optional[int] = 0

    model_config = ConfigDict(from_attributes=True)


class SeriesCreate(BaseModel):
    title: str
    slug: Optional[str] = None
    description: Optional[str] = None
    cover_image: Optional[str] = None
    post_ids: List[int] = []


class SeriesUpdate(BaseModel):
    title: Optional[str] = None
    slug: Optional[str] = None
    description: Optional[str] = None
    cover_image: Optional[str] = None
    post_ids: Optional[List[int]] = None


class SeriesResponse(BaseModel):
    id: int
    title: str
    slug: str
    description: Optional[str] = None
    cover_image: Optional[str] = None
    author: AuthorSummary
    posts_count: int = 0
    items: List[SeriesPostItemResponse] = []
    created_at: datetime
    updated_at: datetime

    model_config = ConfigDict(from_attributes=True)
