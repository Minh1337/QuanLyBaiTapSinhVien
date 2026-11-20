import os
import json
import logging
import random
from typing import List, Optional, Dict, Any
from fastapi import APIRouter, Depends, HTTPException, status, Query
from sqlalchemy.orm import Session
from sqlalchemy import func, or_

from app.core.database import get_db
from app.models.user import User
from app.models.post import Post
from app.models.course import Course
from app.models.quiz import QuizQuestion, QuizQuestionPost
from app.schemas.quiz import (
    QuizQuestionCreate,
    QuizQuestionResponse,
    QuizGenerateRequest,
    QuizSubmitRequest,
    QuizSubmitResponse,
    QuizSubmitDetail
)
from app.services.gemini_service import gemini_service
from app.api.deps import get_current_user_optional, get_current_active_user

logger = logging.getLogger(__name__)

router = APIRouter(tags=["Quiz & Knowledge Testing"])


def build_fallback_questions(
    material: str,
    count: int,
    language: str = "IT",
    difficulty: str = "medium"
) -> List[Dict[str, Any]]:
    """Generate high-quality default fallback questions when AI service is unavailable."""
    sample_bank = [
        {
            "question": "Trong ngôn ngữ lập trình C++, tính năng cốt lõi nào giúp tổ chức mã nguồn theo các đối tượng, hỗ trợ tính đóng gói (encapsulation), kế thừa (inheritance) và đa hình (polymorphism)?",
            "options": [
                "Lập trình hướng đối tượng (OOP - Object-Oriented Programming)",
                "Lập trình hướng sự kiện thuần túy",
                "Lập trình hàm không trạng thái (Pure Functional)",
                "Lập trình kịch bản thông dịch (Scripting)"
            ],
            "answer_index": 0,
            "explanation": "C++ là ngôn ngữ mở rộng từ C với tính năng hướng đối tượng mạnh mẽ thông qua class, struct, virtual functions và access specifiers.",
            "difficulty": "easy",
            "lang": "C++"
        },
        {
            "question": "Những môi trường phát triển (IDE) hoặc trình biên tập mã nguồn nào sau đây thường được sử dụng phổ biến nhất để bắt đầu học và biên dịch chương trình C++?",
            "options": [
                "Dev-C++, CodeBlocks, VS Code (kèm MinGW/GCC) hoặc Visual Studio",
                "Microsoft Word hoặc Google Docs",
                "Adobe Photoshop và Illustrator",
                "Figma và Canva"
            ],
            "answer_index": 0,
            "explanation": "Dev-C++, CodeBlocks, CLion và VS Code kết hợp với trình biên dịch như GCC/G++ là những công cụ tiêu chuẩn hàng đầu cho người học và lập trình viên C++.",
            "difficulty": "easy",
            "lang": "C++"
        },
        {
            "question": "Để bắt đầu học lập trình C++, người học cần nắm vững những khối kiến thức nền tảng cơ bản nào trước khi đi sâu vào cấu trúc dữ liệu nâng cao?",
            "options": [
                "Biến, kiểu dữ liệu, hàm, vòng lặp (for/while) và cấu trúc điều kiện (if/else)",
                "Chỉ cần học cách cài đặt hệ điều hành Linux",
                "Lập trình mạng Socket và điện toán đám mây Kubernetes",
                "Thiết kế giao diện Photoshop và dựng video"
            ],
            "answer_index": 0,
            "explanation": "Nắm vững biến, kiểu dữ liệu nguyên thủy, hàm, vòng lặp và câu lệnh rẽ nhánh là bước khởi đầu cốt lõi bắt buộc khi học lập trình C++.",
            "difficulty": "easy",
            "lang": "C++"
        },
        {
            "question": "Đặc điểm nổi bật nào giúp C++ trở thành ngôn ngữ hàng đầu trong phát triển phần mềm hệ thống, game engine (Unreal Engine), thuật toán và đồ họa hiệu năng cao?",
            "options": [
                "Tốc độ thực thi cực nhanh gần sát phần cứng, quản lý bộ nhớ trực tiếp và khả năng tối ưu hóa sâu",
                "Tự động tải xuống mã nguồn từ Internet",
                "Không cần khai báo kiểu dữ liệu cho biến",
                "Chỉ chạy được trên máy tính cấu hình yếu"
            ],
            "answer_index": 0,
            "explanation": "C++ được biên dịch trực tiếp sang mã máy nhị phân mà không cần thông dịch qua máy ảo, mang lại hiệu năng tối đa và quyền kiểm soát bộ nhớ chi tiết.",
            "difficulty": "easy",
            "lang": "C++"
        },
        {
            "question": "Trong C++, đối tượng nào thuộc thư viện `<iostream>` được sử dụng chuẩn mực để in dữ liệu ra màn hình dòng lệnh (Console Output)?",
            "options": [
                "std::cout (kết hợp toán tử chèn luồng <<)",
                "std::cin (dùng để nhập dữ liệu)",
                "printf_screen()",
                "console.log()"
            ],
            "answer_index": 0,
            "explanation": "std::cout trong thư viện <iostream> là đối tượng luồng đầu ra tiêu chuẩn (standard character output) của C++.",
            "difficulty": "easy",
            "lang": "C++"
        },
        {
            "question": "Trong giao thức HTTP, mã trạng thái (HTTP Status Code) nào biểu thị yêu cầu đã được máy chủ xử lý thành công và tạo mới tài nguyên (Resource Created)?",
            "options": ["201 Created", "200 OK", "400 Bad Request", "404 Not Found"],
            "answer_index": 0,
            "explanation": "Mã 201 Created thường được trả về sau các thao tác POST thành công để thông báo tài nguyên mới đã được tạo và lưu trữ trên máy chủ.",
            "difficulty": "easy",
            "lang": "HTTP"
        },
        {
            "question": "Trong các phương thức HTTP chuẩn của kiến trúc RESTful API, phương thức nào mang tính chất an toàn (Safe) và lũy kế (Idempotent) dùng để lấy dữ liệu mà không làm thay đổi trạng thái máy chủ?",
            "options": ["GET", "POST", "PATCH", "CONNECT"],
            "answer_index": 0,
            "explanation": "Phương thức GET là safe và idempotent: gọi GET nhiều lần với cùng tham số sẽ trả về cùng kết quả và không gây tác dụng phụ lên dữ liệu máy chủ.",
            "difficulty": "easy",
            "lang": "HTTP"
        },
        {
            "question": "Trong React 19, cú pháp để sử dụng Form Actions và xử lý trạng thái submit bất đồng bộ là gì?",
            "options": ["Sử dụng hook useActionState()", "Sử dụng hook useEffect() kết hợp useState()", "Bắt buộc dùng Redux Thunk", "Dùng useCallback() với ref"],
            "answer_index": 0,
            "explanation": "React 19 giới thiệu hook useActionState để tự động quản lý trạng thái loading, lỗi và kết quả trả về của Form Actions.",
            "difficulty": "medium",
            "lang": "React"
        },
        {
            "question": "FastAPI sử dụng thư viện nào làm nền tảng kiểm tra kiểu dữ liệu (data validation) và serialization?",
            "options": ["Marshmallow", "Pydantic", "Django REST Serializer", "Cerberus"],
            "answer_index": 1,
            "explanation": "FastAPI được xây dựng dựa trên Pydantic để thực hiện validation dữ liệu tự động với hiệu năng cao bằng Rust (Pydantic V2).",
            "difficulty": "easy",
            "lang": "FastAPI"
        },
        {
            "question": "Mục tiêu cốt lõi của việc thiết lập Connection Pooling trong PostgreSQL / SQLAlchemy 2.0 là gì?",
            "options": ["Mã hóa dữ liệu tại chỗ", "Tái sử dụng các kết nối cơ sở dữ liệu đã mở, giảm overhead khởi tạo kết nối TCP/TLS", "Tự động phân vùng bảng lớn", "Tăng kích thước RAM của máy chủ"],
            "answer_index": 1,
            "explanation": "Khởi tạo kết nối PostgreSQL tốn kém tài nguyên. Connection Pooling duy trì một nhóm kết nối sẵn sàng để tái sử dụng ngay lập tức.",
            "difficulty": "medium",
            "lang": "PostgreSQL"
        },
        {
            "question": "Trong quy trình CI/CD với Docker, Multi-stage Build mang lại lợi ích quan trọng nào?",
            "options": ["Cho phép chạy nhiều container cùng một port", "Giảm đáng kể kích thước image thành phẩm bằng cách loại bỏ compiler và dev dependencies", "Tự động backup dữ liệu container", "Thay thế hoàn toàn Kubernetes"],
            "answer_index": 1,
            "explanation": "Multi-stage build tách biệt giai đoạn build và runtime, chỉ copy artifact cần thiết vào image cuối, giúp tối ưu dung lượng và bảo mật.",
            "difficulty": "easy",
            "lang": "DevOps"
        },
        {
            "question": "Cơ chế nào trong Python 3.11+ giúp tăng tốc độ thực thi mã nguồn lên tới 10-60% so với Python 3.10?",
            "options": ["Loại bỏ hoàn toàn GIL (Global Interpreter Lock)", "Dự án Faster CPython (Adaptive specializing interpreter)", "Chuyển sang JIT compiler của PyPy", "Bắt buộc biên dịch AOT"],
            "answer_index": 1,
            "explanation": "Python 3.11 triển khai dự án Faster CPython với Adaptive Specializing Interpreter giúp tối ưu các bytecode thường xuyên được gọi.",
            "difficulty": "hard",
            "lang": "Python"
        },
        {
            "question": "Trong hệ thống quản lý phiên bản phân tán Git, lệnh nào được sử dụng để tải toàn bộ lịch sử và mã nguồn của một kho chứa từ xa (Remote Repository) về máy tính cá nhân?",
            "options": ["git clone <repository_url>", "git pull --all", "git fork <url>", "git copy <url>"],
            "answer_index": 0,
            "explanation": "Lệnh 'git clone' sao chép toàn bộ kho mã nguồn từ xa bao gồm mọi nhánh (branches) và lịch sử commit về máy tính local.",
            "difficulty": "easy",
            "lang": "Git"
        },
        {
            "question": "Trong Git, thao tác nào được sử dụng để tạm thời lưu trữ (cất đi) các thay đổi đang làm dở trong thư mục làm việc mà chưa muốn tạo commit?",
            "options": ["git stash (kèm git stash pop để lấy lại)", "git reset --hard", "git clean -f", "git checkout -b"],
            "answer_index": 0,
            "explanation": "Lệnh 'git stash' lưu tạm thời các thay đổi vào ngăn xếp stash để bạn có thể chuyển nhánh sạch sẽ mà không làm mất code.",
            "difficulty": "easy",
            "lang": "Git"
        },
        {
            "question": "Sự khác biệt cơ bản nhất giữa `git merge` và `git rebase` khi hợp nhất hai nhánh là gì?",
            "options": [
                "`git merge` tạo ra một commit hợp nhất (Merge Commit) giữ nguyên lịch sử thực tế, trong khi `git rebase` tua lại và xếp các commit thành một đường thẳng tuyến tính sạch sẽ",
                "`git rebase` chỉ dùng cho file hình ảnh",
                "`git merge` tự động xóa nhánh gốc",
                "Không có sự khác biệt giữa hai lệnh này"
            ],
            "answer_index": 0,
            "explanation": "Merge bảo toàn lịch sử theo nhánh rẽ với merge commit; Rebase viết lại lịch sử commit để nhánh trông như được phát triển tuần tự thẳng hàng.",
            "difficulty": "medium",
            "lang": "Git"
        },
        {
            "question": "Một Commit trong Git về bản chất kỹ thuật lưu trữ thông tin gì?",
            "options": [
                "Một ảnh chụp nhanh (Snapshot) toàn bộ trạng thái của cây thư mục dự án tại thời điểm đó cùng metadata (tác giả, ngày giờ, commit cha)",
                "Chỉ lưu một dòng chữ tin nhắn văn bản ngắn",
                "Một tệp nén zip chứa toàn bộ hệ điều hành",
                "Một file PDF báo cáo tiến độ công việc"
            ],
            "answer_index": 0,
            "explanation": "Git không lưu theo kiểu chênh lệch diff delta giữa các file, mà lưu dưới dạng snapshot (commit object) trỏ tới cây thư mục (tree object).",
            "difficulty": "medium",
            "lang": "Git"
        },
        {
            "question": "Khi đã commit nhầm hoặc muốn quay ngược lại commit trước đó trên nhánh đang làm việc một cách an toàn mà vẫn giữ lại code đã sửa đổi trong thư mục làm việc, lệnh nào tối ưu nhất?",
            "options": ["git reset --soft HEAD~1", "git reset --hard HEAD~1", "git branch -D main", "git push --force"],
            "answer_index": 0,
            "explanation": "'git reset --soft HEAD~1' đưa HEAD về commit trước đó nhưng vẫn giữ nguyên tất cả thay đổi trong Staging Area để bạn có thể chỉnh sửa lại và commit tiếp.",
            "difficulty": "medium",
            "lang": "Git"
        }
    ]

    # Detect matching domain from language and material content
    search_text = f"{language} {material}".lower()
    target_lang = None
    if "git" in search_text or "version control" in search_text or "quản lý phiên bản" in search_text:
        target_lang = "Git"
    elif "c++" in search_text or "cpp" in search_text or "lập trình c" in search_text:
        target_lang = "C++"
    elif "http" in search_text or "rest api" in search_text:
        target_lang = "HTTP"
    elif "docker" in search_text or "devops" in search_text or "kubernetes" in search_text:
        target_lang = "DevOps"
    elif "postgres" in search_text or "database" in search_text or "sql" in search_text:
        target_lang = "PostgreSQL"
    elif "fastapi" in search_text:
        target_lang = "FastAPI"
    elif "python" in search_text:
        target_lang = "Python"
    elif "react" in search_text:
        target_lang = "React"

    matched = []
    if target_lang:
        matched = [q for q in sample_bank if q.get("lang") == target_lang]
    if not matched:
        clean_lang = language.strip().lower()
        matched = [q for q in sample_bank if clean_lang in q.get("lang", "").lower()]
    if not matched:
        matched = sample_bank

    results = []
    for i in range(count):
        item = matched[i % len(matched)].copy()
        if difficulty in ["easy", "medium", "hard"]:
            item["difficulty"] = difficulty
        results.append(item)

    return results



