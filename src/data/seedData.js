/**
 * Dữ liệu mẫu ban đầu (Seed Data) cho hệ thống IT Blog
 * (Tất cả người dùng đều là thành viên bình đẳng, chưa phân quyền tác nhân)
 */

export const SEED_USERS = [
  {
    id: "demo_admin",
    name: "Quản trị viên Demo (Admin)",
    username: "admin",
    email: "admin@itblog.dev",
    role: "admin",
    is_superuser: true,
    roles: ["admin", "moderator", "user"],
    avatar: "https://api.dicebear.com/7.x/bottts/svg?seed=admin",
    bio: "Quản trị viên tối cao: Toàn quyền cấu hình hệ thống, duyệt bài, cào tin, quản lý thành viên và xuất Markdown (.md)",
    following: ["user_hoang", "user_linh"],
    createdAt: "2026-02-01T08:00:00.000Z"
  },
  {
    id: "demo_moderator",
    name: "Lê Hoàng Nam (Moderator)",
    username: "mod_dev",
    email: "mod@itblog.dev",
    role: "moderator",
    is_superuser: false,
    roles: ["moderator", "user"],
    avatar: "https://api.dicebear.com/7.x/bottts/svg?seed=namle",
    bio: "Kiểm duyệt viên: Kiểm tra bài viết, xử lý báo cáo vi phạm, quản lý chuyên mục (Không có quyền xuất .md)",
    following: ["user_hoang"],
    createdAt: "2026-02-15T08:30:00.000Z"
  },
  {
    id: "demo_user",
    name: "Nguyễn Văn Hoàng (Member)",
    username: "hoang_dev",
    email: "hoang.dev@itblog.vn",
    role: "user",
    is_superuser: false,
    roles: ["user", "author"],
    avatar: "https://api.dicebear.com/7.x/bottts/svg?seed=hoang",
    bio: "Thành viên cộng đồng: Đọc và viết bài, like, bookmark, làm trắc nghiệm (Không vào được Cài đặt & Không xuất .md)",
    following: ["user_linh"],
    createdAt: "2026-01-10T10:00:00.000Z"
  },
  {
    id: "user_linh",
    name: "Linh Nguyễn",
    email: "linh@itblog.local",
    avatar: "https://api.dicebear.com/7.x/bottts/svg?seed=linh",
    bio: "Kỹ sư Trí tuệ nhân tạo | Nghiên cứu LLMs và RAG",
    following: ["user_hoang"],
    createdAt: "2026-01-12T11:00:00.000Z"
  },
  {
    id: "user_nam",
    name: "Nam Trần",
    email: "nam@itblog.local",
    avatar: "https://api.dicebear.com/7.x/bottts/svg?seed=nam",
    bio: "Kỹ sư DevOps & Cloud | Docker, CI/CD Pipelines",
    following: ["user_hoang"],
    createdAt: "2026-01-20T14:00:00.000Z"
  }
];

export const CATEGORIES = [
  "Tất cả",
  "Frontend",
  "Backend",
  "AI & LLM",
  "DevOps",
  "Database",
  "System Design",
  "Career",
  "Ngoài Lề"
];

export const POPULAR_TAGS = [
  "React",
  "Tailwind",
  "FastAPI",
  "Go",
  "Docker",
  "Kubernetes",
  "PostgreSQL",
  "Redis",
  "ngoàilề",
  "goclaptrinh",
  "desksetup",
  "chiaseanh",
  "RAG",
  "LLM",
  "Microservices",
  "TypeScript"
];

