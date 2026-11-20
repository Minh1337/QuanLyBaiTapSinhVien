import json
import logging
import random
import re
from typing import List, Dict, Any, Optional
from sqlalchemy.orm import Session

from app.models.post import Post
from app.models.quiz import QuizQuestion, QuizQuestionPost
from app.services.gemini_service import gemini_service

logger = logging.getLogger(__name__)


def _shuffle_question_options(q_dict: Dict[str, Any]) -> Dict[str, Any]:
    """Helper to shuffle options while preserving the correct answer index."""
    options = list(q_dict.get("options", []))
    correct_idx = q_dict.get("answer_index", 0)
    if not options or correct_idx >= len(options):
        return q_dict

    correct_option = options[correct_idx]
    random.shuffle(options)
    new_correct_idx = options.index(correct_option)

    res = q_dict.copy()
    res["options"] = options
    res["answer_index"] = new_correct_idx
    return res


def build_domain_fallback_bank() -> Dict[str, List[Dict[str, Any]]]:
    """Pre-crafted, accurate technical questions categorized by technical domain."""
    return {
        "web_basic": [
            {
                "question": "Trong kiến trúc xây dựng website cơ bản, ngôn ngữ nào đóng vai trò xác định 'bộ khung xương' và cấu trúc ngữ nghĩa của trang web?",
                "options": [
                    "HTML (HyperText Markup Language)",
                    "CSS (Cascading Style Sheets)",
                    "JavaScript (JS Engine)",
                    "SQL (Structured Query Language)"
                ],
                "answer_index": 0,
                "explanation": "HTML cung cấp cấu trúc ngữ nghĩa bằng các phần tử (tags/elements) để trình duyệt hiển thị đúng nội dung văn bản, ảnh, video của website.",
                "difficulty": "easy",
                "language": "HTML/Web"
            },
            {
                "question": "Thẻ `<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">` trong phần `<head>` của trang web có vai trò cốt lõi nào?",
                "options": [
                    "Đảm bảo website hiển thị thích ứng chuẩn xác (Responsive) trên màn hình thiết bị di động",
                    "Kích hoạt nạp tệp JavaScript bất đồng bộ",
                    "Mã hóa bảo mật toàn bộ dữ liệu người dùng gửi lên",
                    "Tự động làm mới trang web sau mỗi 10 giây"
                ],
                "answer_index": 0,
                "explanation": "Thẻ meta viewport thiết lập kích thước vùng nhìn bằng với độ rộng thiết bị và tỷ lệ phóng đại ban đầu là 1.0, là chuẩn mực bắt buộc cho thiết kế Web thích ứng.",
                "difficulty": "easy",
                "language": "HTML5"
            },
            {
                "question": "Trong CSS hiện đại, thuộc tính nào được sử dụng để kích hoạt mô hình dàn trang 1 chiều linh hoạt, tự động căn chỉnh và phân bổ không gian giữa các phần tử?",
                "options": [
                    "display: flex",
                    "position: absolute",
                    "float: left",
                    "display: inline"
                ],
                "answer_index": 0,
                "explanation": "Mô hình Flexbox (display: flex) cho phép thiết kế giao diện linh hoạt, dễ dàng căn giữa (justify-content, align-items) và co giãn theo kích thước khung chứa.",
                "difficulty": "medium",
                "language": "CSS3"
            },
            {
                "question": "Vai trò quan trọng nhất của ngôn ngữ JavaScript khi chạy trên trình duyệt web là gì?",
                "options": [
                    "Bắt các sự kiện tương tác của người dùng, thực thi logic nghiệp vụ và cập nhật cây cấu trúc DOM động",
                    "Định dạng màu sắc, font chữ và căn chỉnh lề margin cho trang web",
                    "Quản lý việc lưu trữ bảng dữ liệu quan hệ tại máy chủ database",
                    "Biên dịch mã HTML sang mã nhị phân của hệ điều hành"
                ],
                "answer_index": 0,
                "explanation": "JavaScript mang lại tính tương tác cho trang web, cho phép phản hồi các thao tác như click, gõ phím, kéo thả và gọi API lấy dữ liệu mà không cần tải lại trang.",
                "difficulty": "easy",
                "language": "JavaScript"
            },
            {
                "question": "Giao thức truyền tải web an toàn sử dụng cổng chuẩn 443 kết hợp chứng chỉ số TLS/SSL để mã hóa đường truyền dữ liệu là gì?",
                "options": [
                    "HTTPS (HyperText Transfer Protocol Secure)",
                    "HTTP không mã hóa (cổng 80)",
                    "FTP (File Transfer Protocol - cổng 21)",
                    "Telnet (cổng 23)"
                ],
                "answer_index": 0,
                "explanation": "HTTPS mã hóa toàn bộ dữ liệu trao đổi giữa trình duyệt của người dùng và máy chủ, ngăn chặn nghe lén thông tin và bảo vệ quyền riêng tư.",
                "difficulty": "easy",
                "language": "Web Security"
            },
            {
                "question": "Trong HTML5, các thẻ ngữ nghĩa (Semantic Tags) như `<header>`, `<nav>`, `<main>`, `<article>`, `<section>`, `<footer>` mang lại lợi ích quan trọng nhất nào?",
                "options": [
                    "Giúp cấu trúc trang web rõ ràng, tăng khả năng tiếp cận (Accessibility) và tối ưu hóa công cụ tìm kiếm (SEO)",
                    "Làm cho trang web tự động đổi màu mà không cần CSS",
                    "Tăng tốc độ kết nối Internet của người dùng",
                    "Bắt buộc mã nguồn phải viết bằng TypeScript"
                ],
                "answer_index": 0,
                "explanation": "Thẻ ngữ nghĩa (Semantic Elements) giúp các trình đọc màn hình (Screen Readers) và bot tìm kiếm (Googlebot) hiểu rõ vai trò của từng khối nội dung trên website.",
                "difficulty": "easy",
                "language": "HTML5"
            },
            {
                "question": "Sự khác biệt cơ bản giữa phần tử khối (Block Element như `<div>`, `<p>`) và phần tử nội dòng (Inline Element như `<span>`, `<a>`) trong HTML là gì?",
                "options": [
                    "Phần tử Block luôn bắt đầu trên một dòng mới và chiếm toàn bộ chiều rộng có sẵn, trong khi Inline chỉ chiếm độ rộng vừa đủ nội dung",
                    "Phần tử Inline không thể chứa chữ",
                    "Phần tử Block chỉ dùng được trong phần <head>",
                    "Phần tử Inline tự động tạo cơ sở dữ liệu"
                ],
                "answer_index": 0,
                "explanation": "Phần tử Block (như div, p, h1-h6) bắt đầu trên dòng mới và kéo dài tối đa chiều rộng vùng chứa; phần tử Inline (span, a, strong) nằm cùng hàng với các phần tử khác xung quanh.",
                "difficulty": "easy",
                "language": "HTML/Web"
            },
            {
                "question": "Mô hình hộp (CSS Box Model) trong thiết kế website bao gồm 4 thành phần theo thứ tự từ trong ra ngoài là gì?",
                "options": [
                    "Content (Nội dung) -> Padding (Đệm) -> Border (Viền) -> Margin (Lề)",
                    "Margin -> Border -> Padding -> Content",
                    "Border -> Margin -> Content -> Padding",
                    "Content -> Margin -> Border -> Padding"
                ],
                "answer_index": 0,
                "explanation": "CSS Box Model gồm: Content (nội dung text/ảnh), Padding (khoảng cách từ nội dung tới viền), Border (đường viền bao quanh) và Margin (khoảng cách bên ngoài viền với phần tử khác).",
                "difficulty": "easy",
                "language": "CSS3"
            },
            {
                "question": "Khi cần xây dựng bố cục dàn trang website 2 chiều phức tạp (cả hàng dọc lẫn cột ngang đồng thời), giải pháp CSS hiện đại nào tối ưu nhất?",
                "options": [
                    "CSS Grid Layout (display: grid)",
                    "Dùng thẻ <table> lồng nhau",
                    "Dùng thuộc tính float: left cho mọi thẻ",
                    "Chỉ dùng margin âm"
                ],
                "answer_index": 0,
                "explanation": "CSS Grid là hệ thống dàn trang 2 chiều (2-dimensional) mạnh mẽ nhất của CSS, cho phép chia trang web thành các hàng (rows) và cột (columns) với kích thước linh hoạt.",
                "difficulty": "medium",
                "language": "CSS3"
            },
            {
                "question": "Trong JavaScript, phương thức chuẩn hiện đại nào được sử dụng phổ biến nhất để tìm và chọn phần tử đầu tiên trong DOM khớp với một CSS Selector?",
                "options": [
                    "document.querySelector()",
                    "document.findFirstElement()",
                    "document.searchCSS()",
                    "window.selectNode()"
                ],
                "answer_index": 0,
                "explanation": "document.querySelector('selector') cho phép chọn phần tử theo bất kỳ cú pháp CSS selector hợp lệ nào (như '.my-class', '#my-id', 'div > p') rất linh hoạt.",
                "difficulty": "easy",
                "language": "JavaScript"
            }
        ],
        "react": [
            {
                "question": "Trong React 19, hook nào được chuẩn hóa để xử lý Form Actions và tự động quản lý các trạng thái pending, error và kết quả trả về?",
                "options": [
                    "useActionState()",
                    "useEffect() kết hợp useState()",
                    "useReducer() truyền thống",
                    "useMemo() với dependency array"
                ],
                "answer_index": 0,
                "explanation": "React 19 giới thiệu hook useActionState giúp đơn giản hóa việc submit form và quản lý trạng thái tải/lỗi mà không cần viết boilerplate thủ công.",
                "difficulty": "medium",
                "language": "React 19"
            },
            {
                "question": "Trong Tailwind CSS v4, tính năng cấu hình giao diện mới nào thay thế cho tệp `tailwind.config.js` truyền thống?",
                "options": [
                    "Cấu hình trực tiếp bằng CSS-first với chỉ thị `@theme` trong tệp CSS chính",
                    "Sử dụng tệp XML riêng biệt mang tên `tailwind.xml`",
                    "Chuyển toàn bộ cấu hình sang biến môi trường `.env`",
                    "Bắt buộc cấu hình qua runtime JavaScript inline"
                ],
                "answer_index": 0,
                "explanation": "Tailwind CSS v4 chuyển sang kiến trúc CSS-first sử dụng chỉ thị @theme trực tiếp trong CSS, giúp tăng tốc độ build bằng Lightning CSS.",
                "difficulty": "medium",
                "language": "Tailwind CSS"
            },
            {
                "question": "Khái niệm Virtual DOM (DOM ảo) trong React có mục đích cốt lõi là gì?",
                "options": [
                    "Tối ưu hiệu năng bằng cách tính toán sự khác biệt (diffing) trong bộ nhớ trước khi áp dụng thay đổi tối thiểu lên Real DOM",
                    "Thay thế hoàn toàn bộ nhớ RAM của trình duyệt",
                    "Mã hóa bảo mật mã nguồn JavaScript trên máy khách",
                    "Tự động chuyển đổi code JSX sang ngôn ngữ C++"
                ],
                "answer_index": 0,
                "explanation": "Thao tác trên DOM thật rất chậm; React dùng Virtual DOM để so sánh (Reconciliation) và chỉ cập nhật những node thực sự thay đổi trên DOM thật.",
                "difficulty": "easy",
                "language": "React"
            },
            {
                "question": "Quy tắc quan trọng (Rule of Hooks) nào trong React bắt buộc lập trình viên phải tuân thủ?",
                "options": [
                    "Chỉ gọi Hooks ở cấp cao nhất của Functional Component, không gọi trong vòng lặp (loop), câu lệnh điều kiện (if) hay hàm lồng nhau",
                    "Mọi Hook đều phải được khai báo trong Class Component",
                    "Không được phép truyền tham số vào React Hook",
                    "Bắt buộc đặt tên Hook bắt đầu bằng chữ 'get' thay vì 'use'"
                ],
                "answer_index": 0,
                "explanation": "Quy tắc này đảm bảo các React Hooks luôn được thực thi theo cùng một thứ tự ở mỗi lần component re-render.",
                "difficulty": "easy",
                "language": "React"
            },
            {
                "question": "Trong React 19, thẻ `<form action={...}>` có thể nhận trực tiếp loại giá trị nào để thực thi cả ở máy chủ (Server Action) hoặc máy khách (Client Action)?",
                "options": [
                    "Một hàm bất đồng bộ (Async Function) xử lý dữ liệu Form",
                    "Chỉ nhận một chuỗi URL string trỏ tới endpoint backend",
                    "Chỉ chấp nhận đối tượng JSON tĩnh",
                    "Bắt buộc phải truyền một biến Boolean true/false"
                ],
                "answer_index": 0,
                "explanation": "React 19 nâng cấp thuộc tính action của thẻ form cho phép truyền trực tiếp function xử lý dữ liệu tự động tích hợp với useActionState.",
                "difficulty": "medium",
                "language": "React 19"
            }
        ],
        "fastapi": [
            {
                "question": "FastAPI sử dụng thư viện nào làm nền tảng kiểm tra kiểu dữ liệu (Data Validation) và tự động sinh Swagger OpenAPI documentation?",
                "options": [
                    "Pydantic (Pydantic V2 được viết lại bằng Rust)",
                    "Marshmallow",
                    "Cerberus",
                    "Django Forms Serializer"
                ],
                "answer_index": 0,
                "explanation": "FastAPI được xây dựng dựa trên Pydantic để thực hiện validation dữ liệu tự động với hiệu năng cực cao và kiểu dữ liệu Python chuẩn (type hints).",
                "difficulty": "easy",
                "language": "FastAPI"
            },
            {
                "question": "Trong FastAPI, cơ chế nào cho phép tái sử dụng logic dùng chung như xác thực quyền (authentication), kết nối CSDL và xử lý phân quyền?",
                "options": [
                    "Hệ thống Dependency Injection thông qua hàm `Depends()`",
                    "Ghi đè thuộc tính toàn cục `__globals__`",
                    "Sử dụng biến static trong Class Singleton",
                    "Bắt buộc dùng decorators `@property` lồng nhau"
                ],
                "answer_index": 0,
                "explanation": "FastAPI có hệ thống Dependency Injection mạnh mẽ và trực quan thông qua Depends(), giúp inject DB Session, current user rất sạch sẽ và dễ test.",
                "difficulty": "medium",
                "language": "FastAPI"
            },
            {
                "question": "Mục đích quan trọng nhất của việc sử dụng Connection Pooling (nhóm kết nối) trong PostgreSQL với SQLAlchemy là gì?",
                "options": [
                    "Tái sử dụng các kết nối cơ sở dữ liệu đã mở, giảm chi phí overhead khởi tạo kết nối TCP/TLS tốn kém",
                    "Tự động mã hóa toàn bộ ổ đĩa cứng của server database",
                    "Xóa bỏ hoàn toàn nhu cầu đánh Index cho bảng dữ liệu",
                    "Tăng tốc độ mạng internet của máy tính client"
                ],
                "answer_index": 0,
                "explanation": "Khởi tạo kết nối mới tới PostgreSQL tiêu tốn nhiều tài nguyên CPU và RAM. Connection Pool duy trì sẵn các kết nối mở để tái sử dụng ngay lập tức.",
                "difficulty": "medium",
                "language": "PostgreSQL"
            },
            {
                "question": "Trong kiến trúc Microservices phân tán, Pattern nào thường được sử dụng để quản lý các transaction liên quan đến nhiều service mà không cần dùng Two-Phase Commit (2PC)?",
                "options": [
                    "Saga Pattern (Choreography hoặc Orchestration)",
                    "Singleton Pattern",
                    "Factory Pattern",
                    "Observer Pattern"
                ],
                "answer_index": 0,
                "explanation": "Saga Pattern chia transaction phân tán thành chuỗi các local transaction với các giao dịch bù trừ (compensating transactions) khi xảy ra lỗi.",
                "difficulty": "hard",
                "language": "Architecture"
            },
            {
                "question": "Khái niệm `async / await` trong Python và FastAPI mang lại lợi ích gì cho các tác vụ I/O bound (gọi database, HTTP API ngoài)?",
                "options": [
                    "Cho phép một tiến trình xử lý hàng ngàn request đồng thời (non-blocking concurrency) mà không bị block luồng khi đợi I/O",
                    "Tăng xung nhịp phần cứng CPU của máy chủ",
                    "Tự động biên dịch mã nguồn Python sang Java",
                    "Bỏ qua hoàn toàn việc kiểm tra lỗi runtime"
                ],
                "answer_index": 0,
                "explanation": "Asyncio giải phóng Event Loop khi đang chờ phản hồi từ CSDL hay network I/O, giúp server tiếp tục phục vụ các request khác song song.",
                "difficulty": "medium",
                "language": "Python"
            }
        ],
        "devops": [
            {
                "question": "Trong kỹ thuật viết Dockerfile cho ứng dụng sản phẩm (Production), Multi-stage Build mang lại lợi ích cốt lõi nào?",
                "options": [
                    "Giảm đáng kể dung lượng Docker Image thành phẩm bằng cách tách biệt môi trường build và runtime, loại bỏ công cụ biên dịch thừa",
                    "Cho phép nhiều container cùng chạy trên một cổng mạng duy nhất mà không bị xung đột",
                    "Tự động backup dữ liệu container lên đám mây",
                    "Thay thế hoàn toàn cụm Kubernetes"
                ],
                "answer_index": 0,
                "explanation": "Multi-stage build chỉ copy kết quả build cần thiết sang image runtime siêu nhẹ (như alpine hay distroless), giúp image nhẹ hơn và an toàn hơn.",
                "difficulty": "medium",
                "language": "Docker"
            },
            {
                "question": "Trong tệp cấu hình GitHub Actions Workflow (`.github/workflows/*.yml`), chỉ thị `on:` có nhiệm vụ gì?",
                "options": [
                    "Định nghĩa các sự kiện kích hoạt quy trình CI/CD (ví dụ: push, pull_request, schedule)",
                    "Thiết lập mật khẩu đăng nhập vào máy chủ",
                    "Cài đặt hệ điều hành cho máy tính cá nhân của lập trình viên",
                    "Chỉ định danh sách người nhận email báo cáo kết quả"
                ],
                "answer_index": 0,
                "explanation": "Chỉ thị 'on:' xác định các trigger (sự kiện) cụ thể trên kho mã nguồn GitHub sẽ làm quy trình CI/CD tự động khởi chạy.",
                "difficulty": "easy",
                "language": "CI/CD"
            },
            {
                "question": "Sự khác biệt cơ bản nhất giữa Docker Container và Máy ảo truyền thống (Virtual Machine) là gì?",
                "options": [
                    "Container chia sẻ chung nhân hệ điều hành (Host OS Kernel) nên khởi động tính bằng giây và tiêu thụ ít RAM hơn rất nhiều so với máy ảo",
                    "Máy ảo không thể chạy ứng dụng web",
                    "Container bắt buộc phải có card đồ họa rời GPU mới chạy được",
                    "Container luôn yêu cầu cài đặt đầy đủ một Guest OS riêng biệt"
                ],
                "answer_index": 0,
                "explanation": "Container đóng gói ứng dụng với user-space libraries nhưng dùng chung kernel của máy chủ, giúp khởi động gần như tức thì và cực kỳ tiết kiệm tài nguyên.",
                "difficulty": "easy",
                "language": "Docker"
            },
            {
                "question": "Trong Docker Compose (`docker-compose.yml`), thuộc tính `volumes:` thường được khai báo với mục đích gì?",
                "options": [
                    "Duy trì dữ liệu bền vững (persistence) bên ngoài vòng đời của container (như dữ liệu database)",
                    "Điều chỉnh âm lượng âm thanh của hệ thống máy chủ",
                    "Giới hạn tốc độ băng thông internet của ứng dụng",
                    "Tự động tăng RAM ảo cho container khi bị quá tải"
                ],
                "answer_index": 0,
                "explanation": "Dữ liệu bên trong container mặc định là tạm thời (ephemeral). Volumes ánh xạ thư mục ra máy chủ host để dữ liệu không bị mất khi container khởi động lại.",
                "difficulty": "easy",
                "language": "Docker"
            },
            {
                "question": "Lệnh Docker nào được sử dụng để xây dựng một image từ Dockerfile trong thư mục hiện tại và gắn thẻ (tag) định danh tên cho image đó?",
                "options": [
                    "docker build -t my-app:latest .",
                    "docker run -d my-app",
                    "docker pull my-app",
                    "docker commit my-app"
                ],
                "answer_index": 0,
                "explanation": "Lệnh 'docker build -t <tên:tag> <đường_dẫn>' thực hiện đọc Dockerfile, thực thi các bước hướng dẫn và đóng gói thành image hoàn chỉnh.",
                "difficulty": "easy",
                "language": "Docker"
            }
        ],
        "cpp": [
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
                "language": "C++"
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
                "language": "C++"
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
                "language": "C++"
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
                "language": "C++"
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
                "language": "C++"
            },
            {
                "question": "Trong C++, cặp toán tử nào được sử dụng để cấp phát bộ nhớ động trên vùng nhớ Heap và giải phóng bộ nhớ khi không còn sử dụng?",
                "options": [
                    "new và delete",
                    "malloc và free (thuần chuẩn C)",
                    "alloc và dealloc",
                    "create và destroy"
                ],
                "answer_index": 0,
                "explanation": "C++ cung cấp toán tử 'new' để cấp phát bộ nhớ động cho đối tượng (gọi constructor) và 'delete' để giải phóng bộ nhớ (gọi destructor).",
                "difficulty": "medium",
                "language": "C++"
            },
            {
                "question": "Khái niệm con trỏ (Pointer) trong ngôn ngữ lập trình C++ đại diện cho điều gì?",
                "options": [
                    "Một biến lưu trữ địa chỉ vùng nhớ của một biến hoặc đối tượng khác",
                    "Một hàm đặc biệt dùng để kết nối cơ sở dữ liệu",
                    "Một vòng lặp vô tận để kiểm tra phần cứng",
                    "Một file ảnh đính kèm trong mã nguồn"
                ],
                "answer_index": 0,
                "explanation": "Con trỏ trong C++ là biến chứa địa chỉ ô nhớ (memory address) của giá trị khác, được khai báo với ký tự * và thao tác qua toán tử lấy địa chỉ &.",
                "difficulty": "easy",
                "language": "C++"
            }
        ],
        "http": [
            {
                "question": "Trong giao thức HTTP, mã trạng thái (HTTP Status Code) nào biểu thị yêu cầu đã được máy chủ xử lý thành công và tạo mới tài nguyên (Resource Created)?",
                "options": [
                    "201 Created",
                    "200 OK",
                    "400 Bad Request",
                    "404 Not Found"
                ],
                "answer_index": 0,
                "explanation": "Mã 201 Created thường được trả về sau các thao tác POST thành công để thông báo tài nguyên mới đã được tạo và lưu trữ trên máy chủ.",
                "difficulty": "easy",
                "language": "HTTP"
            },
            {
                "question": "Trong các phương thức HTTP chuẩn của kiến trúc RESTful API, phương thức nào mang tính chất an toàn (Safe) và lũy kế (Idempotent) dùng để lấy dữ liệu mà không làm thay đổi trạng thái máy chủ?",
                "options": [
                    "GET",
                    "POST",
                    "PATCH",
                    "CONNECT"
                ],
                "answer_index": 0,
                "explanation": "Phương thức GET là safe và idempotent: gọi GET nhiều lần với cùng tham số sẽ trả về cùng kết quả và không gây tác dụng phụ lên dữ liệu máy chủ.",
                "difficulty": "easy",
                "language": "HTTP"
            },
            {
                "question": "Sự khác biệt cốt lõi giữa HTTP/1.1 và HTTP/2 là gì?",
                "options": [
                    "HTTP/2 hỗ trợ Multiplexing (truyền nhiều request/response đồng thời trên 1 kết nối TCP duy nhất) và nén Header HPACK",
                    "HTTP/2 chỉ chạy được trên mạng nội bộ",
                    "HTTP/1.1 không hỗ trợ truyền tải hình ảnh",
                    "HTTP/2 bắt buộc sử dụng định dạng XML"
                ],
                "answer_index": 0,
                "explanation": "HTTP/2 giải quyết vấn đề Head-of-line blocking của HTTP/1.1 nhờ cơ chế Multiplexing dạng binary stream trên 1 kết nối TCP duy nhất.",
                "difficulty": "medium",
                "language": "HTTP"
            },
            {
                "question": "Trong thiết kế RESTful API, sự khác biệt cơ bản giữa phương thức PUT và phương thức PATCH là gì?",
                "options": [
                    "PUT thay thế toàn bộ tài nguyên bằng dữ liệu mới, trong khi PATCH chỉ cập nhật từng phần (cột/trường) thay đổi",
                    "PUT dùng để xóa dữ liệu, PATCH dùng để tạo mới",
                    "PATCH không bao giờ gửi body",
                    "PUT chỉ dùng cho tài liệu hình ảnh"
                ],
                "answer_index": 0,
                "explanation": "Theo chuẩn HTTP RFC: PUT là idempotent thay thế hoàn toàn tài nguyên, PATCH áp dụng cập nhật cục bộ cho một phần tài nguyên.",
                "difficulty": "medium",
                "language": "HTTP"
            },
            {
                "question": "Header nào trong HTTP Request hoặc Response được sử dụng để thông báo định dạng kiểu dữ liệu (MIME Type) của phần thân thông điệp (Payload)?",
                "options": [
                    "Content-Type: application/json",
                    "Authorization: Bearer <token>",
                    "User-Agent: Mozilla/5.0",
                    "Cache-Control: no-cache"
                ],
                "answer_index": 0,
                "explanation": "Header 'Content-Type' cho trình duyệt hoặc máy chủ biết chính xác định dạng dữ liệu (JSON, HTML, form-data, XML) để thực hiện parse tương ứng.",
                "difficulty": "easy",
                "language": "HTTP"
            },
            {
                "question": "Sự khác nhau giữa mã trạng thái HTTP 401 Unauthorized và 403 Forbidden là gì?",
                "options": [
                    "401 có nghĩa chưa xác thực danh tính (chưa đăng nhập hoặc token không hợp lệ), còn 403 có nghĩa đã xác thực nhưng không đủ quyền hạn truy cập tài nguyên",
                    "401 là lỗi máy chủ, 403 là lỗi mạng",
                    "403 dùng khi database bị ngắt kết nối",
                    "Không có sự khác nhau giữa hai mã này"
                ],
                "answer_index": 0,
                "explanation": "401 yêu cầu client cung cấp thông tin xác thực (Credentials/Token). 403 là server hiểu rõ client là ai nhưng từ chối thực hiện vì không có quyền.",
                "difficulty": "medium",
                "language": "HTTP"
            }
        ],
        "python": [
            {
                "question": "Cơ chế nào trong Python 3.11+ giúp tăng tốc độ thực thi mã nguồn lên tới 10-60% so với Python 3.10?",
                "options": [
                    "Dự án Faster CPython (Adaptive specializing interpreter)",
                    "Loại bỏ hoàn toàn GIL (Global Interpreter Lock)",
                    "Chuyển sang JIT compiler của PyPy",
                    "Bắt buộc biên dịch AOT"
                ],
                "answer_index": 0,
                "explanation": "Python 3.11 triển khai dự án Faster CPython với Adaptive Specializing Interpreter giúp tối ưu các bytecode thường xuyên được gọi.",
                "difficulty": "hard",
                "language": "Python"
            },
            {
                "question": "Khái niệm Generator trong Python sử dụng từ khóa nào để tạm dừng hàm và trả về từng giá trị tuần tự mà không cần nạp toàn bộ danh sách vào RAM?",
                "options": [
                    "yield",
                    "return",
                    "break",
                    "continue"
                ],
                "answer_index": 0,
                "explanation": "Từ khóa yield biến một hàm thành Generator function, trả về từng giá trị theo nhu cầu (lazy evaluation) giúp tiết kiệm bộ nhớ tối đa.",
                "difficulty": "medium",
                "language": "Python"
            },
            {
                "question": "Trong Python, cú pháp `with open(...) as f:` (Context Manager) mang lại lợi ích quan trọng nhất nào khi thao tác với tệp tin hoặc kết nối mạng?",
                "options": [
                    "Đảm bảo tài nguyên (file, socket) luôn được tự động đóng an toàn ngay cả khi xảy ra Exception",
                    "Tự động dịch nội dung file sang tiếng Việt",
                    "Tăng tốc độ đọc ghi đĩa cứng lên gấp đôi",
                    "Mã hóa bảo mật toàn bộ tệp tin"
                ],
                "answer_index": 0,
                "explanation": "Context Manager đảm bảo phương thức __exit__ luôn được gọi để giải phóng tài nguyên an toàn.",
                "difficulty": "easy",
                "language": "Python"
            }
        ],
        "database": [
            {
                "question": "Mục đích quan trọng nhất của việc sử dụng Connection Pooling trong PostgreSQL với SQLAlchemy là gì?",
                "options": [
                    "Tái sử dụng các kết nối cơ sở dữ liệu đã mở, giảm overhead khởi tạo kết nối TCP/TLS tốn kém",
                    "Tự động mã hóa toàn bộ ổ đĩa cứng của server database",
                    "Xóa bỏ hoàn toàn nhu cầu đánh Index cho bảng dữ liệu",
                    "Tăng tốc độ mạng internet của máy tính client"
                ],
                "answer_index": 0,
                "explanation": "Khởi tạo kết nối mới tới PostgreSQL tiêu tốn nhiều tài nguyên CPU và RAM. Connection Pool duy trì sẵn các kết nối mở để tái sử dụng ngay lập tức.",
                "difficulty": "medium",
                "language": "PostgreSQL"
            },
            {
                "question": "Trong PostgreSQL, loại Index nào thường được tối ưu hóa đặc biệt cho các cột kiểu mảng (Array), JSONB hoặc tìm kiếm toàn văn bản (Full-text Search)?",
                "options": [
                    "GIN (Generalized Inverted Index)",
                    "B-tree mặc định",
                    "Hash Index",
                    "BRIN (Block Range Index)"
                ],
                "answer_index": 0,
                "explanation": "GIN Index rất phù hợp cho kiểu dữ liệu đa giá trị (composite values) như JSONB hoặc mảng vì nó lập chỉ mục cho từng phần tử bên trong.",
                "difficulty": "medium",
                "language": "PostgreSQL"
            }
        ],
        "git": [
            {
                "question": "Trong hệ thống quản lý phiên bản phân tán Git, lệnh nào được sử dụng để tải toàn bộ lịch sử và mã nguồn của một kho chứa từ xa (Remote Repository) về máy tính cá nhân?",
                "options": [
                    "git clone <repository_url>",
                    "git pull --all",
                    "git fork <url>",
                    "git copy <url>"
                ],
                "answer_index": 0,
                "explanation": "Lệnh 'git clone' sao chép toàn bộ kho mã nguồn từ xa bao gồm mọi nhánh (branches) và lịch sử commit về máy tính local.",
                "difficulty": "easy",
                "language": "Git"
            },
            {
                "question": "Trong Git, thao tác nào được sử dụng để tạm thời lưu trữ (cất đi) các thay đổi đang làm dở trong thư mục làm việc mà chưa muốn tạo commit?",
                "options": [
                    "git stash (kèm git stash pop để lấy lại)",
                    "git reset --hard",
                    "git clean -f",
                    "git checkout -b"
                ],
                "answer_index": 0,
                "explanation": "Lệnh 'git stash' lưu tạm thời các thay đổi (cả staged và unstaged) vào ngăn xếp stash để bạn có thể chuyển nhánh sạch sẽ mà không làm mất code.",
                "difficulty": "easy",
                "language": "Git"
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
                "language": "Git"
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
                "language": "Git"
            },
            {
                "question": "Khi đã commit nhầm hoặc muốn quay ngược lại commit trước đó trên nhánh đang làm việc một cách an toàn mà vẫn giữ lại code đã sửa đổi trong thư mục làm việc, lệnh nào tối ưu nhất?",
                "options": [
                    "git reset --soft HEAD~1",
                    "git reset --hard HEAD~1",
                    "git branch -D main",
                    "git push --force"
                ],
                "answer_index": 0,
                "explanation": "'git reset --soft HEAD~1' đưa HEAD về commit trước đó nhưng vẫn giữ nguyên tất cả thay đổi trong Staging Area để bạn có thể chỉnh sửa lại và commit tiếp.",
                "difficulty": "medium",
                "language": "Git"
            }
        ]
    }


DOMAIN_TO_LANG = {
    "git": "Git",
    "cpp": "C++",
    "http": "HTTP",
    "python": "Python",
    "fastapi": "FastAPI",
    "database": "PostgreSQL",
    "devops": "DevOps",
    "react": "React",
    "web_basic": "HTML/Web"
}


def _classify_post_domain(post: Post) -> str:
    """Classify the post into technical domain based on weighted title, tags, category and content."""
    # Normalize multiple whitespace in title and content
    title_text = re.sub(r"\s+", " ", (post.title or "").lower().strip())
    tag_text = " ".join([t.name.lower() for t in post.tags]) if post.tags else ""
    cat_text = (post.category.name.lower() if post.category else "").strip()
    content_text = f"{post.excerpt or ''} {post.content or ''}".lower()

    domain_keywords = {
        "git": [
            "git", "github", "gitlab", "commit", "branch", "pull request", "merge", "rebase",
            "git clone", "git push", "git pull", "git stash", "version control", "quản lý phiên bản"
        ],
        "cpp": [
            "c++", "cpp", "lập trình c++", "lập trình c", "c/c++", "dev-c++", "codeblocks",
            "iostream", "cout", "cin", "con trỏ", "pointer", "oop", "hướng đối tượng",
            "g++", "clang", "std::", "vector<", "virtual function"
        ],
        "http": [
            "http", "https", "rest api", "restful", "api rest", "status code", "201 created",
            "200 ok", "giao thức http", "làm chủ http", "http/2", "http/3", "request", "response"
        ],
        "python": ["python", "python 3", "cpython", "async def", "yield", "generator", "decorator"],
        "fastapi": ["fastapi", "pydantic", "sqlalchemy", "uvicorn", "asyncio", "dependency injection"],
        "database": ["postgresql", "postgres", "database", "cơ sở dữ liệu", "csdl", "sql", "index", "connection pooling"],
        "devops": ["docker", "container", "devops", "ci/cd", "github actions", "kubernetes", "k8s", "dockerfile", "docker-compose"],
        "react": ["react", "react 19", "tailwind", "next.js", "nextjs", "jsx", "tsx", "virtual dom", "frontend framework", "useactionstate"],
        "web_basic": [
            "lập trình web", "lập trình website", "website cơ bản", "web cơ bản",
            "html", "html5", "css", "css3", "javascript", "flexbox", "responsive web",
            "dom", "frontend cơ bản", "website"
        ]
    }

    scores = {d: 0 for d in domain_keywords}
    for dom, kws in domain_keywords.items():
        for kw in kws:
            if kw in title_text:
                scores[dom] += 30  # Title is strongest signal
            if kw in tag_text:
                scores[dom] += 12
            if kw in cat_text:
                scores[dom] += 5
            if kw in content_text:
                scores[dom] += 1

    best_domain = max(scores, key=scores.get)
    if scores[best_domain] > 0:
        return best_domain

    return "web_basic"


def generate_questions_from_post_text(post: Post, count: int = 5) -> List[Dict[str, Any]]:
    """
    Trích xuất và tự động tạo câu hỏi trắc nghiệm trực tiếp từ tiêu đề và từng câu văn bản của bài viết.
    Đảm bảo 100% nội dung câu hỏi, đáp án và giải thích bám sát những gì tác giả viết trong bài viết.
    """
    title = (post.title or "").strip()
    content = (post.content or "").strip()
    if not content and not title:
        return []

    # Làm sạch markdown
    clean_content = re.sub(r":::video.*?:::", "", content, flags=re.DOTALL)
    clean_content = re.sub(r"```.*?```", "", clean_content, flags=re.DOTALL)
    clean_content = re.sub(r"\[(.*?)\]\(.*?\)", r"\1", clean_content)
    clean_content = re.sub(r"[*_#`]", "", clean_content)
    clean_content = re.sub(r"\s+", " ", clean_content).strip()

    # Tách bài viết thành các câu độc lập
    raw_sentences = [s.strip() for s in re.split(r"(?<=[.!?\n])\s+", clean_content) if len(s.strip()) >= 15]

    questions: List[Dict[str, Any]] = []
    category_label = title if len(title) <= 25 else (post.category.name if post.category else "Kiến thức bài viết")

    # 1. Trích xuất ngữ nghĩa chuyên sâu theo từng câu văn của bài viết
    for sentence in raw_sentences:
        lower_s = sentence.lower()

        # Dạng: Định dạng file / Đuôi mở rộng (.docx, .pdf, .json, .cpp, .py, .html...)
        ext_match = re.search(r"(\.[a-zA-Z0-9]{2,5})\b", sentence)
        if ext_match and any(w in lower_s for kw in ["dạng", "đuôi", "định dạng", "tệp", "file"] for w in [kw]):
            ext = ext_match.group(1)
            other_exts = [e for e in [".docx", ".pdf", ".exe", ".mp4", ".apk", ".zip", ".html", ".json"] if e != ext]
            random.shuffle(other_exts)
            options = [ext] + other_exts[:3]
            random.shuffle(options)
            questions.append({
                "question": "Theo nội dung bài viết, tệp hoặc tài liệu thường được lưu trữ dưới định dạng tiêu chuẩn nào?",
                "options": options,
                "answer_index": options.index(ext),
                "explanation": f"Trích dẫn từ bài viết: \"{sentence}\"",
                "difficulty": "easy",
                "language": category_label
            })

        # Dạng: Phần mềm / Công cụ chính thức (Microsoft Word, Dev-C++, VS Code, Git, Docker...)
        if "microsoft word" in lower_s:
            options = ["Microsoft Word", "Adobe Photoshop", "AutoCAD 3D", "VLC Media Player"]
            random.shuffle(options)
            questions.append({
                "question": "Bài viết đề cập định dạng này là tiêu chuẩn chính thức cho phần mềm hoặc tài liệu nào?",
                "options": options,
                "answer_index": options.index("Microsoft Word"),
                "explanation": f"Trích dẫn từ bài viết: \"{sentence}\"",
                "difficulty": "easy",
                "language": category_label
            })

        # Dạng: Nền tảng / Môi trường hoạt động (Windows, macOS, web, Linux...)
        if any(kw in lower_s for kw in ["windows", "macos", "web", "linux", "mở và chỉnh sửa", "hệ điều hành"]):
            platform_match = re.search(r"(trên|trong)\s+(windows[^\n.]*|macos[^\n.]*|web[^\n.]*|linux[^\n.]*)", sentence, re.IGNORECASE)
            plat_text = platform_match.group(2).strip() if platform_match else "Windows, macOS và web"
            distractors = [
                "Chỉ chạy được trên môi trường MS-DOS cũ",
                "Chỉ mở được trên thiết bị máy chơi game chuyên dụng",
                "Không thể xem hoặc chỉnh sửa trực tiếp trên trình duyệt web"
            ]
            options = [plat_text] + distractors
            random.shuffle(options)
            questions.append({
                "question": "Theo bài viết, các tệp hoặc phần mềm có thể được mở và chỉnh sửa trên những nền tảng nào?",
                "options": options,
                "answer_index": options.index(plat_text),
                "explanation": f"Trích dẫn từ bài viết: \"{sentence}\"",
                "difficulty": "easy",
                "language": category_label
            })

        # Dạng: Tính năng hỗ trợ / Khả năng ("hỗ trợ văn bản, hình ảnh, bảng...", "có khả năng...")
        if any(kw in lower_s for kw in ["hỗ trợ", "có khả năng", "bao gồm", "chứa"]):
            support_match = re.search(r"(hỗ trợ|có khả năng|bao gồm)\s+([^.]+)", sentence, re.IGNORECASE)
            if support_match:
                feature_text = support_match.group(2).strip().capitalize()
                distractors = [
                    "Chỉ hỗ trợ tệp âm thanh đơn thuần không kèm hình ảnh",
                    "Không hỗ trợ bảng biểu hay các tính năng định dạng",
                    "Chỉ hiển thị dòng lệnh văn bản thô không hỗ trợ cộng tác"
                ]
                options = [feature_text] + distractors
                random.shuffle(options)
                questions.append({
                    "question": "Theo nội dung bài viết, tệp hoặc công cụ này hỗ trợ những thành phần và tính năng nào?",
                    "options": options,
                    "answer_index": options.index(feature_text),
                    "explanation": f"Trích dẫn từ bài viết: \"{sentence}\"",
                    "difficulty": "easy",
                    "language": category_label
                })

        # Dạng: Công cụ trong ngoặc đơn (như Dev-C++, CodeBlocks, VS Code)
        tools_match = re.search(r"\((như|ví dụ:?)\s+([^)]+)\)", sentence)
        if tools_match:
            tools_str = tools_match.group(2).strip()
            distractors = [
                "Microsoft Word và Excel",
                "Adobe Photoshop và Illustrator",
                "Trình duyệt Google Chrome đơn thuần"
            ]
            options = [tools_str] + distractors
            random.shuffle(options)
            questions.append({
                "question": "Theo nội dung bài viết, những công cụ hoặc môi trường nào sau đây được gợi ý sử dụng?",
                "options": options,
                "answer_index": options.index(tools_str),
                "explanation": f"Trích dẫn từ bài viết: \"{sentence}\"",
                "difficulty": "easy",
                "language": category_label
            })

        # Dạng: Khối kiến thức nền tảng (kiến thức cơ bản như biến, kiểu dữ liệu, hàm...)
        knowledge_match = re.search(r"(kiến thức cơ bản|nền tảng|khái niệm cơ bản)\s+(như|bao gồm)\s+([^.]+)", sentence, re.IGNORECASE)
        if knowledge_match:
            k_str = knowledge_match.group(3).strip().capitalize()
            distractors = [
                "Chỉ cần cài đặt hệ điều hành và không cần học cú pháp",
                "Điện toán đám mây và kiến trúc cụm vi dịch vụ phân tán",
                "Thiết kế banner đồ họa và biên tập âm thanh kỹ thuật số"
            ]
            options = [k_str] + distractors
            random.shuffle(options)
            questions.append({
                "question": "Theo bài viết, người học cần nắm vững những kiến thức nền tảng cơ bản nào?",
                "options": options,
                "answer_index": options.index(k_str),
                "explanation": f"Trích dẫn từ bài viết: \"{sentence}\"",
                "difficulty": "easy",
                "language": category_label
            })

        # Dạng: Mục đích sử dụng ("sử dụng để...", "dùng để...")
        use_match = re.search(r"(sử dụng để|dùng để|dùng rộng rãi trong)\s+([^.]+)", sentence, re.IGNORECASE)
        if use_match:
            u_str = use_match.group(2).strip().capitalize()
            distractors = [
                "Nghe nhạc và giải trí trực tuyến",
                "Thiết kế mô hình 3D cho các trò chơi",
                "Tự động tính toán tiền điện nước gia đình"
            ]
            options = [u_str] + distractors
            random.shuffle(options)
            questions.append({
                "question": "Theo nội dung bài viết, mục đích hoặc phạm vi ứng dụng chính là gì?",
                "options": options,
                "answer_index": options.index(u_str),
                "explanation": f"Trích dẫn từ bài viết: \"{sentence}\"",
                "difficulty": "easy",
                "language": category_label
            })

        # Dạng: Định nghĩa cốt lõi ("X là Y")
        def_match = re.search(r"^([^.,]+?)\s+là\s+([^.,]+(?:,[^.,]+){0,2})", sentence)
        if def_match and len(def_match.group(1).split()) <= 4:
            term = def_match.group(1).strip()
            def_text = def_match.group(2).strip()
            distractors = [
                "Một phần mềm chuyên dụng chỉ dùng để nghe nhạc và xem video",
                "Một giao thức mạng cục bộ không có khả năng lưu trữ",
                "Một công cụ đồ họa 3D không liên quan đến bài viết"
            ]
            options = [def_text.capitalize()] + distractors
            random.shuffle(options)
            questions.append({
                "question": f"Theo bài viết, '{term}' được định nghĩa hoặc có vai trò cốt lõi là gì?",
                "options": options,
                "answer_index": options.index(def_text.capitalize()),
                "explanation": f"Trích dẫn từ bài viết: \"{sentence}\"",
                "difficulty": "easy",
                "language": category_label
            })

    # 2. Tạo câu hỏi xác minh tính đúng đắn trực tiếp từ các câu trong bài viết (Fact Verification)
    for sentence in raw_sentences:
        if len(questions) >= count:
            break
        clean_s = sentence.rstrip(". ")
        distractors = [
            f"Không {clean_s[0].lower() + clean_s[1:]}",
            f"{clean_s} (chỉ áp dụng duy nhất trên thiết bị di động cũ)",
            "Nội dung này hoàn toàn không được hỗ trợ trong bài viết"
        ]
        options = [clean_s] + distractors
        random.shuffle(options)
        questions.append({
            "question": "Dựa vào nội dung bài viết, khẳng định nào sau đây là CHÍNH XÁC?",
            "options": options,
            "answer_index": options.index(clean_s),
            "explanation": f"Trích dẫn từ bài viết: \"{sentence}\"",
            "difficulty": "medium",
            "language": category_label
        })

    # 3. Câu hỏi về chủ đề / mục tiêu của bài viết (trích xuất từ Tiêu đề)
    if title:
        distractor_topics = [
            "Cài đặt hệ điều hành Linux Server cho doanh nghiệp",
            "Lập trình vi điều khiển và thiết kế bo mạch phần cứng",
            "Kỹ thuật tối ưu hóa cơ sở dữ liệu quan hệ nâng cao"
        ]
        options = [title] + distractor_topics
        random.shuffle(options)
        questions.insert(0, {
            "question": "Chủ đề chính được hướng dẫn và thảo luận trong bài viết là gì?",
            "options": options,
            "answer_index": options.index(title),
            "explanation": f"Tiêu đề và nội dung trọng tâm của bài viết là: '{title}'.",
            "difficulty": "easy",
            "language": category_label
        })

    # Khử trùng lặp nội dung câu hỏi
    seen_q = set()
    unique_questions = []
    for q in questions:
        key = q["question"].strip()
        if key not in seen_q:
            seen_q.add(key)
            unique_questions.append(q)

    return unique_questions[:count]


def generate_heuristic_questions_for_post(post: Post, count: int = 5) -> List[Dict[str, Any]]:
    """Produce domain-matched, randomized technical quiz questions tailored to the post."""
    bank = build_domain_fallback_bank()
    domain = _classify_post_domain(post)
    pool = bank.get(domain) or bank["web_basic"]

    # Cycle within the same domain to never cross-contaminate questions across different technologies
    selected_raw = pool.copy()
    while len(selected_raw) < count:
        selected_raw.extend(pool)

    chosen = selected_raw[:count]
    return [_shuffle_question_options(q) for q in chosen]


def generate_questions_with_gemini(post: Post, count: int = 5) -> Optional[List[Dict[str, Any]]]:
    """Attempt to generate questions matching the article via Gemini AI."""
    if not gemini_service.keys_pool:
        return None

    cat_name = post.category.name if post.category else "Công nghệ Thông tin"
    tag_str = ", ".join([t.name for t in post.tags]) if post.tags else ""
    content_snippet = (post.content or "").strip()[:4000]

    prompt = (
        f"Bạn là chuyên gia khảo thí và giáo dục CNTT. Dựa trên thông tin bài viết sau, hãy soạn đúng {count} "
        f"câu hỏi trắc nghiệm tiếng Việt chất lượng cao (4 lựa chọn A, B, C, D) kiểm tra trực tiếp kiến thức "
        f"của người học về nội dung bài viết.\n\n"
        f"THÔNG TIN BÀI VIẾT:\n"
        f"- Tiêu đề: {post.title}\n"
        f"- Chuyên mục: {cat_name}\n"
        f"- Thẻ tags: {tag_str}\n"
        f"- Tóm tắt: {post.excerpt or ''}\n"
        f"- Nội dung chính:\n{content_snippet}\n\n"
        f"YÊU CẦU ĐẦU RA:\n"
        f"Chỉ trả về JSON thuần (mảng gồm {count} đối tượng), không dùng code block markdown, mỗi đối tượng gồm:\n"
        f"[\n"
        f"  {{\n"
        f'    "question": "Nội dung câu hỏi rõ ràng, bám sát kiến thức bài viết",\n'
        f'    "options": ["Lựa chọn A", "Lựa chọn B", "Lựa chọn C", "Lựa chọn D"],\n'
        f'    "answer_index": 0,\n'
        f'    "explanation": "Giải thích chi tiết vì sao đáp án này chính xác dựa trên bài viết",\n'
        f'    "difficulty": "easy" | "medium" | "hard",\n'
        f'    "language": "{cat_name}"\n'
        f"  }}\n"
        f"]"
    )

    try:
        raw_res = gemini_service.call_gemini(
            prompt=prompt,
            system_instruction="Bạn là trợ lý khảo thí chuyên gia IT. Hãy trả về JSON hợp lệ theo đúng cấu trúc yêu cầu.",
            json_mode=True,
            temperature=0.3
        )
        if not raw_res:
            return None

        clean_text = raw_res.strip()
        if clean_text.startswith("```json"):
            clean_text = clean_text[7:]
        if clean_text.startswith("```"):
            clean_text = clean_text[3:]
        if clean_text.endswith("```"):
            clean_text = clean_text[:-3]
        clean_text = clean_text.strip()

        data = json.loads(clean_text)
        if isinstance(data, dict) and "questions" in data:
            data = data["questions"]

        if isinstance(data, list):
            valid_list = []
            for item in data:
                if (
                    isinstance(item, dict)
                    and item.get("question")
                    and isinstance(item.get("options"), list)
                    and len(item["options"]) >= 2
                ):
                    valid_list.append(_shuffle_question_options({
                        "question": str(item["question"]).strip(),
                        "options": [str(opt).strip() for opt in item["options"]],
                        "answer_index": int(item.get("answer_index", 0)),
                        "explanation": str(item.get("explanation", "")).strip(),
                        "difficulty": item.get("difficulty", "medium"),
                        "language": str(item.get("language", cat_name or "IT"))
                    }))
            if len(valid_list) >= 2:
                return valid_list[:count]
    except Exception as e:
        logger.warning(f"AI Quiz Generation for Post #{post.id} encountered error: {e}")

    return None


def is_meaningful_post_content(post: Post) -> bool:
    """
    Kiểm tra tiêu đề và nội dung bài viết có ý nghĩa hay không.
    Nếu bài viết là ký tự vô nghĩa (như 'nnnnn', 'kkkk', 'asdfgh', lặp ký tự, nội dung rác),
    trả về False và không tạo trắc nghiệm.
    """
    if not post:
        return False
    title = (post.title or "").strip()
    content = (post.content or "").strip()

    # Tiêu đề không được để trống
    if not title:
        return False

    clean_title = re.sub(r"\s+", "", title).lower()
    # 1. Phát hiện tiêu đề rác lặp lại 1 ký tự duy nhất (ví dụ: 'nnnnn', 'aaaaa', '.....', '11111')
    if len(clean_title) >= 3 and len(set(clean_title)) < 2:
        return False

    # 2. Kiểm tra nội dung
    clean_content = content.replace(":::video", "").replace(":::", "").strip()
    words_content = [w for w in re.split(r"\s+", clean_content) if len(w) > 1]
    unique_words_content = set(w.lower() for w in words_content)

    # Nếu nội dung có ý nghĩa (>= 25 ký tự và ít nhất 4 từ độc lập khác nhau, hoặc có video nhúng)
    if (":::video" in content or len(clean_content) >= 25) and len(unique_words_content) >= 4:
        return True

    # Nếu tiêu đề có ý nghĩa rõ ràng (>= 4 ký tự và ít nhất 3 ký tự khác nhau) và nội dung có ít nhất 15 ký tự
    if len(title) >= 4 and len(set(clean_title)) >= 3 and len(clean_content) >= 15 and len(unique_words_content) >= 3:
        return True

    return False


def ensure_post_quiz_questions(
    db: Session,
    post: Post,
    count: int = 5,
    user_id: Optional[int] = None
) -> List[QuizQuestion]:
    """
    Ensure the given post has relevant quiz questions matching its content.
    If no questions exist for this post in `quiz_question_posts`, generates and persists them.
    If the post is meaningless/gibberish, does NOT create any questions.
    """
    # 0. Kiểm tra bài viết có ý nghĩa thực tế không (tránh sinh đề trắc nghiệm cho bài rác "nnnnn", "kkkk")
    if not is_meaningful_post_content(post):
        return []

    # 1. Check existing questions
    existing = (
        db.query(QuizQuestion)
        .join(QuizQuestionPost, QuizQuestionPost.question_id == QuizQuestion.id)
        .filter(QuizQuestionPost.post_id == post.id)
        .all()
    )
    if len(existing) >= 3:
        return existing

    # 2. Generate questions matching post content
    generated_raw = generate_questions_with_gemini(post, count=count)
    source = "ai"
    if not generated_raw:
        # Ưu tiên cao nhất: Trích xuất trực tiếp câu hỏi & đáp án từ chính tiêu đề và nội dung bài viết
        generated_raw = generate_questions_from_post_text(post, count=count)
        source = "content_extracted"
    if not generated_raw:
        generated_raw = generate_heuristic_questions_for_post(post, count=count)
        source = "heuristic"

    cat_name = post.category.name if post.category else "IT"
    post_domain = _classify_post_domain(post)
    domain_lang = DOMAIN_TO_LANG.get(post_domain)

    # Map category name → canonical quiz language (matches TOPIC_ALIASES in quiz.py)
    CAT_TO_LANG = {
        "Backend": "FastAPI",
        "Frontend": "React",
        "DevOps": "DevOps",
        "Python": "Python",
        "Database": "PostgreSQL",
        "Security": "Security",
        "Architecture": "Architecture",
        "Fullstack": "Fullstack",
        "IT": "IT",
    }

    created_questions: List[QuizQuestion] = []
    for q_data in generated_raw:
        clean_opts = q_data.get("options", [])
        ans_idx = int(q_data.get("answer_index", 0))
        if ans_idx < 0 or ans_idx >= len(clean_opts):
            ans_idx = 0

        # Prefer specific language from question data; fallback to domain-derived language, then category
        raw_lang = (q_data.get("language") or q_data.get("lang") or "").strip()
        if raw_lang and raw_lang not in ["IT", "Backend", "Frontend", "Fullstack", "Khác"]:
            lang_val = raw_lang
        elif domain_lang:
            lang_val = domain_lang
        else:
            lang_val = CAT_TO_LANG.get(cat_name, cat_name or "IT")

        q_obj = QuizQuestion(
            course_id=None,
            language=lang_val,
            question=str(q_data.get("question", "")).strip(),
            options=clean_opts,
            answer_index=ans_idx,
            explanation=str(q_data.get("explanation", "")).strip(),
            difficulty=q_data.get("difficulty", "medium"),
            source=source,
            created_by=user_id or post.author_id
        )
        db.add(q_obj)
        db.flush()

        # Link with post
        link = QuizQuestionPost(question_id=q_obj.id, post_id=post.id)
        db.add(link)
        created_questions.append(q_obj)

    db.commit()
    for q in created_questions:
        db.refresh(q)

    logger.info(f"Generated and linked {len(created_questions)} quiz questions for post #{post.id} ('{post.title}').")
    return created_questions