_TOPICS_FILE = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))), "data", "quiz_topics.json")

DEFAULT_TOPICS_METADATA = [
    {
        "id": "all",
        "title": "Tất cả Đề tài",
        "icon": "🌐",
        "badge": "Toàn diện",
        "description": "Luyện tập tổng hợp câu hỏi từ tất cả các chuyên đề kỹ thuật."
    },
    {
        "id": "React",
        "title": "React 19 & Frontend",
        "icon": "⚛️",
        "badge": "Frontend",
        "description": "Hooks, Server Actions, useActionState, Suspense, Fiber và React Compiler."
    },
    {
        "id": "FastAPI",
        "title": "FastAPI & Backend",
        "icon": "⚡",
        "badge": "Backend",
        "description": "Async def, Pydantic V2, Dependency Injection, Lifespan và CORS."
    },
    {
        "id": "PostgreSQL",
        "title": "PostgreSQL & Database",
        "icon": "🐘",
        "badge": "Database",
        "description": "Connection Pooling, GIN Index, EXPLAIN ANALYZE, MVCC và Partitioning."
    },
    {
        "id": "DevOps",
        "title": "DevOps, Docker & K8s",
        "icon": "🐳",
        "badge": "DevOps",
        "description": "Multi-stage builds, Kubernetes Pods/Probes, CI/CD và Reverse Proxy."
    },
    {
        "id": "Python",
        "title": "Python Core & Async",
        "icon": "🐍",
        "badge": "Language",
        "description": "Asyncio TaskGroup, Faster CPython 3.11+, Generators, GIL và Metaclasses."
    },
    {
        "id": "Security",
        "title": "Bảo mật Web & Ứng dụng",
        "icon": "🛡️",
        "badge": "Security",
        "description": "OWASP Top 10, SQL Injection, XSS, CSRF, JWT và Password Hashing."
    },
    {
        "id": "Architecture",
        "title": "Kiến trúc Hệ thống",
        "icon": "🏗️",
        "badge": "System Design",
        "description": "Microservices, Saga Pattern, Caching Redis, Kafka vs RabbitMQ và CAP."
    },
    {
        "id": "Fullstack",
        "title": "Fullstack Engineering",
        "icon": "💻",
        "badge": "Software Eng",
        "description": "gRPC vs REST, HTTP/2 & 3, SSE, SOLID, Clean Architecture và SemVer."
    },
    {
        "id": "C++",
        "title": "Lập trình C / C++",
        "icon": "⚙️",
        "badge": "Systems & OOP",
        "description": "Lập trình hướng đối tượng OOP, quản lý bộ nhớ, con trỏ, STL và tối ưu hiệu năng."
    },
    {
        "id": "HTTP",
        "title": "Giao thức HTTP & REST API",
        "icon": "🌐",
        "badge": "Web Protocols",
        "description": "HTTP Methods, Status Codes, HTTP/2 & 3 Multiplexing, Headers và chuẩn RESTful API."
    },
    {
        "id": "Git",
        "title": "Git & Quản lý Phiên bản",
        "icon": "🐙",
        "badge": "VCS & Git Flow",
        "description": "Quản lý mã nguồn phân tán, Commit snapshot, Merge vs Rebase, Stash và Reset."
    }
]