export const SEED_POSTS = [
  {
    id: "9",
    title: "Hướng dẫn Thực chiến: python 2026",
    slug: "huong-dan-thuc-chien-python-2026",
    excerpt: "Khám phá các phương pháp tốt nhất và kinh nghiệm thực chiến về python.",
    content: "Khám phá các phương pháp tốt nhất và kinh nghiệm thực chiến về python 2026. Hướng dẫn chi tiết áp dụng Best Practices, tối ưu Performance và thiết kế Architecture bài bản cho dự án thực tế.",
    coverImage: "https://images.unsplash.com/photo-1526379095098-d400fd0bf935?w=1000&auto=format&fit=crop&q=80",
    category: "Frontend",
    tags: ["Architecture", "Performance", "BestPractices"],
    authorId: "1",
    author: {
      id: "1",
      name: "Quản Trị Viên IT Blog",
      username: "admin",
      avatar: "https://api.dicebear.com/7.x/bottts/svg?seed=admin",
      role: "admin"
    },
    likes: [],
    bookmarks: [],
    comments: [],
    views: 0,
    readTime: "4 phút đọc",
    status: "approved",
    createdAt: "2026-10-16T12:37:38.614Z"
  },
  {
    id: "7",
    title: "Làm chủ Lập trình website cơ  bản: Hướng dẫn từ Cơ bản đến Thực chiến 2026",
    slug: "lam-chu-lap-trinh-website-co-ban-huong-dan-tu-co-ban-den-thuc-chien-2026",
    excerpt: "Khám phá phân tích chi tiết về Lập trình website cơ  bản. Hướng dẫn thực chiến, tối ưu hóa và kinh nghiệm lập trình từ cộng đồng IT Blog.",
    content: ":::video youtube:S0SAw6FZeCU\n:::",
    coverImage: "https://images.unsplash.com/photo-1461749280684-dccba630e2f6?w=1000&auto=format&fit=crop&q=80",
    category: "Backend",
    tags: ["Công nghệ", "Kinh nghiệm", "Lập trình"],
    authorId: "1",
    author: {
      id: "1",
      name: "Quản Trị Viên IT Blog",
      username: "admin",
      avatar: "https://api.dicebear.com/7.x/bottts/svg?seed=admin",
      role: "admin"
    },
    likes: [],
    bookmarks: [],
    comments: [],
    views: 69,
    readTime: "1 phút đọc",
    status: "approved",
    createdAt: "2026-10-14T22:40:20.998Z"
  },
  {
    id: "3",
    title: "Tự Động Hóa Triển Khai Ứng Dụng Bằng Docker Và GitHub Actions",
    slug: "tu-dong-hoa-trien-khai-ung-dung-bang-docker-va-github-actions",
    excerpt: "Thiết lập quy trình CI/CD tự động kiểm thử, build Docker image và đẩy lên máy chủ sản xuất với GitHub Actions và Docker Compose.",
    content: "# Tự Động Hóa Triển Khai Ứng Dụng Bằng Docker Và GitHub Actions\n\nQuy trình CI/CD giúp đội ngũ phát triển phát hiện lỗi sớm và bàn giao tính năng mới đến tay người dùng một cách an toàn và tự động.",
    coverImage: "https://images.unsplash.com/photo-1607799279861-4dd421887fb3?w=1000&auto=format&fit=crop&q=80",
    category: "DevOps",
    tags: ["Docker", "Git", "Linux"],
    authorId: "2",
    author: {
      id: "2",
      name: "Nguyễn Văn Hoàng",
      username: "hoang_dev",
      avatar: "https://api.dicebear.com/7.x/bottts/svg?seed=hoang",
      role: "user"
    },
    likes: [],
    bookmarks: [],
    comments: [],
    views: 755,
    readTime: "6 phút đọc",
    status: "approved",
    createdAt: "2026-10-14T14:11:29.363Z"
  },
  {
    id: "2",
    title: "Tối Ưu Hóa Giao Diện Người Dùng Với React 19 Và Tailwind CSS v4",
    slug: "toi-uu-hoa-giao-dien-nguoi-dung-voi-react-19-va-tailwind-css-v4",
    excerpt: "Khám phá các kỹ thuật hiện đại để xây dựng giao diện người dùng mượt mà, chuẩn responsive trên mọi thiết bị với React và Tailwind CSS.",
    content: "# Tối Ưu Hóa Giao Diện Người Dùng Với React 19 Và Tailwind CSS v4\n\nXây dựng trải nghiệm người dùng trực quan, tốc độ tải trang nhanh và khả năng tương thích hoàn hảo.",
    coverImage: "https://images.unsplash.com/photo-1633356122544-f134324a6cee?w=1000&auto=format&fit=crop&q=80",
    category: "Frontend",
    tags: ["JavaScript", "Next.js", "React", "TailwindCSS"],
    authorId: "1",
    author: {
      id: "1",
      name: "Quản Trị Viên IT Blog",
      username: "admin",
      avatar: "https://api.dicebear.com/7.x/bottts/svg?seed=admin",
      role: "admin"
    },
    likes: [],
    bookmarks: [],
    comments: [],
    views: 981,
    readTime: "5 phút đọc",
    status: "approved",
    createdAt: "2026-10-14T14:11:29.363Z"
  },
  {
    id: "1",
    title: "Xây Dựng Hệ Thống Kiến Trúc Microservices Với FastAPI Và PostgreSQL",
    slug: "xay-dung-he-thong-kien-truc-microservices-voi-fastapi-va-postgresql",
    excerpt: "Hướng dẫn chi tiết từ A-Z cách thiết kế, xây dựng và tối ưu hóa hệ thống backend microservices chịu tải cao bằng FastAPI, PostgreSQL và Docker.",
    content: "# Xây Dựng Hệ Thống Kiến Trúc Microservices Với FastAPI Và PostgreSQL\n\nThiết kế hệ thống phân tán hiệu năng cao với FastAPI và PostgreSQL.",
    coverImage: "https://images.unsplash.com/photo-1558494949-ef010cbdcc31?w=1000&auto=format&fit=crop&q=80",
    category: "Backend",
    tags: ["FastAPI", "Microservices", "PostgreSQL", "Python"],
    authorId: "2",
    author: {
      id: "2",
      name: "Nguyễn Văn Hoàng",
      username: "hoang_dev",
      avatar: "https://api.dicebear.com/7.x/bottts/svg?seed=hoang",
      role: "user"
    },
    likes: [],
    bookmarks: [],
    comments: [],
    views: 1420,
    readTime: "8 phút đọc",
    status: "approved",
    createdAt: "2026-10-14T14:11:29.363Z"
  },
  {
    id: "991",
    title: "Góc Làm Việc Minimalist 2026: Chia Sẻ Bàn Nâng Hạ, Màn Hình Ultrawide & Đèn BenQ",
    slug: "goc-lam-viec-minimalist-2026-chia-se-ban-nang-ha-man-hinh-ultrawide",
    excerpt: "Bộ ảnh góc làm việc tại nhà (Work from Home) của một lập trình viên Fullstack. Cùng ngắm setup ánh sáng, bàn phím cơ và các phụ kiện công nghệ yêu thích.",
    content: "# 📸 Góc Làm Việc Minimalist 2026 Của Fullstack Dev\n\nChào anh em IT Blog! Hôm nay nhân ngày cuối tuần dọn dẹp lại không gian làm việc sau chuỗi ngày cày sprint căng thẳng, mình chụp vài tấm ảnh chia sẻ cùng mọi người theo phong cách Flickr album.\n\n![Setup Bàn Làm Việc](https://images.unsplash.com/photo-1518455027359-f3f8164ba6bd?w=1000&auto=format&fit=crop&q=80)\n\n### 🖥️ Danh sách thiết bị cấu hình:\n- **Bàn làm việc:** Bàn nâng hạ Epione SmartDesk Pro mặt gỗ sồi tự nhiên.\n- **Màn hình:** LG 34WN80C Ultrawide 34 inch IPS chuẩn màu cho code & xem preview.\n- **Bàn phím cơ:** Keychron Q1 Pro mod gateron oil kings gõ cực đầm và êm tai.\n- **Ánh sáng:** Đèn treo màn hình BenQ ScreenBar Halo chống mỏi mắt ban đêm.\n\n![Bàn phím cơ và cốc cafe](https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=1000&auto=format&fit=crop&q=80)\n\nAnh em cùng chia sẻ góc làm việc hoặc phụ kiện công nghệ tâm đắc của mình ở phần bình luận bên dưới nhé!",
    coverImage: "https://images.unsplash.com/photo-1518455027359-f3f8164ba6bd?w=1000&auto=format&fit=crop&q=80",
    category: "Ngoài Lề",
    tags: ["ngoàilề", "goclaptrinh", "desksetup", "chiaseanh"],
    authorId: "2",
    author: {
      id: "2",
      name: "Nguyễn Văn Hoàng",
      username: "hoang_dev",
      avatar: "https://api.dicebear.com/7.x/bottts/svg?seed=hoang",
      role: "user"
    },
    likes: [],
    bookmarks: [],
    comments: [],
    views: 856,
    readTime: "3 phút đọc",
    status: "approved",
    createdAt: "2026-11-01T10:00:00.000Z"
  },
  {
    id: "992",
    title: "Chùm Ảnh: 1 Ngày Đi Cafe Code & Săn Bình Minh Cùng MacBook M3 Pro",
    slug: "chum-anh-1-ngay-di-cafe-code-va-san-binh-minh",
    excerpt: "Những khoảnh khắc đời sống bình yên ngoài dòng code: Tách cà phê sáng, thành phố thức giấc và vài trang sách công nghệ truyền cảm hứng.",
    content: "# ☕ Chùm Ảnh: Một Ngày Rời Xa Văn Phòng\n\nĐôi khi bug không thể fix được bằng cách ngồi dán mắt vào màn hình 8 tiếng liên tục, mà cần một chuyến đi dạo, một tách latte nóng và chút khí trời ban mai.\n\n![Cà phê và Laptop](https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=1000&auto=format&fit=crop&q=80)\n\n> \"Lập trình là nghệ thuật sáng tạo, và người nghệ sĩ cần những khoảng lặng để tái tạo nguồn năng lượng!\"\n\n![Khoảnh khắc bình minh](https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=1000&auto=format&fit=crop&q=80)",
    coverImage: "https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=1000&auto=format&fit=crop&q=80",
    category: "Ngoài Lề",
    tags: ["ngoàilề", "chiaseanh", "devlife"],
    authorId: "1",
    author: {
      id: "1",
      name: "Quản Trị Viên IT Blog",
      username: "admin",
      avatar: "https://api.dicebear.com/7.x/bottts/svg?seed=admin",
      role: "admin"
    },
    likes: [],
    bookmarks: [],
    comments: [],
    views: 620,
    readTime: "2 phút đọc",
    status: "approved",
    createdAt: "2026-11-05T08:30:00.000Z"
  }
];
