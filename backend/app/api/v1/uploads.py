import os
import uuid
from typing import Set
from fastapi import APIRouter, UploadFile, File, HTTPException, status, Depends
from app.models.user import User
from app.api.deps import get_current_active_user

router = APIRouter(prefix="/uploads", tags=["File Uploads & Assets"])

ALLOWED_EXTENSIONS: Set[str] = {".jpg", ".jpeg", ".png", ".webp", ".gif", ".svg"}
ALLOWED_MIME_TYPES: Set[str] = {
    "image/jpeg",
    "image/png",
    "image/webp",
    "image/gif",
    "image/svg+xml"
}
MAX_FILE_SIZE = 5 * 1024 * 1024  # 5 MB

UPLOAD_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "..", "..", "uploads"))
os.makedirs(UPLOAD_DIR, exist_ok=True)


@router.post("/image")
async def upload_image(
    file: UploadFile = File(...),
    current_user: User = Depends(get_current_active_user)
):
    """
    Section 16: Secure file upload with size limit, extension whitelist, and MIME verification.
    """
    if not file.filename:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Tên tệp tin không hợp lệ."
        )

    _, ext = os.path.splitext(file.filename.lower())
    if ext not in ALLOWED_EXTENSIONS:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Định dạng tệp không được hỗ trợ ({ext}). Chỉ chấp nhận: {', '.join(sorted(ALLOWED_EXTENSIONS))}."
        )

    if not file.content_type or file.content_type.lower() not in ALLOWED_MIME_TYPES:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="MIME type không hợp lệ. Vui lòng tải lên tệp ảnh chuẩn."
        )

    # Read and check size
    content = await file.read()
    if len(content) == 0:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Tệp tin tải lên rỗng (0 bytes)."
        )

    if len(content) > MAX_FILE_SIZE:
        raise HTTPException(
            status_code=getattr(status, "HTTP_413_CONTENT_TOO_LARGE", 413),
            detail=f"Kích thước tệp vượt quá giới hạn cho phép (Tối đa 5MB, hiện tại {len(content) / (1024 * 1024):.1f}MB)."
        )

    # Generate safe random filename
    unique_filename = f"{uuid.uuid4().hex}{ext}"
    file_path = os.path.join(UPLOAD_DIR, unique_filename)

    with open(file_path, "wb") as f:
        f.write(content)

    return {
        "url": f"/uploads/{unique_filename}",
        "filename": unique_filename,
        "original_name": file.filename,
        "size_bytes": len(content),
        "content_type": file.content_type
    }


def parse_docx_bytes(data: bytes) -> dict:
    import io
    import zipfile
    import xml.etree.ElementTree as ET

    try:
        with zipfile.ZipFile(io.BytesIO(data)) as z:
            xml_data = z.read("word/document.xml")
    except Exception as e:
        raise ValueError(f"Không thể đọc tệp Word (.docx): {e}")

    root = ET.fromstring(xml_data)
    ns = {"w": "http://schemas.openxmlformats.org/wordprocessingml/2006/main"}

    lines = []
    title = ""

    for p in root.iter(f"{{{ns['w']}}}p"):
        pStyle = p.find(".//w:pStyle", ns)
        val = pStyle.get(f"{{{ns['w']}}}val") if pStyle is not None else ""

        parts = []
        for r in p.findall(".//w:r", ns):
            b = r.find(".//w:b", ns) is not None
            i = r.find(".//w:i", ns) is not None
            t = r.find(".//w:t", ns)
            if t is not None and t.text:
                txt = t.text
                if b and i:
                    txt = f"***{txt}***"
                elif b:
                    txt = f"**{txt}**"
                elif i:
                    txt = f"*{txt}*"
                parts.append(txt)

        p_text = "".join(parts).strip()
        if not p_text:
            continue

        is_heading_1 = val in ("Heading1", "Heading 1", "Title")
        is_heading_2 = val in ("Heading2", "Heading 2")
        is_heading_3 = val in ("Heading3", "Heading 3")
        is_bullet = val in ("ListParagraph", "List Bullet", "ListBullet")

        if is_heading_1:
            if not title:
                title = p_text.replace("*", "").strip()
            else:
                lines.append(f"# {p_text}")
        elif is_heading_2:
            lines.append(f"## {p_text}")
        elif is_heading_3:
            lines.append(f"### {p_text}")
        elif is_bullet:
            lines.append(f"- {p_text}")
        else:
            if not title and len(p_text) < 120 and not lines:
                title = p_text.replace("*", "").strip()
            else:
                lines.append(p_text)

    content = "\n\n".join(lines)
    return {"title": title, "content": content}


@router.post("/document")
async def import_document_file(
    file: UploadFile = File(...),
    current_user: User = Depends(get_current_active_user)
):
    """
    Import .docx (Word), .md (Markdown), .txt, or .html documents and convert directly into Markdown content and title.
    """
    if not file.filename:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Tên tệp không hợp lệ."
        )

    filename_lower = file.filename.lower()
    content_bytes = await file.read()

    if len(content_bytes) == 0:
        raise HTTPException(status_code=400, detail="Tệp tin tải lên rỗng.")

    if len(content_bytes) > 20 * 1024 * 1024:
        raise HTTPException(status_code=413, detail="Tệp tin vượt quá 20MB.")

    extracted_title = ""
    extracted_markdown = ""

    if filename_lower.endswith(".docx"):
        try:
            res = parse_docx_bytes(content_bytes)
            extracted_title = res.get("title", "")
            extracted_markdown = res.get("content", "")
        except Exception as e:
            raise HTTPException(status_code=400, detail=f"Lỗi khi đọc file .docx: {str(e)}")

    elif filename_lower.endswith((".md", ".markdown", ".txt")):
        try:
            raw_text = content_bytes.decode("utf-8")
        except UnicodeDecodeError:
            raw_text = content_bytes.decode("latin-1", errors="ignore")

        # Check frontmatter
        import re
        fm_match = re.match(r"^---\r?\n([\s\S]*?)\r?\n---\r?\n([\s\S]*)$", raw_text)
        if fm_match:
            yaml_part = fm_match.group(1)
            body_part = fm_match.group(2)
            title_m = re.search(r'^title:\s*["\']?(.*?)["\']?$', yaml_part, re.M)
            if title_m:
                extracted_title = title_m.group(1).strip()
            extracted_markdown = body_part.strip()
        else:
            first_h1 = re.search(r"^#\s+(.+)$", raw_text, re.M)
            if first_h1:
                extracted_title = first_h1.group(1).strip()
                extracted_markdown = re.sub(r"^#\s+.+$", "", raw_text, count=1, flags=re.M).strip()
            else:
                extracted_markdown = raw_text.strip()

    elif filename_lower.endswith(".html") or filename_lower.endswith(".htm"):
        try:
            from bs4 import BeautifulSoup
            soup = BeautifulSoup(content_bytes, "html.parser")
            h1 = soup.find(["h1", "title"])
            if h1:
                extracted_title = h1.get_text().strip()
                h1.decompose()
            extracted_markdown = soup.get_text("\n\n").strip()
        except Exception:
            extracted_markdown = content_bytes.decode("utf-8", errors="ignore")

    else:
        raise HTTPException(
            status_code=400,
            detail="Định dạng tệp không được hỗ trợ. Vui lòng tải lên tệp .docx (Word), .md (Markdown), hoặc .txt"
        )

    return {
        "success": True,
        "filename": file.filename,
        "title": extracted_title or file.filename.rsplit(".", 1)[0].replace("-", " ").replace("_", " ").title(),
        "content": extracted_markdown
    }