TOPIC_ALIASES = {
    "all": [],
    "React": ["react", "react 19", "frontend", "tailwind css", "tailwind", "nextjs", "next.js", "ui"],
    "FastAPI": ["fastapi", "backend", "pydantic", "fastapi & backend", "fastapi / python"],
    "PostgreSQL": ["postgresql", "postgres", "database", "sql", "csdl"],
    "DevOps": ["devops", "docker", "ci/cd", "kubernetes", "k8s", "docker / devops", "linux"],
    "Python": ["python", "asyncio", "python core", "cpython"],
    "Security": ["security", "web security", "owasp", "bảo mật", "bảo mật web", "jwt"],
    "Architecture": ["architecture", "microservices", "system design", "kiến trúc", "hệ thống", "kiến trúc hệ thống"],
    "Fullstack": ["fullstack", "full stack", "html5", "css3", "javascript", "html/web", "web", "clean architecture"],
    "C++": ["c++", "cpp", "c/c++", "c và c++", "lập trình c++", "lập trình c", "clang", "g++"],
    "HTTP": ["http", "https", "giao thức http", "làm chủ http", "rest api", "restful", "api"],
    "Git": ["git", "github", "gitlab", "vcs", "version control", "quản lý phiên bản", "commit", "stash"]
}


def get_topics_metadata() -> List[Dict[str, Any]]:
    if os.path.exists(_TOPICS_FILE):
        try:
            with open(_TOPICS_FILE, "r", encoding="utf-8") as f:
                data = json.load(f)
                if isinstance(data, list) and len(data) > 0:
                    return data
        except Exception as e:
            logger.warning(f"Error loading quiz_topics.json: {e}")
    return DEFAULT_TOPICS_METADATA


@router.get("/topics", response_model=List[Dict[str, Any]])
def get_quiz_topics(db: Session = Depends(get_db)):
    """
    Lấy danh sách các chuyên đề thi trắc nghiệm kèm số lượng câu hỏi thực tế trong CSDL.
    """
    counts = dict(
        db.query(QuizQuestion.language, func.count(QuizQuestion.id))
        .group_by(QuizQuestion.language)
        .all()
    )
    total_all = sum(counts.values())

    topics = []
    for item in get_topics_metadata():
        t_id = item["id"]
        if t_id == "all":
            cnt = total_all
        else:
            aliases = TOPIC_ALIASES.get(t_id, [t_id.lower()])
            matched_langs = {
                lang for lang in counts.keys()
                if any(alias in (lang or "").lower() for alias in aliases)
            }
            cnt = sum(counts[lang] for lang in matched_langs)
        topics.append({
            **item,
            "question_count": cnt
        })
    return topics


@router.get("/post-quizzes", response_model=List[Dict[str, Any]])
def get_post_quizzes(db: Session = Depends(get_db)):
    """
    Lấy danh sách các bài viết đã có bộ đề trắc nghiệm trong CSDL,
    kèm thông tin bài viết và số lượng câu hỏi để hiển thị trên trang Ngân hàng trắc nghiệm.
    """
    rows = (
        db.query(Post, func.count(QuizQuestionPost.question_id).label("question_count"))
        .join(QuizQuestionPost, QuizQuestionPost.post_id == Post.id)
        .group_by(Post.id)
        .having(func.count(QuizQuestionPost.question_id) > 0)
        .order_by(Post.id.desc())
        .all()
    )
    result = []
    for post, count in rows:
        result.append({
            "post_id": post.id,
            "title": post.title,
            "slug": post.slug,
            "category_name": post.category.name if post.category else "IT",
            "cover_image": post.cover_image,
            "question_count": count,
            "author_name": (post.author.name or post.author.username) if post.author else "IT Blog",
            "created_at": post.created_at.isoformat() if post.created_at else None,
        })
    return result


@router.get("/ai-quizzes", response_model=List[Dict[str, Any]])
def get_ai_quizzes(db: Session = Depends(get_db)):
    """
    Lấy danh sách các bộ đề thi trắc nghiệm được tạo bằng AI trong CSDL.
    """
    rows = (
        db.query(
            QuizQuestion.language,
            func.count(QuizQuestion.id).label("question_count"),
            func.max(QuizQuestion.created_at).label("latest_created_at")
        )
        .filter(QuizQuestion.source == "ai")
        .group_by(QuizQuestion.language)
        .order_by(func.max(QuizQuestion.created_at).desc())
        .all()
    )
    result = []
    for lang, count, latest_at in rows:
        clean_lang = (lang or "").strip()
        if not clean_lang:
            continue
        display_title = f"Đề thi AI: {clean_lang.title() if len(clean_lang) > 3 else clean_lang.upper()}"
        result.append({
            "id": f"ai_{clean_lang.lower().replace(' ', '_')}",
            "language": clean_lang,
            "title": display_title,
            "question_count": count,
            "created_at": latest_at.isoformat() if latest_at else None,
            "badge": "⚡ AI Gemini",
            "icon": "🤖"
        })
    return result


@router.get("/questions", response_model=Dict[str, List[QuizQuestionResponse]])
def get_quiz_questions(
    course_id: Optional[int] = Query(None, description="Lọc theo ID khóa học"),
    post_id: Optional[int] = Query(None, description="Lọc theo ID bài viết"),
    language: Optional[str] = Query(None, description="Lọc theo ngôn ngữ lập trình / công nghệ"),
    difficulty: Optional[str] = Query(None, description="Lọc theo độ khó (easy, medium, hard)"),
    limit: int = Query(50, ge=1, le=500, description="Giới hạn số lượng câu hỏi"),
    randomize: bool = Query(False, description="Xáo trộn thứ tự câu hỏi ngẫu nhiên"),
    db: Session = Depends(get_db)
):
    """
    Lấy danh sách câu hỏi trắc nghiệm từ ngân hàng đề thi.
    Hỗ trợ lọc theo Khóa học, Bài viết liên quan, Ngôn ngữ, hoặc Độ khó, kèm tính năng lấy ngẫu nhiên và chọn số lượng câu hỏi.
    """
    query = db.query(QuizQuestion)

    if post_id is not None:
        query = query.join(QuizQuestionPost, QuizQuestionPost.question_id == QuizQuestion.id)\
                     .filter(QuizQuestionPost.post_id == post_id)
        if isinstance(difficulty, str) and difficulty.strip() and difficulty.lower().strip() != "mixed":
            diff_q = query.filter(QuizQuestion.difficulty == difficulty.lower().strip())
            if diff_q.count() > 0:
                query = diff_q
    elif course_id is not None:
        course_q = query.filter(QuizQuestion.course_id == course_id)
        if isinstance(difficulty, str) and difficulty.strip() and difficulty.lower().strip() != "mixed":
            course_q = course_q.filter(QuizQuestion.difficulty == difficulty.lower().strip())

        if course_q.count() > 0:
            query = course_q
        else:
            # Fallback by course category or title
            target_course = db.query(Course).filter(Course.id == course_id).first()
            if target_course:
                course_topic = (target_course.category.name if target_course.category else target_course.title) or ""
                aliases = TOPIC_ALIASES.get(course_topic, [course_topic.lower()])
                query = query.filter(or_(*[QuizQuestion.language.ilike(f"%{a}%") for a in aliases]))
                if isinstance(difficulty, str) and difficulty.strip() and difficulty.lower().strip() != "mixed":
                    diff_q = query.filter(QuizQuestion.difficulty == difficulty.lower().strip())
                    if diff_q.count() > 0:
                        query = diff_q
            else:
                query = course_q
    else:
        if isinstance(language, str) and language.strip().lower() not in ["all", "tất cả", "tat ca", ""]:
            clean_lang = language.strip()
            aliases = TOPIC_ALIASES.get(clean_lang, [clean_lang.lower()])
            query = query.filter(or_(*[QuizQuestion.language.ilike(f"%{a}%") for a in aliases]))
        if isinstance(difficulty, str) and difficulty.strip() and difficulty.lower().strip() != "mixed":
            query = query.filter(QuizQuestion.difficulty == difficulty.lower().strip())

    # Lấy danh sách ngẫu nhiên hoặc theo thứ tự
    if randomize:
        items = query.order_by(func.random()).limit(limit).all()
    else:
        items = query.order_by(QuizQuestion.id.asc()).limit(limit).all()

    # Nếu chưa có câu hỏi nào theo filter và không chỉ định post_id/course_id, lấy câu hỏi chung
    if not items and post_id is None and course_id is None:
        fallback_q = db.query(QuizQuestion)
        if randomize:
            items = fallback_q.order_by(func.random()).limit(limit).all()
        else:
            items = fallback_q.order_by(QuizQuestion.id.asc()).limit(limit).all()

        # Batch fetch post links for all retrieved questions to eliminate N+1 queries
    q_ids = [item.id for item in items]
    links_by_qid = {}
    if q_ids:
        post_links = db.query(QuizQuestionPost.question_id, QuizQuestionPost.post_id).filter(
            QuizQuestionPost.question_id.in_(q_ids)
        ).all()
        for qid, pid in post_links:
            links_by_qid.setdefault(qid, []).append(pid)

    response_items: List[QuizQuestionResponse] = []
    for item in items:
        pids = links_by_qid.get(item.id, [])

        opts = item.options
        if isinstance(opts, str):
            try:
                opts = json.loads(opts)
            except Exception:
                opts = []

        response_items.append(
            QuizQuestionResponse(
                id=item.id,
                course_id=item.course_id,
                language=item.language or "",
                question=item.question,
                options=opts if isinstance(opts, list) else [],
                answer_index=item.answer_index,
                explanation=item.explanation or "",
                difficulty=item.difficulty or "medium",
                source=item.source or "manual",
                created_by=item.created_by,
                created_at=item.created_at,
                post_ids=pids
            )
        )

    return {"questions": response_items}


@router.post("/questions", response_model=Dict[str, Any], status_code=status.HTTP_201_CREATED)
def create_quiz_question(
    data: QuizQuestionCreate,
    db: Session = Depends(get_db),
    current_user: Optional[User] = Depends(get_current_user_optional)
):
    """
    Thêm mới thủ công 1 câu hỏi trắc nghiệm vào ngân hàng đề thi và lưu vào PostgreSQL.
    """
    if not data.question.strip():
        raise HTTPException(status_code=400, detail="Nội dung câu hỏi không được để trống.")
    clean_options = [opt.strip() for opt in data.options if isinstance(opt, str) and opt.strip()]
    if len(clean_options) < 2:
        raise HTTPException(status_code=400, detail="Câu hỏi phải có ít nhất 2 phương án lựa chọn không rỗng.")

    # Normalize answer_index / correct_index
    idx = data.answer_index
    if idx is None or idx < 0:
        idx = getattr(data, "correct_index", None)
    if idx is None or idx < 0:
        idx = 0
    if idx >= len(clean_options):
        idx = 0

    # Normalize difficulty
    raw_diff = str(data.difficulty or "medium").lower().strip()
    diff_map = {
        "easy": "easy", "basic": "easy", "cơ bản": "easy", "co ban": "easy", "dễ": "easy", "de": "easy",
        "medium": "medium", "intermediate": "medium", "trung bình": "medium", "trung binh": "medium", "vừa": "medium", "vua": "medium",
        "hard": "hard", "advanced": "hard", "nâng cao": "hard", "nang cao": "hard", "khó": "hard", "kho": "hard"
    }
    if raw_diff not in diff_map:
        raise HTTPException(status_code=400, detail="Độ khó câu hỏi không hợp lệ. Cho phép: easy, medium, hard.")
    clean_diff = diff_map[raw_diff]

    if data.course_id:
        course = db.query(Course).filter(Course.id == data.course_id).first()
        if not course:
            raise HTTPException(status_code=400, detail="Khóa học được chỉ định không tồn tại.")

    user_roles = [r.name.lower() for r in getattr(current_user, "roles", [])] if current_user else []
    is_staff = bool(
        current_user and (
            getattr(current_user, "is_superuser", False)
            or "admin" in user_roles
            or "moderator" in user_roles
        )
    )
    if not is_staff:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Chỉ Quản trị viên (Admin) hoặc Kiểm duyệt viên mới có quyền thêm câu hỏi trắc nghiệm."
        )

    user_id = getattr(current_user, "id", None) if (current_user and not hasattr(current_user, "dependency")) else None

    # Handle code snippet if attached
    final_question = data.question.strip()
    if hasattr(data, "code_snippet") and data.code_snippet and data.code_snippet.strip():
        snippet = data.code_snippet.strip()
        if snippet not in final_question:
            final_question = f"{final_question}\n\n```\n{snippet}\n```"

    question = QuizQuestion(
        course_id=data.course_id,
        language=data.language or "IT",
        question=final_question,
        options=clean_options,
        answer_index=idx,
        explanation=data.explanation or "",
        difficulty=clean_diff,
        source="manual",
        created_by=user_id
    )
    db.add(question)
    db.flush()

    if data.post_ids:
        for pid in data.post_ids:
            if db.query(Post.id).filter(Post.id == pid).first():
                link = QuizQuestionPost(question_id=question.id, post_id=pid)
                db.add(link)

    db.commit()
    db.refresh(question)

    return {
        "ok": True,
        "message": "Tạo câu hỏi trắc nghiệm thành công và đã lưu vào cơ sở dữ liệu PostgreSQL.",
        "id": question.id,
        "question_id": question.id,
        "language": question.language,
        "question": question.question,
        "options": question.options,
        "answer_index": question.answer_index,
        "correct_index": question.answer_index,
        "explanation": question.explanation or "",
        "difficulty": question.difficulty,
        "course_id": question.course_id,
        "source": question.source,
        "created_at": question.created_at.isoformat() if question.created_at else None
    }


@router.delete("/questions/{id}")
def delete_quiz_question(
    id: int,
    db: Session = Depends(get_db),
    current_user: Optional[User] = Depends(get_current_user_optional)
):
    """
    Xóa 1 câu hỏi trắc nghiệm khỏi hệ thống (Yêu cầu tác giả hoặc Admin/Moderator).
    """
    q = db.query(QuizQuestion).filter(QuizQuestion.id == id).first()
    if not q:
        raise HTTPException(status_code=404, detail="Câu hỏi không tồn tại.")

    if q.created_by is not None:
        if not current_user:
            raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Vui lòng đăng nhập để thực hiện thao tác này.")
        is_admin = current_user.is_superuser or any(r.name in ["admin", "moderator"] for r in current_user.roles)
        if q.created_by != current_user.id and not is_admin:
            raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Bạn không có quyền xóa câu hỏi này.")

    # Clean up dependent associations
    db.query(QuizQuestionPost).filter(QuizQuestionPost.question_id == id).delete(synchronize_session=False)
    db.delete(q)
    db.commit()
    return {"ok": True, "deleted_id": id}


@router.post("/generate", status_code=status.HTTP_201_CREATED)
def generate_quiz_with_ai(
    req: QuizGenerateRequest,
    db: Session = Depends(get_db),
    current_user: Optional[User] = Depends(get_current_user_optional)
):
    """
    Tự động sinh bộ câu hỏi trắc nghiệm bằng Trí tuệ Nhân tạo (Gemini AI)
    dựa trên nội dung Khóa học hoặc các Bài viết được chỉ định.
    Chỉ Quản trị viên (Admin) mới có quyền tạo đề thi bằng AI.
    """
    user_roles = [r.name.lower() for r in getattr(current_user, "roles", [])] if current_user else []
    is_staff = bool(
        current_user and (
            getattr(current_user, "is_superuser", False)
            or "admin" in user_roles
            or "moderator" in user_roles
        )
    )
    if not is_staff:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Chỉ Quản trị viên (Admin) hoặc Kiểm duyệt viên mới có quyền tạo đề trắc nghiệm bằng AI."
        )

    materials = []
    lang = req.language or ""
    course = None
    posts = []
    valid_course_id = None

    if req.course_id:
        course = db.query(Course).filter(Course.id == req.course_id).first()
        if not course:
            raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Khóa học không tồn tại.")
        valid_course_id = course.id
        if not lang:
            lang = course.title
        course_text = f"KHÓA HỌC: {course.title}\nMô tả: {course.description}\n"
        if course.lessons:
            for idx, lesson in enumerate(course.lessons, 1):
                course_text += f"\nBài {idx}: {lesson.title}\n{lesson.content[:1000]}"
        materials.append(course_text)

    if req.post_ids:
        all_posts = db.query(Post).filter(Post.id.in_(req.post_ids)).all()
        from app.services.quiz_generator import is_meaningful_post_content
        posts = [p for p in all_posts if is_meaningful_post_content(p)]
        if not posts and all_posts:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Bài viết chứa nội dung hoặc tiêu đề không mang nhiều ý nghĩa (ký tự lặp vô nghĩa hoặc quá ngắn) để tạo câu hỏi trắc nghiệm."
            )
        from app.services.quiz_generator import _classify_post_domain, DOMAIN_TO_LANG

        for idx, post in enumerate(posts, 1):
            materials.append(f"--- BÀI {idx}: {post.title} ---\n{post.content[:3000]}")

        # Intelligently resolve the canonical language/domain from post title, material & requested topic
        combined_text = f"{req.language or ''} {' '.join([p.title for p in posts])} {' '.join([(p.content or '')[:200] for p in posts])}".lower()
        if "git" in combined_text or "github" in combined_text or "version control" in combined_text or "quản lý phiên bản" in combined_text:
            lang = "Git"
        elif "c++" in combined_text or "cpp" in combined_text or "lập trình c" in combined_text:
            lang = "C++"
        elif "http" in combined_text or "giao thức http" in combined_text:
            lang = "HTTP"
        elif "python" in combined_text:
            lang = "Python"
        elif len(posts) == 1:
            dom = _classify_post_domain(posts[0])
            lang = DOMAIN_TO_LANG.get(dom) or (posts[0].category.name if posts[0].category else "IT")
        elif not lang:
            lang = "IT"

    if not materials:
        materials.append(f"Chủ đề kỹ thuật và kiến thức lập trình: {lang or 'Công nghệ thông tin, Lập trình Web & Backend'}")

    context_str = "\n\n".join(materials)[:8000]
    count = max(1, min(req.count, 10))
    difficulty = req.difficulty if req.difficulty in ["easy", "medium", "hard"] else "medium"

    prompt = (
        f"Bạn là giảng viên IT cấp cao. Dựa trên tài liệu sau, hãy tạo đúng {count} câu hỏi trắc nghiệm "
        f"4 lựa chọn về {lang or 'chủ đề IT'}, độ khó: {difficulty}.\n"
        "Chỉ trả về định dạng JSON thuần (mảng các đối tượng). Mỗi đối tượng gồm:\n"
        "{\n"
        '  "question": "Nội dung câu hỏi rõ ràng, thực tế",\n'
        '  "options": ["Lựa chọn A", "Lựa chọn B", "Lựa chọn C", "Lựa chọn D"],\n'
        '  "answer_index": 0,\n'
        '  "explanation": "Giải thích chi tiết vì sao đáp án này đúng",\n'
        '  "difficulty": "easy" | "medium" | "hard"\n'
        "}\n\n"
        f"TÀI LIỆU HỌC TẬP:\n{context_str}"
    )

    generated_questions: List[Dict[str, Any]] = []
    via = "fallback_local"

    try:
        ai_resp = gemini_service.call_gemini(
            prompt=prompt,
            system_instruction="Bạn là trợ lý AI chuyên gia giáo dục kỹ thuật. Hãy luôn trả về JSON hợp lệ theo đúng cấu trúc yêu cầu.",
            json_mode=True,
            temperature=0.3
        )
        if ai_resp:
            parsed = json.loads(ai_resp)
            if isinstance(parsed, dict) and "questions" in parsed:
                parsed = parsed["questions"]
            if isinstance(parsed, list):
                valid_items = [
                    item for item in parsed
                    if isinstance(item, dict)
                    and item.get("question")
                    and isinstance(item.get("options"), list)
                    and len(item["options"]) >= 2
                ]
                if valid_items:
                    generated_questions = valid_items[:count]
                    via = "gemini"
    except Exception as e:
        logger.warning(f"AI Quiz Generation fallback due to: {e}")

    # Fallback to local intelligent questions generator if AI failed or unconfigured
    if not generated_questions:
        if posts and len(posts) == 1:
            from app.services.quiz_generator import generate_questions_from_post_text
            extracted = generate_questions_from_post_text(posts[0], count=count)
            if extracted:
                generated_questions = extracted
                via = "content_extracted"

        if not generated_questions:
            generated_questions = build_fallback_questions(
                material=context_str,
                count=count,
                language=lang or "IT",
                difficulty=difficulty
            )
            via = "fallback_local"

    user_id = getattr(current_user, "id", None) if (current_user and not hasattr(current_user, "dependency")) else None
    created = []

    for q_data in generated_questions:
        q_lang = (q_data.get("language") or q_data.get("lang") or lang or "IT").strip()
        q_obj = QuizQuestion(
            course_id=valid_course_id,
            language=q_lang,
            question=str(q_data.get("question", "")).strip(),
            options=q_data.get("options", []),
            answer_index=int(q_data.get("answer_index", 0)),
            explanation=str(q_data.get("explanation", "")),
            difficulty=q_data.get("difficulty", difficulty) if q_data.get("difficulty") in ["easy", "medium", "hard"] else difficulty,
            source="ai",
            created_by=user_id
        )
        db.add(q_obj)
        db.flush()

        # Link with posts
        for p in posts:
            db.add(QuizQuestionPost(question_id=q_obj.id, post_id=p.id))

        opts = q_obj.options
        if isinstance(opts, str):
            try:
                opts = json.loads(opts)
            except Exception:
                opts = []

        created.append({
            "id": q_obj.id,
            "course_id": q_obj.course_id,
            "question": q_obj.question,
            "options": opts if isinstance(opts, list) else [],
            "answer_index": q_obj.answer_index,
            "explanation": q_obj.explanation or "",
            "difficulty": q_obj.difficulty,
            "language": q_obj.language or lang
        })

    db.commit()

    return {
        "ok": True,
        "via": via,
        "count": len(created),
        "created": created
    }


@router.post("/submit", response_model=QuizSubmitResponse)
def submit_quiz_answers(
    data: QuizSubmitRequest,
    db: Session = Depends(get_db)
):
    """
    Chấm điểm bài làm trắc nghiệm của học viên và trả về đáp án, giải thích chi tiết.
    """
    if not data.answers:
        return QuizSubmitResponse(
            total_questions=0,
            correct_count=0,
            score_percentage=0.0,
            details=[]
        )

    try:
        question_ids = [int(k) for k in data.answers.keys()]
    except (ValueError, TypeError):
        question_ids = []

    questions = db.query(QuizQuestion).filter(QuizQuestion.id.in_(question_ids)).all() if question_ids else []
    q_map = {q.id: q for q in questions}

    details: List[QuizSubmitDetail] = []
    correct_count = 0

    for qid, user_choice in data.answers.items():
        try:
            int_qid = int(qid)
        except (ValueError, TypeError):
            continue
        q = q_map.get(int_qid)
        if not q:
            continue

        is_correct = (user_choice == q.answer_index)
        if is_correct:
            correct_count += 1

        details.append(
            QuizSubmitDetail(
                question_id=q.id,
                is_correct=is_correct,
                selected_index=user_choice,
                correct_index=q.answer_index,
                explanation=q.explanation or ""
            )
        )

    total = len(details)
    score = round((correct_count / total) * 100, 1) if total > 0 else 0.0

    return QuizSubmitResponse(
        total_questions=total,
        correct_count=correct_count,
        score_percentage=score,
        details=details
    )
