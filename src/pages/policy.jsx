import { useState, useEffect } from "react";
import { api } from "../services/api";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../context/ToastContext";
import { useBlog } from "../context/BlogContext";
import { getStoredBugReports, addStoredBugReport } from "../utils/bugReportsStore";

// const FEEDBACK_STORAGE_KEY = "itblog_user_feedback_history";

export default function PolicyPage({ onNavigate, initialSection = "terms" }) {
  const [activeSection, setActiveSection] = useState(initialSection);
  const { currentUser } = useAuth();
  const { addToast } = useToast();
  const { setSelectedCategory } = useBlog();

  const [feedbackType, setFeedbackType] = useState("bug");
  const [feedbackPriority, setFeedbackPriority] = useState("medium");
  const [feedbackName, setFeedbackName] = useState(currentUser?.name || "");
  const [feedbackEmail, setFeedbackEmail] = useState(currentUser?.email || "");
  const [feedbackSubject, setFeedbackSubject] = useState("");
  const [feedbackContent, setFeedbackContent] = useState("");
  const [feedbackSubmitting, setFeedbackSubmitting] = useState(false);
  const [feedbackSubTab, setFeedbackSubTab] = useState("form");
  const [bugFilterStatus, setBugFilterStatus] = useState("all");
  const [bugFilterCategory, setBugFilterCategory] = useState("all");
  const [bugSearch, setBugSearch] = useState("");

  const [bugTrackerList, setBugTrackerList] = useState(() => getStoredBugReports());

  useEffect(() => {
    api.bugReports.list()
      .then((data) => {
        if (Array.isArray(data) && data.length > 0) {
          setBugTrackerList(data);
        }
      })
      .catch(() => {});
  }, []);

  const handleFeedbackSubmit = async (e) => {
    e.preventDefault();
    if (!feedbackSubject.trim() || !feedbackContent.trim()) {
      addToast("Vui lòng nhập đầy đủ tiêu đề và nội dung phản hồi!", "warning");
      return;
    }
    if (!feedbackEmail.trim()) {
      addToast("Vui lòng cung cấp email liên hệ để Ban Quản trị phản hồi!", "warning");
      return;
    }

    setFeedbackSubmitting(true);
    const newBug = {
      category: feedbackType,
      priority: feedbackPriority,
      title: feedbackSubject.trim(),
      description: feedbackContent.trim(),
      reporter_name: feedbackName || currentUser?.name || "Thành viên",
      reporter_email: feedbackEmail.trim()
    };

    try {
      await api.bugReports.create(newBug);
    } catch {
      // offline fallback
    }

    const updated = addStoredBugReport(newBug);
    setBugTrackerList(updated);
    setFeedbackSubject("");
    setFeedbackContent("");
    setFeedbackSubmitting(false);
    setFeedbackSubTab("tracker");
    addToast("Báo lỗi của bạn đã được tiếp nhận thành công và đưa vào Bảng theo dõi sự cố! 🚀", "success");
  };

  const copyRssUrl = () => {
    const rssUrl = `${window.location.origin}/api/v1/feeds/rss`;
    navigator.clipboard.writeText(rssUrl).then(() => {
      addToast("Đã sao chép đường dẫn RSS Feed vào Clipboard! 📋", "success");
    }).catch(() => {
      addToast(`Đường dẫn RSS: ${rssUrl}`, "info");
    });
  };

  const navigateToCategory = (catName) => {
    if (setSelectedCategory) setSelectedCategory(catName);
    if (onNavigate) onNavigate("home");
    setTimeout(() => {
      const el = document.getElementById("posts-container");
      if (el) el.scrollIntoView({ behavior: "smooth" });
    }, 150);
  };

  return (
    <div className="w-full max-w-[1720px] mx-auto px-4 sm:px-6 lg:px-8 xl:px-10 py-8 animate-fade-in">
      {/* Top Breadcrumb & Actions */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6 pb-4 border-b border-base-200">
        <div>
          <div className="flex items-center gap-2 text-xs text-base-content/60 mb-1.5">
            <button
              onClick={() => onNavigate && onNavigate("home")}
              className="hover:text-primary transition-colors flex items-center gap-1 font-semibold cursor-pointer"
            >
              <span>←</span> Về trang chủ
            </button>
            <span>•</span>
            <span className="text-primary font-bold uppercase tracking-wider">
              Trung tâm pháp lý & Hỗ trợ
            </span>
          </div>
          <h1 className="text-2xl sm:text-3xl font-black text-base-content tracking-tight">
            Chính Sách, Quy Định & Hỗ Trợ Kỹ Thuật
          </h1>
          <p className="text-xs sm:text-sm text-base-content/70 mt-1">
            Minh bạch về quyền lợi, nghĩa vụ thành viên, quy chuẩn biên tập và kênh hỗ trợ chính thức của cộng đồng IT Blog.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={() => window.print()}
            className="btn btn-sm btn-outline border-base-300 hover:bg-base-200 text-xs font-bold gap-1.5 shadow-2xs cursor-pointer"
            title="In hoặc lưu trang dưới định dạng PDF"
          >
            <span>🖨️</span> In trang
          </button>
          <button
            type="button"
            onClick={() => setActiveSection("feedback")}
            className="btn btn-sm btn-primary text-white font-bold text-xs gap-1.5 shadow-xs cursor-pointer"
          >
            <span>📨</span> Gửi phản hồi
          </button>
        </div>
      </div>

      {/* Navigation Pills Bar */}
      <div className="flex flex-wrap items-center gap-1.5 sm:gap-2 p-1.5 sm:p-2 bg-base-200/90 backdrop-blur-xs rounded-2xl mb-8 border border-base-300 shadow-2xs w-full">
        <button
          type="button"
          onClick={() => setActiveSection("terms")}
          className={`inline-flex items-center gap-1.5 px-3.5 py-2 rounded-xl text-xs sm:text-sm font-bold transition-all shrink-0 cursor-pointer ${
            activeSection === "terms"
              ? "bg-primary text-white shadow-xs font-black ring-1 ring-primary/30"
              : "bg-base-100/70 hover:bg-base-100 text-base-content/75 hover:text-base-content border border-base-200/60"
          }`}
        >
          <span>📜</span>
          <span>Điều khoản sử dụng</span>
        </button>

        <button
          type="button"
          onClick={() => setActiveSection("privacy")}
          className={`inline-flex items-center gap-1.5 px-3.5 py-2 rounded-xl text-xs sm:text-sm font-bold transition-all shrink-0 cursor-pointer ${
            activeSection === "privacy"
              ? "bg-primary text-white shadow-xs font-black ring-1 ring-primary/30"
              : "bg-base-100/70 hover:bg-base-100 text-base-content/75 hover:text-base-content border border-base-200/60"
          }`}
        >
          <span>🔒</span>
          <span>Chính sách bảo mật</span>
        </button>

        <button
          type="button"
          onClick={() => setActiveSection("guidelines")}
          className={`inline-flex items-center gap-1.5 px-3.5 py-2 rounded-xl text-xs sm:text-sm font-bold transition-all shrink-0 cursor-pointer ${
            activeSection === "guidelines"
              ? "bg-primary text-white shadow-xs font-black ring-1 ring-primary/30"
              : "bg-base-100/70 hover:bg-base-100 text-base-content/75 hover:text-base-content border border-base-200/60"
          }`}
        >
          <span>✍️</span>
          <span>Quy chuẩn đăng bài</span>
        </button>

        <button
          type="button"
          onClick={() => setActiveSection("license")}
          className={`inline-flex items-center gap-1.5 px-3.5 py-2 rounded-xl text-xs sm:text-sm font-bold transition-all shrink-0 cursor-pointer ${
            activeSection === "license"
              ? "bg-primary text-white shadow-xs font-black ring-1 ring-primary/30"
              : "bg-base-100/70 hover:bg-base-100 text-base-content/75 hover:text-base-content border border-base-200/60"
          }`}
        >
          <span>⚖️</span>
          <span>Bản quyền & Giấy phép</span>
        </button>

        <button
          type="button"
          onClick={() => setActiveSection("feedback")}
          className={`inline-flex items-center gap-1.5 px-3.5 py-2 rounded-xl text-xs sm:text-sm font-bold transition-all shrink-0 cursor-pointer ${
            activeSection === "feedback"
              ? "bg-primary text-white shadow-xs font-black ring-1 ring-primary/30"
              : "bg-base-100/70 hover:bg-base-100 text-base-content/75 hover:text-base-content border border-base-200/60"
          }`}
        >
          <span>📨</span>
          <span>Gửi phản hồi / Báo lỗi</span>
          {bugTrackerList.length > 0 && (
            <span className="badge badge-xs badge-info text-white font-bold ml-0.5">
              {bugTrackerList.length}
            </span>
          )}
        </button>

        <button
          type="button"
          onClick={() => setActiveSection("sitemap")}
          className={`inline-flex items-center gap-1.5 px-3.5 py-2 rounded-xl text-xs sm:text-sm font-bold transition-all shrink-0 cursor-pointer ${
            activeSection === "sitemap"
              ? "bg-primary text-white shadow-xs font-black ring-1 ring-primary/30"
              : "bg-base-100/70 hover:bg-base-100 text-base-content/75 hover:text-base-content border border-base-200/60"
          }`}
        >
          <span>🗺️</span>
          <span>Sơ đồ trang (Sitemap)</span>
        </button>

        <button
          type="button"
          onClick={() => setActiveSection("rss")}
          className={`inline-flex items-center gap-1.5 px-3.5 py-2 rounded-xl text-xs sm:text-sm font-bold transition-all shrink-0 cursor-pointer ${
            activeSection === "rss"
              ? "bg-primary text-white shadow-xs font-black ring-1 ring-primary/30"
              : "bg-base-100/70 hover:bg-base-100 text-base-content/75 hover:text-base-content border border-base-200/60"
          }`}
        >
          <span>📰</span>
          <span>RSS Feeds</span>
        </button>
      </div>

      {/* Main Content Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
        <div className="lg:col-span-3">
          {/* SECTION 1: ĐIỀU KHOẢN SỬ DỤNG */}
          {activeSection === "terms" && (
            <div className="p-6 sm:p-10 bg-base-100 rounded-3xl border border-base-300 shadow-sm space-y-8 text-sm text-base-content/85 leading-relaxed animate-fade-in">
              <div className="border-b border-base-200 pb-5">
                <span className="badge badge-primary text-white font-bold text-xs mb-2">Quy định pháp lý & Hoạt động</span>
                <h2 className="text-xl sm:text-2xl font-black text-base-content">
                  Điều Khoản Dịch Vụ & Quy Chế Hoạt Động IT Blog
                </h2>
                <p className="text-xs text-base-content/60 mt-1">
                  Phiên bản: 2.1 • Cập nhật lần cuối: 15/11/2026 • Ban Quản trị Nền tảng IT Blog
                </p>
              </div>

              {/* Điều 1 */}
              <div className="space-y-3">
                <h3 className="text-base font-bold text-base-content flex items-center gap-2">
                  <span className="text-primary font-mono font-black">Điều 1.</span>
                  Chấp Thuận Các Điều Khoản & Thỏa Thuận Pháp Lý
                </h3>
                <p>
                  1.1. Điều Khoản Dịch Vụ này ("Điều Khoản") cấu thành một thỏa thuận pháp lý ràng buộc giữa cá nhân bạn ("Người dùng" hoặc "Bạn") và Ban Quản trị Nền tảng IT Blog ("Chúng tôi" hoặc "IT Blog") liên quan đến việc truy cập và sử dụng website IT Blog, bao gồm toàn bộ các tính năng, giao diện, API, công cụ học tập và cơ sở dữ liệu liên quan (gọi chung là "Nền tảng").
                </p>
                <p>
                  1.2. Bạn xác nhận rằng việc tạo tài khoản, gửi bài viết, bình luận hoặc chủ động đánh dấu xác nhận đồng ý trên giao diện đồng nghĩa với việc bạn đã đọc kỹ, hiểu rõ và cam kết tuân thủ toàn bộ các điều khoản tại văn bản này cùng <strong>Chính Sách Bảo Mật Quyền Riêng Tư</strong> của chúng tôi.
                </p>
                <p>
                  1.3. Theo quy định pháp luật Việt Nam, sự im lặng hoặc không phản hồi không mặc nhiên được coi là sự đồng ý. Nếu bạn không đồng ý với bất kỳ phần nào của các Điều Khoản này, bạn có quyền ngừng sử dụng các tính năng tương tác của Nền tảng.
                </p>
              </div>

              {/* Điều 2 */}
              <div className="space-y-3">
                <h3 className="text-base font-bold text-base-content flex items-center gap-2">
                  <span className="text-primary font-mono font-black">Điều 2.</span>
                  Điều Kiện Độ Tuổi & Năng Lực Hành Vi Dân Sự
                </h3>
                <p>
                  2.1. Nền tảng được thiết kế và vận hành dành cho người dùng từ đủ 15 tuổi trở lên có đầy đủ năng lực hành vi dân sự để xác lập các giao dịch và tương tác điện tử theo quy định của pháp luật Việt Nam.
                </p>
                <p>
                  2.2. Trường hợp người chưa thành niên sử dụng Nền tảng, bạn xác nhận rằng bạn đã có sự đồng ý hoặc dưới sự giám sát hợp pháp của cha mẹ hoặc người giám hộ theo quy định pháp luật.
                </p>
              </div>

              {/* Điều 3 */}
              <div className="space-y-3">
                <h3 className="text-base font-bold text-base-content flex items-center gap-2">
                  <span className="text-primary font-mono font-black">Điều 3.</span>
                  Đăng Ký, Xác Thực & Bảo Mật Tài Khoản
                </h3>
                <p>
                  3.1. <strong>Tính chính xác của thông tin</strong>: Bạn cam kết cung cấp thông tin trung thực, chính xác và cập nhật khi đăng ký tài khoản (họ tên, email, tên người dùng).
                </p>
                <p>
                  3.2. <strong>Bảo mật mật khẩu</strong>: Bạn có trách nhiệm tự bảo mật mật khẩu của mình và chịu trách nhiệm pháp lý đối với mọi hoạt động diễn ra dưới tài khoản cá nhân. Khi phát hiện tài khoản bị xâm nhập trái phép, bạn cần thông báo ngay cho Ban Quản trị qua email <code className="text-primary font-mono">support@itblog.local</code>.
                </p>
                <p>
                  3.3. <strong>Quy định về tên tài khoản</strong>: Ban Quản trị bảo lưu quyền từ chối, tạm dừng hoặc thu hồi các tên người dùng (username) vi phạm thuần phong mỹ tục, giả mạo danh tính cá nhân/tổ chức khác, hoặc xâm phạm nhãn hiệu đã được bảo hộ.
                </p>
              </div>

              {/* Điều 4 */}
              <div className="space-y-3">
                <h3 className="text-base font-bold text-base-content flex items-center gap-2">
                  <span className="text-primary font-mono font-black">Điều 4.</span>
                  Quyền Sở Hữu Trí Tuệ & Giấy Phép Chia Sẻ Tri Thức
                </h3>
                <p>
                  4.1. <strong>Tài sản của IT Blog</strong>: Giao diện thiết kế, mã nguồn xây dựng website, biểu tượng nhận diện, cấu trúc cơ sở dữ liệu và thuật toán gợi ý của Nền tảng thuộc quyền sở hữu trí tuệ hợp pháp của IT Blog và được bảo hộ theo Luật Sở hữu trí tuệ Việt Nam.
                </p>
                <p>
                  4.2. <strong>Bản quyền bài viết do Người dùng đóng góp</strong>:
                </p>
                <ul className="list-disc list-inside space-y-1.5 pl-2 text-xs sm:text-sm">
                  <li>Tác giả giữ toàn bộ quyền sở hữu trí tuệ và quyền nhân thân bất khả chuyển nhượng đối với bài viết, tài liệu kỹ thuật và đồ án do chính mình sáng tạo và đăng tải.</li>
                  <li>Trừ khi có thỏa thuận riêng, các bài viết chuyên môn trên IT Blog được phát hành theo giấy phép mở <strong>Creative Commons Ghi công - Phi thương mại - Chia sẻ tương tự (CC BY-NC-SA 4.0)</strong>. Độc giả được quyền trích dẫn, chia sẻ và phát triển nội dung với điều kiện ghi rõ nguồn tác giả và không sử dụng vào mục đích thương mại độc quyền.</li>
                  <li>Các đoạn mã nguồn mẫu (code snippets) chia sẻ trong bài viết được mặc định áp dụng giấy phép <strong>Mã nguồn mở MIT</strong>, cho phép cộng đồng lập trình viên tự do tham khảo, tích hợp và tái sử dụng trong các sản phẩm phần mềm.</li>
                </ul>
                <p>
                  4.3. <strong>Quyền cấp phép vận hành</strong>: Bằng việc đăng tải nội dung, bạn cấp cho IT Blog quyền phi độc quyền, miễn phí bản quyền để lưu trữ, lập chỉ mục tìm kiếm (SEO), định dạng hiển thị, phân phối qua RSS Feed và phục vụ chức năng tóm tắt/đọc bài trên hệ thống.
                </p>
              </div>

              {/* Điều 5 */}
              <div className="space-y-3">
                <h3 className="text-base font-bold text-base-content flex items-center gap-2">
                  <span className="text-primary font-mono font-black">Điều 5.</span>
                  Tiêu Chuẩn Cộng Đồng & Các Hành Vi Nghiêm Cấm
                </h3>
                <p>Theo Luật An ninh mạng và các quy định pháp luật liên quan của Việt Nam, thành viên tuyệt đối <strong>KHÔNG</strong> thực hiện các hành vi sau:</p>
                <div className="p-4 rounded-2xl bg-base-200/70 border border-base-300 space-y-2 text-xs">
                  <p>• <strong>Phát tán thông tin độc hại:</strong> Nghiêm cấm đăng tải nội dung chống phá Nhà nước, kích động bạo lực, xúc phạm danh dự nhân phẩm, xuyên tạc lịch sử hoặc vi phạm thuần phong mỹ tục.</p>
                  <p>• <strong>Xâm phạm an ninh mạng:</strong> Nghiêm cấm phát tán mã độc (virus, trojan, spyware), khai thác lỗ hổng bảo mật, can thiệp vào máy chủ hoặc thực hiện các cuộc tấn công từ chối dịch vụ (DDoS).</p>
                  <p>• <strong>Thu thập dữ liệu trái phép:</strong> Không sử dụng bot, crawler tự động để trích xuất trái phép dữ liệu người dùng hoặc cơ sở dữ liệu bài viết mà không có sự đồng ý bằng văn bản.</p>
                  <p>• <strong>Đạo văn & Vi phạm bản quyền:</strong> Nghiêm cấm sao chép nguyên văn công trình nghiên cứu, bài dịch kỹ thuật hoặc phần mềm có bản quyền mà không có sự cho phép của chủ sở hữu.</p>
                  <p>• <strong>Spam & Quảng cáo rác:</strong> Nghiêm cấm rải liên kết cờ bạc, đa cấp, lừa đảo (phishing) hoặc quảng cáo thương mại không liên quan vào bài viết và phần bình luận.</p>
                </div>
              </div>

              {/* Điều 6 */}
              <div className="space-y-3">
                <h3 className="text-base font-bold text-base-content flex items-center gap-2">
                  <span className="text-primary font-mono font-black">Điều 6.</span>
                  Quy Định Sử Dụng Tính Năng Trí Tuệ Nhân Tạo (AI)
                </h3>
                <p>
                  6.1. <strong>Minh bạch tương tác AI</strong>: Nền tảng tích hợp Trợ lý Trí tuệ nhân tạo (Google Gemini AI) để hỗ trợ tóm tắt bài viết, giải thích mã nguồn và tạo giọng đọc âm thanh. Bạn hiểu rõ và đồng ý rằng bạn đang tương tác với hệ thống AI tự động theo quy định của Luật Trí tuệ nhân tạo.
                </p>
                <p>
                  6.2. <strong>Giới hạn trách nhiệm AI</strong>: Kết quả do AI cung cấp (giải thích mã, đề xuất refactor, câu trả lời kỹ thuật) chỉ mang tính chất tham khảo học thuật. Bạn tự chịu trách nhiệm kiểm thử, đánh giá tính an toàn trước khi áp dụng vào các hệ thống phần mềm thực tế hoặc môi trường sản xuất (Production).
                </p>
                <p>
                  6.3. <strong>Bảo mật bí mật công nghệ</strong>: Người dùng cam kết không đưa các bí mật kinh doanh, mã khóa truy cập bảo mật (API Keys, Token bí mật) hoặc dữ liệu nhạy cảm của tổ chức vào khung hội thoại AI.
                </p>
              </div>

              {/* Điều 7 */}
              <div className="space-y-3">
                <h3 className="text-base font-bold text-base-content flex items-center gap-2">
                  <span className="text-primary font-mono font-black">Điều 7.</span>
                  Tài Trợ & Cơ Hội Dành Cho Lập Trình Viên (Developer Promotions)
                </h3>
                <p>
                  7.1. IT Blog hiển thị các mẩu tin tài trợ phù hợp với ngữ cảnh chuyên môn (như mã ưu đãi Cloud, giấy phép IDE, công cụ DevOps) qua thẻ quảng cáo trên trang.
                </p>
                <p>
                  7.2. Các liên kết tài trợ có thể chuyển hướng bạn sang website của bên thứ ba. IT Blog chỉ cung cấp không gian giới thiệu kỹ thuật và không chịu trách nhiệm đối với bất kỳ cam kết thương mại, chính sách thanh toán hoặc tranh chấp phát sinh giữa bạn và bên thứ ba đó.
                </p>
              </div>

              {/* Điều 8 */}
              <div className="space-y-3">
                <h3 className="text-base font-bold text-base-content flex items-center gap-2">
                  <span className="text-primary font-mono font-black">Điều 8.</span>
                  Quy Trình Tiếp Nhận & Xử Lý Vi Phạm Bản Quyền (Notice & Take-down)
                </h3>
                <p>Chúng tôi tôn trọng quyền sở hữu trí tuệ của cộng đồng và tuân thủ quy trình xử lý vi phạm bản quyền:</p>
                <ol className="list-decimal list-inside space-y-1.5 pl-2 text-xs sm:text-sm">
                  <li><strong>Gửi thông báo vi phạm:</strong> Nếu bạn phát hiện nội dung trên Nền tảng xâm phạm quyền tác giả của bạn, vui lòng gửi thông báo kèm bằng chứng sở hữu về email <code className="text-primary font-mono">support@itblog.local</code>.</li>
                  <li><strong>Xác minh và tạm ẩn:</strong> Trong vòng 24 - 48 giờ kể từ khi nhận đủ bằng chứng hợp lệ, Ban Quản trị sẽ tạm thời ẩn nội dung bị khiếu nại và thông báo cho tác giả đăng bài để giải trình.</li>
                  <li><strong>Khôi phục hoặc xóa bỏ vĩnh viễn:</strong> Nếu người đăng bài chứng minh được quyền sử dụng hợp pháp, nội dung có thể được phục hồi; trường hợp xác định vi phạm thực tế, bài viết sẽ bị xóa vĩnh viễn và tài khoản vi phạm có thể bị xử lý kỷ luật.</li>
                </ol>
              </div>

              {/* Điều 9 */}
              <div className="space-y-3">
                <h3 className="text-base font-bold text-base-content flex items-center gap-2">
                  <span className="text-primary font-mono font-black">Điều 9.</span>
                  Đình Chỉ & Chấm Dứt Quyền Sử Dụng Dịch Vụ
                </h3>
                <p>
                  9.1. <strong>Quyền của Bạn</strong>: Bạn có quyền ngừng sử dụng dịch vụ và yêu cầu xóa tài khoản cá nhân bất kỳ lúc nào thông qua trang Cài đặt hồ sơ hoặc gửi yêu cầu tới Ban Quản trị.
                </p>
                <p>
                  9.2. <strong>Quyền của IT Blog</strong>: Chúng tôi có quyền khóa tạm thời hoặc xóa vĩnh viễn tài khoản của bạn mà không cần bồi thường nếu bạn vi phạm nghiêm trọng Tiêu chuẩn cộng đồng, tài khoản bị xâm nhập gây nguy hại hệ thống, hoặc theo yêu cầu bằng văn bản từ cơ quan nhà nước có thẩm quyền tại Việt Nam.
                </p>
              </div>

              {/* Điều 10 */}
              <div className="space-y-3">
                <h3 className="text-base font-bold text-base-content flex items-center gap-2">
                  <span className="text-primary font-mono font-black">Điều 10.</span>
                  Tuyên Bố Miễn Trừ Trách Nhiệm Kỹ Thuật
                </h3>
                <p>
                  10.1. Nền tảng được cung cấp trên nguyên tắc <strong>"NGUYÊN TRẠNG" (AS-IS)</strong> và <strong>"TÙY THEO TÌNH TRẠNG SẴN CÓ" (AS-AVAILABLE)</strong>.
                </p>
                <p>
                  10.2. Trong phạm vi tối đa được pháp luật cho phép, IT Blog không cam kết rằng Nền tảng sẽ luôn vận hành không gián đoạn, hoàn toàn không có lỗi kỹ thuật hoặc tương thích tuyệt đối với mọi thiết bị.
                </p>
                <p>
                  10.3. Chúng tôi không chịu trách nhiệm đối với bất kỳ thiệt hại gián tiếp nào (như mất mát dữ liệu phần mềm cá nhân, đình trệ dự án lập trình) phát sinh từ việc sử dụng hoặc không thể sử dụng các đoạn code mẫu, giải pháp kỹ thuật do cộng đồng chia sẻ trên Nền tảng.
                </p>
              </div>

              {/* Điều 11 */}
              <div className="space-y-3">
                <h3 className="text-base font-bold text-base-content flex items-center gap-2">
                  <span className="text-primary font-mono font-black">Điều 11.</span>
                  Luật Điều Chỉnh & Giải Quyết Tranh Chấp
                </h3>
                <p>
                  11.1. Các Điều Khoản này được điều chỉnh, diễn giải và áp dụng theo toàn bộ <strong>Pháp luật của nước Cộng hòa Xã hội Chủ nghĩa Việt Nam</strong>.
                </p>
                <p>
                  11.2. Mọi tranh chấp phát sinh từ hoặc liên quan đến thỏa thuận này trước hết sẽ được ưu tiên giải quyết thông qua thương lượng, hòa giải thiện chí. Trường hợp thương lượng không thành trong vòng ba mươi (30) ngày, tranh chấp sẽ được đưa ra giải quyết tại <strong>Tòa án nhân dân có thẩm quyền tại Việt Nam</strong>.
                </p>
              </div>

              {/* Điều 12 */}
              <div className="space-y-3">
                <h3 className="text-base font-bold text-base-content flex items-center gap-2">
                  <span className="text-primary font-mono font-black">Điều 12.</span>
                  Thông Tin Đầu Mối Liên Hệ
                </h3>
                <p>Mọi thắc mắc, phản ánh hoặc đề nghị hỗ trợ liên quan đến Điều Khoản Dịch Vụ xin vui lòng liên hệ:</p>
                <div className="p-4 rounded-2xl bg-base-200/60 border border-base-300 text-xs space-y-1.5 font-sans">
                  <p>• <strong>Đơn vị vận hành:</strong> Ban Quản trị Nền tảng IT Blog</p>
                  <p>• <strong>Địa chỉ liên hệ:</strong> 227 Nguyễn Văn Cừ, Phường 4, Quận 5, TP. Hồ Chí Minh</p>
                  <p>• <strong>Email hỗ trợ:</strong> <a href="mailto:support@itblog.local" className="text-primary font-bold hover:underline">support@itblog.local</a></p>
                  <p>• <strong>Hotline kỹ thuật:</strong> (028) 3835 4409 (Thứ 2 – Thứ 7: 8:00 – 17:30)</p>
                </div>
              </div>
            </div>
          )}

          {/* SECTION 2: CHÍNH SÁCH BẢO MẬT */}
          {activeSection === "privacy" && (
            <div className="p-6 sm:p-10 bg-base-100 rounded-3xl border border-base-300 shadow-sm space-y-8 text-sm text-base-content/85 leading-relaxed animate-fade-in">
              <div className="border-b border-base-200 pb-5">
                <span className="badge badge-success text-white font-bold text-xs mb-2">Quyền riêng tư & Bảo vệ Dữ liệu</span>
                <h2 className="text-xl sm:text-2xl font-black text-base-content">
                  Chính Sách Bảo Mật Quyền Riêng Tư & Xử Lý Dữ Liệu Cá Nhân
                </h2>
                <p className="text-xs text-base-content/60 mt-1">
                  Phiên bản: 2.1 • Cập nhật lần cuối: 15/11/2026 • Ban Quản trị Nền tảng IT Blog
                </p>
              </div>

              {/* Phần 1 */}
              <div className="space-y-3">
                <h3 className="text-base font-bold text-base-content flex items-center gap-2">
                  <span className="text-success font-mono font-black">1.</span>
                  Phạm Vi & Nguyên Tắc Bảo Vệ Dữ Liệu Cá Nhân
                </h3>
                <p>
                  Nền tảng IT Blog cam kết bảo vệ dữ liệu cá nhân của mọi thành viên và độc giả theo đúng quy định pháp luật Việt Nam. Chúng tôi thực hiện xử lý dữ liệu dựa trên nguyên tắc: hợp pháp, minh bạch, đúng mục đích đã thông báo, tối thiểu hóa lượng dữ liệu thu thập, bảo đảm tính an toàn bảo mật và tôn trọng đầy đủ các quyền của chủ thể dữ liệu.
                </p>
              </div>

              {/* Phần 2 */}
              <div className="space-y-3">
                <h3 className="text-base font-bold text-base-content flex items-center gap-2">
                  <span className="text-success font-mono font-black">2.</span>
                  Phân Loại Dữ Liệu Được Thu Thập
                </h3>
                <div className="overflow-x-auto rounded-2xl border border-base-300">
                  <table className="table table-sm w-full text-xs">
                    <thead className="bg-base-200 text-base-content">
                      <tr>
                        <th className="py-2.5">Nhóm dữ liệu</th>
                        <th>Trường dữ liệu cụ thể</th>
                        <th>Phương thức & Thời điểm thu thập</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-base-200">
                      <tr>
                        <td className="font-bold text-primary">Tài khoản cơ bản</td>
                        <td>Họ tên, địa chỉ email, tên đăng nhập (username), mật khẩu đã mã hóa muối (hash/salt), ảnh đại diện (avatar), phần giới thiệu (bio).</td>
                        <td>Người dùng trực tiếp nhập khi đăng ký tài khoản hoặc cập nhật hồ sơ cá nhân.</td>
                      </tr>
                      <tr>
                        <td className="font-bold text-primary">Đăng nhập bên thứ ba</td>
                        <td>Địa chỉ email, tên hiển thị công khai, ID định danh người dùng do Google hoặc GitHub cấp.</td>
                        <td>Thu thập qua ủy quyền an toàn OAuth 2.0 khi người dùng chủ động chọn đăng nhập qua Google hoặc GitHub.</td>
                      </tr>
                      <tr>
                        <td className="font-bold text-primary">Nội dung sáng tạo</td>
                        <td>Bài viết kỹ thuật, trích đoạn mã nguồn (code block), bình luận thảo luận, thẻ gắn (tags), chuyên mục bài viết.</td>
                        <td>Người dùng chủ động soạn thảo và xuất bản công khai lên cộng đồng.</td>
                      </tr>
                      <tr>
                        <td className="font-bold text-primary">Hành vi đọc & Tương tác</td>
                        <td>Lượt xem (view), các mốc thời gian đọc (15s, 30s, đọc sâu trên 60s), thích (like), lưu bài viết (bookmark), chia sẻ (share), nhấp vào thẻ tag.</td>
                        <td>Ứng dụng tự động đo lường độ trễ đọc và ghi nhận tương tác kỹ thuật khi người dùng mở đọc bài viết.</td>
                      </tr>
                      <tr>
                        <td className="font-bold text-primary">Tương tác tài trợ Dev</td>
                        <td>Lượt hiển thị banner tài trợ, lượt click mở liên kết công cụ lập trình viên, thời điểm click.</td>
                        <td>Tự động ghi nhận khi mẩu tin xuất hiện và khi người dùng nhấp chọn công cụ tài trợ phù hợp.</td>
                      </tr>
                      <tr>
                        <td className="font-bold text-primary">Báo lỗi & Phản hồi</td>
                        <td>Tên người báo, email liên hệ, loại sự cố (Giao diện, API, Hiệu năng, Bảo mật), mô tả lỗi kỹ thuật.</td>
                        <td>Người dùng chủ động điền vào Biểu mẫu Báo lỗi & Góp ý kỹ thuật.</td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>

              {/* Phần 3 */}
              <div className="space-y-4">
                <h3 className="text-base font-bold text-base-content flex items-center gap-2">
                  <span className="text-success font-mono font-black">3.</span>
                  Quy Trình Sử Dụng & Mục Đích Xử Lý Dữ Liệu Chi Tiết
                </h3>
                <p>Mỗi nhóm dữ liệu thu thập được đưa vào các luồng xử lý chuyên biệt với mục đích rõ ràng:</p>

                <div className="space-y-3 text-xs sm:text-sm pl-2">
                  <div className="p-3.5 rounded-2xl bg-base-200/50 border border-base-300">
                    <h4 className="font-bold text-base-content mb-1">3.1. Quản lý tài khoản & Phân quyền bảo mật (RBAC)</h4>
                    <p className="text-base-content/80 text-xs">
                      Email được sử dụng để xác thực danh tính duy nhất, gửi thông báo hệ thống và phục hồi tài khoản. Mật khẩu được mã hóa một chiều qua thuật toán băm an toàn kèm muối (salt), tuyệt đối không lưu trữ dưới dạng văn bản rõ.
                    </p>
                  </div>

                  <div className="p-3.5 rounded-2xl bg-base-200/50 border border-base-300">
                    <h4 className="font-bold text-base-content mb-1">3.2. Thuật toán gợi ý bảng tin "Dành cho bạn" (Recommendation Engine)</h4>
                    <p className="text-base-content/80 text-xs">
                      Để hiển thị bài viết phù hợp với sở thích của từng độc giả, hệ thống phân tích các tín hiệu đọc gần nhất và gán trọng số tương quan: Bookmark (+5.0 điểm), Like (+4.0 điểm), Comment (+3.5 điểm), Đọc từ 30s trở lên (+3.0 điểm), Nhấp Tag (+2.0 điểm), Lượt xem (+1.0 điểm). Điểm số này kết hợp với quy luật suy giảm tự nhiên theo thời gian theo hàm mũ để ưu tiên bài viết mới và hữu ích. Dữ liệu này chỉ phục vụ xếp hạng bảng tin nội bộ, hoàn toàn không sử dụng để đánh giá hay chấm điểm ngoài phạm vi nền tảng.
                    </p>
                  </div>

                  <div className="p-3.5 rounded-2xl bg-base-200/50 border border-base-300">
                    <h4 className="font-bold text-base-content mb-1">3.3. Cơ chế hiển thị quảng cáo tài trợ Developer theo ngữ cảnh (Contextual Ads)</h4>
                    <p className="text-base-content/80 text-xs">
                      Các cơ hội tài trợ (AWS Credits, JetBrains IDE, MongoDB, Docker,...) được hiển thị dựa trên chuyên mục của bài viết bạn đang đọc (ví dụ: bài viết về Cloud sẽ hiển thị AWS; bài viết về Database sẽ hiển thị MongoDB). Chúng tôi chỉ ghi nhận số lượt nhấp chuột ẩn danh để đối soát gian lận, <strong>tuyệt đối không chia sẻ họ tên, email, lịch sử duyệt web hay địa chỉ IP của bạn cho bất kỳ nhà tài trợ nào</strong>.
                    </p>
                  </div>

                  <div className="p-3.5 rounded-2xl bg-base-200/50 border border-base-300">
                    <h4 className="font-bold text-base-content mb-1">3.4. Vận hành Trợ lý Trí tuệ nhân tạo (Google Gemini AI Assistant)</h4>
                    <p className="text-base-content/80 text-xs">
                      Khi bạn sử dụng nút "Hỏi đáp AI", "Tóm tắt bài viết", "Giải thích code" hoặc "Tạo giọng đọc", hệ thống gửi văn bản bài viết và câu hỏi kỹ thuật của bạn qua API để phân tích. Hệ thống <strong>không gửi thông tin cá nhân (họ tên, email, mật khẩu)</strong> vào ngữ cảnh của các lệnh gọi AI.
                    </p>
                  </div>

                  <div className="p-3.5 rounded-2xl bg-base-200/50 border border-base-300">
                    <h4 className="font-bold text-base-content mb-1">3.5. Kiểm duyệt tự động & Duy trì an toàn cộng đồng</h4>
                    <p className="text-base-content/80 text-xs">
                      Nội dung bài viết mới được quét tự động qua mô hình AI để đánh giá chỉ số an toàn, phát hiện spam, ngôn từ kích động thù địch hoặc mã độc nhằm bảo vệ độc giả trước các nguy cơ an ninh mạng.
                    </p>
                  </div>
                </div>
              </div>

              {/* Phần 4 */}
              <div className="space-y-3">
                <h3 className="text-base font-bold text-base-content flex items-center gap-2">
                  <span className="text-success font-mono font-black">4.</span>
                  Cơ Chế Chia Sẻ & Chuyển Giao Dữ Liệu (Trong Nước & Ra Nước Ngoài)
                </h3>
                <p>
                  IT Blog <strong>cam kết không thương mại hóa, không bán hoặc cho thuê dữ liệu cá nhân của người dùng</strong>. Hoạt động chuyển giao dữ liệu chỉ diễn ra trong các trường hợp kỹ thuật tối cần thiết:
                </p>

                <div className="space-y-2.5 text-xs pl-2">
                  <div className="p-3 rounded-xl bg-base-200/60 border border-base-300">
                    <p className="font-bold text-base-content">🌐 Chuyển dữ liệu ra nước ngoài qua Google Gemini API:</p>
                    <p className="text-base-content/80 mt-1">
                      Khi kích hoạt các tính năng AI, nội dung bài viết và truy vấn kỹ thuật được truyền tới máy chủ của Google LLC (Google Cloud) đặt ngoài lãnh thổ Việt Nam. Quá trình truyền tải được mã hóa toàn trình qua giao thức TLS 1.3. Thỏa thuận dịch vụ API doanh nghiệp cam kết không sử dụng dữ liệu truy vấn kỹ thuật của người dùng để đào tạo các mô hình AI đại trà.
                    </p>
                  </div>

                  <div className="p-3 rounded-xl bg-base-200/60 border border-base-300">
                    <p className="font-bold text-base-content">🔑 Đăng nhập liên thông Google & GitHub OAuth:</p>
                    <p className="text-base-content/80 mt-1">
                      Chỉ thực hiện trao đổi khóa xác thực token và thông tin hồ sơ cơ bản khi bạn chủ động lựa chọn phương thức đăng nhập bằng Google hoặc GitHub.
                    </p>
                  </div>

                  <div className="p-3 rounded-xl bg-base-200/60 border border-base-300">
                    <p className="font-bold text-base-content">⚖️ Cung cấp theo yêu cầu cơ quan Nhà nước có thẩm quyền:</p>
                    <p className="text-base-content/80 mt-1">
                      Chúng tôi chỉ cung cấp thông tin trong trường hợp có yêu cầu bằng văn bản chính thức từ cơ quan điều tra hoặc cơ quan có thẩm quyền tại Việt Nam phục vụ điều tra tội phạm công nghệ cao, hành vi phá hoại an ninh mạng theo quy định pháp luật.
                    </p>
                  </div>
                </div>
              </div>

              {/* Phần 5 */}
              <div className="space-y-3">
                <h3 className="text-base font-bold text-base-content flex items-center gap-2">
                  <span className="text-success font-mono font-black">5.</span>
                  Thời Hạn Lưu Trữ, Ẩn Danh & Tiêu Hủy Dữ Liệu
                </h3>
                <ul className="list-disc list-inside space-y-1.5 pl-2 text-xs sm:text-sm">
                  <li><strong>Dữ liệu tài khoản</strong>: Lưu trữ trong suốt thời gian bạn duy trì tài khoản trên hệ thống.</li>
                  <li><strong>Dữ liệu hành vi đọc bài</strong>: Lưu trữ chi tiết tối đa từ 12 đến 24 tháng để tối ưu bảng tin gợi ý, sau thời hạn này hệ thống tự động tổng hợp thành số liệu phi danh tính.</li>
                  <li><strong>Báo lỗi & Phản hồi kỹ thuật</strong>: Lưu trữ cho đến khi sự cố được giải quyết dứt điểm.</li>
                  <li><strong>Tiêu hủy dữ liệu</strong>: Khi bạn gửi yêu cầu xóa tài khoản hợp lệ, toàn bộ dữ liệu định danh cá nhân sẽ được xóa vĩnh viễn khỏi cơ sở dữ liệu chính trong vòng tối đa 30 ngày.</li>
                </ul>
              </div>

              {/* Phần 6 */}
              <div className="space-y-3">
                <h3 className="text-base font-bold text-base-content flex items-center gap-2">
                  <span className="text-success font-mono font-black">6.</span>
                  Quyền Của Chủ Thể Dữ Liệu & Quy Trình Giải Quyết Quyền
                </h3>
                <p>Theo pháp luật về bảo vệ dữ liệu cá nhân, bạn có đầy đủ các quyền hợp pháp sau:</p>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 text-xs">
                  <div className="p-3 rounded-xl bg-base-200/50 border border-base-300 space-y-1">
                    <p className="font-bold text-base-content">• Quyền được biết & Truy cập</p>
                    <p className="text-base-content/70">Xem và trích xuất dữ liệu tài khoản, lịch sử đọc bài tại trang Hồ sơ cá nhân.</p>
                  </div>
                  <div className="p-3 rounded-xl bg-base-200/50 border border-base-300 space-y-1">
                    <p className="font-bold text-base-content">• Quyền chỉnh sửa dữ liệu</p>
                    <p className="text-base-content/70">Tự do cập nhật họ tên, mật khẩu, ảnh đại diện và thông tin nghề nghiệp bất kỳ lúc nào.</p>
                  </div>
                  <div className="p-3 rounded-xl bg-base-200/50 border border-base-300 space-y-1">
                    <p className="font-bold text-base-content">• Quyền rút lại sự đồng ý</p>
                    <p className="text-base-content/70">Xóa lịch sử đọc cá nhân hoặc từ chối các tính năng theo dõi tương tác bất kỳ khi nào.</p>
                  </div>
                  <div className="p-3 rounded-xl bg-base-200/50 border border-base-300 space-y-1">
                    <p className="font-bold text-base-content">• Quyền yêu cầu xóa dữ liệu</p>
                    <p className="text-base-content/70">Yêu cầu xóa bài viết, bình luận hoặc đóng và xóa vĩnh viễn tài khoản khỏi hệ thống.</p>
                  </div>
                </div>

                <div className="p-4 rounded-2xl bg-base-200/70 border border-base-300 text-xs space-y-2 mt-2">
                  <p className="font-bold text-base-content">📩 Quy trình tiếp nhận & Cam kết thời hạn xử lý:</p>
                  <p>• <strong>Đầu mối tiếp nhận:</strong> Ban Quản trị Dữ liệu & An ninh mạng IT Blog qua email <code className="text-primary font-mono font-bold">support@itblog.local</code> hoặc mục "Gửi phản hồi / Báo lỗi".</p>
                  <p>• <strong>Thời hạn phản hồi:</strong> Ban Quản trị cam kết kiểm tra, phản hồi và giải quyết yêu cầu kỹ thuật của bạn trong thời hạn tối đa <strong>72 giờ</strong> kể từ khi tiếp nhận yêu cầu hợp lệ.</p>
                </div>
              </div>

              {/* Phần 7 */}
              <div className="space-y-3">
                <h3 className="text-base font-bold text-base-content flex items-center gap-2">
                  <span className="text-success font-mono font-black">7.</span>
                  Biện Pháp Bảo Đảm An Toàn Dữ Liệu Kỹ Thuật
                </h3>
                <p>Hệ thống IT Blog áp dụng các tiêu chuẩn an toàn thông tin hiện đại:</p>
                <ul className="list-disc list-inside space-y-1.5 pl-2 text-xs sm:text-sm">
                  <li><strong>Mã hóa mật khẩu:</strong> Sử dụng thuật toán băm một chiều an toàn (bcrypt với salt), bảo vệ tuyệt đối trước các đợt rò rỉ dữ liệu.</li>
                  <li><strong>Mã hóa đường truyền:</strong> Bắt buộc giao thức HTTPS/TLS trên toàn bộ kết nối giữa trình duyệt và máy chủ.</li>
                  <li><strong>Phân quyền truy cập (RBAC):</strong> Cách ly quyền hạn giữa Thành viên, Kiểm duyệt viên và Quản trị viên theo nguyên tắc đặc quyền tối thiểu.</li>
                  <li><strong>Bảo vệ chống tấn công:</strong> Tích hợp bộ lọc làm sạch mã độc (XSS Sanitizer) và hệ thống giới hạn tần suất yêu cầu (Rate Limiting).</li>
                </ul>
              </div>

              {/* Phần 8 */}
              <div className="space-y-3">
                <h3 className="text-base font-bold text-base-content flex items-center gap-2">
                  <span className="text-success font-mono font-black">8.</span>
                  Hiệu Lực & Cập Nhật Chính Sách
                </h3>
                <p>
                  Chính sách này có hiệu lực chính thức từ ngày công bố. Khi có điều chỉnh quan trọng về phạm vi thu thập hoặc chuyển dữ liệu ra nước ngoài, chúng tôi sẽ thông báo nổi bật trên trang chủ trước ít nhất 07 ngày để người dùng xem xét trước khi tiếp tục sử dụng dịch vụ.
                </p>
              </div>
            </div>
          )}

          {/* SECTION 3: QUY CHUẨN ĐĂNG BÀI */}
          {activeSection === "guidelines" && (
            <div className="p-6 sm:p-10 bg-base-100 rounded-3xl border border-base-300 shadow-sm space-y-6 text-sm text-base-content/80 leading-relaxed animate-fade-in">
              <div className="border-b border-base-200 pb-4">
                <span className="badge badge-warning text-amber-950 font-black text-xs mb-2">Tiêu chuẩn Biên tập Chuyên gia</span>
                <h2 className="text-xl sm:text-2xl font-black text-base-content">
                  Quy Chuẩn Đăng Bài & Định Dạng Kỹ Thuật
                </h2>
                <p className="text-xs text-base-content/60 mt-1">
                  Đảm bảo chất lượng học thuật và khả năng tiếp thu cao nhất cho độc giả kỹ sư IT
                </p>
              </div>

              <div className="space-y-4">
                <h3 className="text-base font-bold text-base-content flex items-center gap-2">
                  <span className="text-warning font-mono font-black">1.</span>
                  Cấu trúc Bài viết Tiêu chuẩn
                </h3>
                <div className="p-4 rounded-2xl bg-base-200/70 border border-base-300 space-y-2">
                  <p className="font-bold text-base-content">Một bài viết đạt chuẩn trên IT Blog cần có:</p>
                  <p>• <strong>Tiêu đề rõ ràng:</strong> Nêu bật vấn đề kỹ thuật (VD: "Tối ưu hóa Truy vấn PostgreSQL").</p>
                  <p>• <strong>Đoạn tóm tắt (Excerpt):</strong> 2-3 câu giới thiệu bối cảnh và kết quả đạt được.</p>
                  <p>• <strong>Nội dung chuyên sâu:</strong> Phân tích nguyên lý, kiến trúc hệ thống và giải pháp chi tiết.</p>
                  <p>• <strong>Code Block chuẩn:</strong> Code mẫu đầy đủ cú pháp, có thể chạy thử nghiệm được.</p>
                </div>
              </div>

              <div className="space-y-4">
                <h3 className="text-base font-bold text-base-content flex items-center gap-2">
                  <span className="text-warning font-mono font-black">2.</span>
                  Nghiêm cấm Đạo văn & Spam
                </h3>
                <p>
                  Mọi bài viết sao chép nguyên văn không dẫn nguồn, nội dung rác do bot tạo, hoặc bài viết thuần túy quảng cáo sẽ bị từ chối ngay trong khâu kiểm duyệt.
                </p>
              </div>
            </div>
          )}

          {/* SECTION 4: BẢN QUYỀN & GIẤY PHÉP */}
          {activeSection === "license" && (
            <div className="p-6 sm:p-10 bg-base-100 rounded-3xl border border-base-300 shadow-sm space-y-6 text-sm text-base-content/80 leading-relaxed animate-fade-in">
              <div className="border-b border-base-200 pb-4">
                <span className="badge badge-info text-white font-bold text-xs mb-2">Open Source & Creative Commons</span>
                <h2 className="text-xl sm:text-2xl font-black text-base-content">
                  Bản Quyền & Giấy Phép Mã Nguồn
                </h2>
                <p className="text-xs text-base-content/60 mt-1">
                  Quy định sử dụng tài sản trí tuệ và mã nguồn mở trên nền tảng
                </p>
              </div>

              <div className="p-4 rounded-2xl bg-base-200/80 border border-base-300 space-y-2 font-mono text-xs">
                <p className="font-bold text-base-content">MIT License</p>
                <p className="text-base-content/70">Copyright (c) 2026 IT Blog Platform Contributors.</p>
                <p className="text-base-content/70">
                  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files...
                </p>
              </div>

              <div className="space-y-3">
                <h3 className="text-base font-bold text-base-content">Bản quyền Bài viết (CC BY-NC-SA 4.0)</h3>
                <p>
                  Nội dung bài viết được chia sẻ theo giấy phép Creative Commons phi thương mại. Bạn được phép chia sẻ và chỉnh sửa khi có trích dẫn nguồn rõ ràng.
                </p>
              </div>
            </div>
          )}
          {/* SECTION 5: GỬI PHẢN HỒI / BÁO LỖI & THEO DÕI SỰ CỐ */}
          {activeSection === "feedback" && (
            <div className="p-6 sm:p-10 bg-base-100 rounded-3xl border border-base-300 shadow-sm space-y-6 text-sm leading-relaxed animate-fade-in">
              {/* Header */}
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-base-200 pb-4">
                <div>
                  <div className="flex items-center gap-2 mb-1">
                    <span className="badge badge-error text-white font-bold text-xs">Tiếp nhận & Xử lý</span>
                    <span className="badge badge-outline text-xs font-semibold">{bugTrackerList.length} sự cố ghi nhận</span>
                  </div>
                  <h2 className="text-xl sm:text-2xl font-black text-base-content">
                    Trung Tâm Báo Lỗi & Bảng Theo Dõi Sự Cố (Issue Tracker)
                  </h2>
                  <p className="text-xs text-base-content/60 mt-1">
                    Theo dõi danh sách các lỗi kỹ thuật đã được người dùng báo cáo và tiến độ khắc phục của đội ngũ kỹ thuật.
                  </p>
                </div>

                <div className="join self-start sm:self-auto shrink-0 shadow-2xs">
                  <button
                    type="button"
                    onClick={() => setFeedbackSubTab("tracker")}
                    className={`btn btn-sm join-item text-xs font-bold gap-1.5 cursor-pointer ${
                      feedbackSubTab === "tracker" ? "btn-primary text-white" : "btn-outline border-base-300"
                    }`}
                  >
                    <span>📋</span> Danh sách lỗi ({bugTrackerList.length})
                  </button>
                  <button
                    type="button"
                    onClick={() => setFeedbackSubTab("form")}
                    className={`btn btn-sm join-item text-xs font-bold gap-1.5 cursor-pointer ${
                      feedbackSubTab === "form" ? "btn-primary text-white" : "btn-outline border-base-300"
                    }`}
                  >
                    <span>➕</span> Gửi báo lỗi mới
                  </button>
                </div>
              </div>

              {/* View 1: Tracker (Danh sách các lỗi được báo cáo) */}
              {feedbackSubTab === "tracker" && (
                <div className="space-y-4">
                  {/* Search and Filters */}
                  <div className="p-4 rounded-2xl bg-base-200/60 border border-base-300 flex flex-col md:flex-row md:items-center justify-between gap-3">
                    <div className="relative flex-1 max-w-md">
                      <input
                        type="text"
                        placeholder="Tìm kiếm lỗi hoặc đề xuất đã báo cáo..."
                        value={bugSearch}
                        onChange={(e) => setBugSearch(e.target.value)}
                        className="input input-sm input-bordered w-full rounded-xl text-xs pl-8 font-medium"
                      />
                      <span className="absolute left-2.5 top-1/2 -translate-y-1/2 text-xs text-base-content/50">🔍</span>
                      {bugSearch && (
                        <button
                          type="button"
                          onClick={() => setBugSearch("")}
                          className="absolute right-2.5 top-1/2 -translate-y-1/2 text-xs text-base-content/50 hover:text-base-content"
                        >
                          ✕
                        </button>
                      )}
                    </div>

                    <div className="flex flex-wrap items-center gap-2">
                      <select
                        value={bugFilterCategory}
                        onChange={(e) => setBugFilterCategory(e.target.value)}
                        className="select select-sm select-bordered rounded-xl text-xs font-semibold"
                      >
                        <option value="all">Mọi phân loại</option>
                        <option value="bug">🐛 Lỗi tính năng</option>
                        <option value="feature">💡 Góp ý tính năng</option>
                        <option value="content">⚠️ Vi phạm nội dung</option>
                        <option value="other">💬 Ý kiến khác</option>
                      </select>

                      <div className="join">
                        {[
                          { key: "all", label: "Tất cả" },
                          { key: "pending", label: "Chờ xử lý" },
                          { key: "in_progress", label: "Đang sửa" },
                          { key: "resolved", label: "Đã xong" }
                        ].map((tab) => (
                          <button
                            key={tab.key}
                            type="button"
                            onClick={() => setBugFilterStatus(tab.key)}
                            className={`btn btn-xs join-item ${
                              bugFilterStatus === tab.key ? "btn-primary text-white font-bold" : "btn-ghost"
                            }`}
                          >
                            {tab.label}
                          </button>
                        ))}
                      </div>
                    </div>
                  </div>

                  {/* List of Reported Bugs */}
                  {(() => {
                    const filtered = bugTrackerList.filter((item) => {
                      const matchStatus =
                        bugFilterStatus === "all" ||
                        item.status === bugFilterStatus ||
                        (bugFilterStatus === "pending" && (item.status === "received" || !item.status));
                      const matchCategory =
                        bugFilterCategory === "all" || (item.category || item.type) === bugFilterCategory;
                      const term = bugSearch.trim().toLowerCase();
                      const matchSearch =
                        !term ||
                        (item.title || item.subject || "").toLowerCase().includes(term) ||
                        (item.description || item.content || "").toLowerCase().includes(term) ||
                        (item.reporter_name || item.name || "").toLowerCase().includes(term);
                      return matchStatus && matchCategory && matchSearch;
                    });

                    if (filtered.length === 0) {
                      return (
                        <div className="text-center py-12 bg-base-200/40 rounded-2xl border border-dashed border-base-300 p-6 space-y-2">
                          <span className="text-3xl">🎉</span>
                          <h4 className="font-bold text-sm text-base-content">Không tìm thấy báo cáo nào</h4>
                          <p className="text-xs text-base-content/60">
                            Không có báo cáo lỗi nào phù hợp với bộ lọc hiện tại.
                          </p>
                          <button
                            type="button"
                            onClick={() => {
                              setBugFilterStatus("all");
                              setBugFilterCategory("all");
                              setBugSearch("");
                            }}
                            className="btn btn-xs btn-outline rounded-lg mt-2 font-bold"
                          >
                            Xóa bộ lọc
                          </button>
                        </div>
                      );
                    }

                    return (
                      <div className="space-y-3">
                        {filtered.map((item) => {
                          const isBug = (item.category || item.type) === "bug";
                          const isFeature = (item.category || item.type) === "feature";
                          const isResolved = item.status === "resolved";
                          const isInProgress = item.status === "in_progress";
                          const isPending = !item.status || item.status === "pending" || item.status === "received";

                          return (
                            <div
                              key={item.id}
                              className={`p-4 sm:p-5 rounded-2xl border transition-all ${
                                isResolved
                                  ? "bg-base-100 border-success/30 shadow-2xs"
                                  : isInProgress
                                  ? "bg-base-100 border-primary/30 shadow-xs"
                                  : "bg-base-100 border-base-300 shadow-2xs"
                              }`}
                            >
                              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 pb-2 border-b border-base-200">
                                <div className="flex flex-wrap items-center gap-2">
                                  {/* Status Badge */}
                                  <span
                                    className={`badge badge-sm font-black text-xs ${
                                      isResolved
                                        ? "badge-success text-white"
                                        : isInProgress
                                        ? "badge-info text-white"
                                        : isPending
                                        ? "badge-warning text-amber-950"
                                        : "badge-ghost"
                                    }`}
                                  >
                                    {isResolved
                                      ? "🟢 Đã khắc phục"
                                      : isInProgress
                                      ? "🔵 Đang xử lý"
                                      : isPending
                                      ? "🟡 Chờ tiếp nhận"
                                      : "⚪ Đã đóng"}
                                  </span>

                                  {/* Type Badge */}
                                  <span className="badge badge-sm badge-outline font-semibold">
                                    {isBug
                                      ? "🐛 Lỗi chức năng"
                                      : isFeature
                                      ? "💡 Góp ý tính năng"
                                      : (item.category || item.type) === "content"
                                      ? "⚠️ Vi phạm"
                                      : "💬 Ý kiến khác"}
                                  </span>

                                  {/* Priority Badge */}
                                  {item.priority && (
                                    <span
                                      className={`badge badge-xs font-bold uppercase ${
                                        item.priority === "critical"
                                          ? "badge-error text-white"
                                          : item.priority === "high"
                                          ? "badge-warning text-amber-950"
                                          : "badge-ghost"
                                      }`}
                                    >
                                      {item.priority === "critical"
                                        ? "🚨 Khẩn cấp"
                                        : item.priority === "high"
                                        ? "🔴 Cao"
                                        : item.priority === "medium"
                                        ? "🟡 Vừa"
                                        : "🟢 Thấp"}
                                    </span>
                                  )}

                                  <span className="text-[11px] font-mono text-base-content/40">#{item.id}</span>
                                </div>

                                <div className="text-[11px] text-base-content/50">
                                  {item.created_at && !isNaN(new Date(item.created_at).getTime())
                                    ? new Date(item.created_at).toLocaleString("vi-VN")
                                    : "Gần đây"}
                                </div>
                              </div>

                              {/* Title and Description */}
                              <div className="pt-3 space-y-1.5">
                                <h3 className="font-extrabold text-sm sm:text-base text-base-content">
                                  {item.title || item.subject}
                                </h3>
                                <p className="text-xs text-base-content/80 whitespace-pre-wrap leading-relaxed">
                                  {item.description || item.content}
                                </p>
                              </div>

                              {/* Reporter Info */}
                              <div className="pt-2 text-[11px] text-base-content/50 flex items-center gap-3">
                                <span>
                                  Người báo cáo: <strong>{item.reporter_name || item.name || "Khách"}</strong>
                                </span>
                              </div>

                              {/* Admin Response Note */}
                              {item.admin_notes && (
                                <div className="mt-3 p-3 rounded-xl bg-primary/5 border border-primary/20 text-xs text-base-content/90 space-y-1 animate-fade-in">
                                  <div className="flex items-center gap-1.5 font-bold text-primary text-[11px] uppercase tracking-wider">
                                    <span>🛠️ Phản hồi từ Ban Quản trị / Kỹ thuật:</span>
                                  </div>
                                  <p className="leading-relaxed font-medium">{item.admin_notes}</p>
                                </div>
                              )}
                            </div>
                          );
                        })}
                      </div>
                    );
                  })()}
                </div>
              )}

              {/* View 2: Submission Form */}
              {feedbackSubTab === "form" && (
                <div className="space-y-4">
                  <div className="p-3.5 rounded-2xl bg-info/10 border border-info/20 text-xs text-info-content flex items-start gap-2">
                    <span className="text-base">💡</span>
                    <div>
                      <p className="font-bold">Mẹo tránh báo cáo trùng lặp:</p>
                      <p className="text-base-content/70 mt-0.5">
                        Bạn có thể kiểm tra tab <strong>"Danh sách lỗi"</strong> trước để xem sự cố của mình đã được ai báo cáo hay chưa.
                      </p>
                    </div>
                  </div>

                  {/* Feedback Form */}
                  <form onSubmit={handleFeedbackSubmit} className="space-y-4">
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                      <div>
                        <label className="label text-xs font-bold text-base-content pb-1">Phân loại phản hồi:</label>
                        <select
                          value={feedbackType}
                          onChange={(e) => setFeedbackType(e.target.value)}
                          className="select select-sm select-bordered w-full rounded-xl text-xs font-semibold"
                        >
                          <option value="bug">🐛 Báo lỗi chức năng (Bug Report)</option>
                          <option value="feature">💡 Góp ý tính năng mới (Feature Request)</option>
                          <option value="content">⚠️ Khiếu nại nội dung bài viết vi phạm</option>
                          <option value="partner">🤝 Đề xuất hợp tác & Tài trợ</option>
                          <option value="other">💬 Ý kiến đóng góp khác</option>
                        </select>
                      </div>

                      <div>
                        <label className="label text-xs font-bold text-base-content pb-1">Mức độ ưu tiên:</label>
                        <select
                          value={feedbackPriority}
                          onChange={(e) => setFeedbackPriority(e.target.value)}
                          className="select select-sm select-bordered w-full rounded-xl text-xs font-semibold"
                        >
                          <option value="low">🟢 Thấp (Ý kiến tham khảo)</option>
                          <option value="medium">🟡 Trung bình (Cải tiến UI/UX)</option>
                          <option value="high">🔴 Cao (Lỗi cản trở trải nghiệm)</option>
                          <option value="critical">🚨 Khẩn cấp (Sự cố bảo mật/Mất dữ liệu)</option>
                        </select>
                      </div>
                    </div>

                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                      <div>
                        <label className="label text-xs font-bold text-base-content pb-1">Họ tên của bạn:</label>
                        <input
                          type="text"
                          placeholder="Nguyễn Văn A"
                          value={feedbackName}
                          onChange={(e) => setFeedbackName(e.target.value)}
                          className="input input-sm input-bordered w-full rounded-xl text-xs"
                        />
                      </div>
                      <div>
                        <label className="label text-xs font-bold text-base-content pb-1">
                          Email liên hệ nhận phản hồi <span className="text-error">*</span>:
                        </label>
                        <input
                          type="email"
                          required
                          placeholder="you@domain.com"
                          value={feedbackEmail}
                          onChange={(e) => setFeedbackEmail(e.target.value)}
                          className="input input-sm input-bordered w-full rounded-xl text-xs"
                        />
                      </div>
                    </div>

                    <div>
                      <label className="label text-xs font-bold text-base-content pb-1">
                        Tiêu đề phản hồi <span className="text-error">*</span>:
                      </label>
                      <input
                        type="text"
                        required
                        placeholder="VD: Lỗi hiển thị code block bị tràn màn hình..."
                        value={feedbackSubject}
                        onChange={(e) => setFeedbackSubject(e.target.value)}
                        className="input input-sm input-bordered w-full rounded-xl text-xs font-semibold"
                      />
                    </div>

                    <div>
                      <label className="label text-xs font-bold text-base-content pb-1">
                        Chi tiết phản hồi / Các bước tái hiện lỗi <span className="text-error">*</span>:
                      </label>
                      <textarea
                        required
                        rows={4}
                        placeholder="Mô tả cụ thể sự cố, môi trường trình duyệt/thiết bị, hành động trước khi lỗi xảy ra hoặc giải pháp đề xuất..."
                        value={feedbackContent}
                        onChange={(e) => setFeedbackContent(e.target.value)}
                        className="textarea textarea-bordered w-full rounded-2xl text-xs leading-relaxed"
                      ></textarea>
                    </div>

                    <div className="flex items-center justify-between pt-2">
                      <button
                        type="button"
                        onClick={() => setFeedbackSubTab("tracker")}
                        className="text-xs text-base-content/60 hover:text-base-content font-bold cursor-pointer"
                      >
                        ← Quay lại danh sách lỗi
                      </button>
                      <button
                        type="submit"
                        disabled={feedbackSubmitting}
                        className="btn btn-sm btn-primary text-white font-bold rounded-xl gap-2 shadow-sm cursor-pointer"
                      >
                        {feedbackSubmitting ? (
                          <span className="loading loading-spinner loading-xs"></span>
                        ) : (
                          <span>📨 Gửi báo lỗi ngay</span>
                        )}
                      </button>
                    </div>
                  </form>
                </div>
              )}
            </div>
          )}

          {/* SECTION 6: SƠ ĐỒ TRANG (SITEMAP) */}
          {activeSection === "sitemap" && (
            <div className="p-6 sm:p-10 bg-base-100 rounded-3xl border border-base-300 shadow-sm space-y-8 text-sm leading-relaxed animate-fade-in">
              <div className="border-b border-base-200 pb-4">
                <span className="badge badge-primary text-white font-bold text-xs mb-2">Điều hướng toàn diện</span>
                <h2 className="text-xl sm:text-2xl font-black text-base-content">
                  Sơ Đồ Trang & Danh Mục Nền Tảng (Sitemap)
                </h2>
                <p className="text-xs text-base-content/60 mt-1">
                  Bản đồ liên kết trực tiếp tới toàn bộ các tính năng, chuyên mục và tài nguyên trên IT Blog
                </p>
              </div>

              {/* Sitemap Grid */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="p-5 rounded-2xl bg-base-200/60 border border-base-300 space-y-3">
                  <h3 className="font-bold text-sm text-base-content flex items-center gap-2">
                    <span>📰</span> Khám Phá & Đọc Bài Viết
                  </h3>
                  <ul className="space-y-2 text-xs">
                    <li>
                      <button
                        onClick={() => onNavigate && onNavigate("home")}
                        className="text-primary font-bold hover:underline cursor-pointer"
                      >
                        → Bảng tin Trang chủ (Newfeed)
                      </button>
                    </li>
                    <li className="font-semibold text-base-content/70 pt-1">Chuyên mục công nghệ:</li>
                    <div className="grid grid-cols-2 gap-1.5 pl-2">
                      {["Frontend", "Backend", "DevOps", "AI & Data", "Database", "System", "Mobile"].map((cat) => (
                        <button
                          key={cat}
                          onClick={() => navigateToCategory(cat)}
                          className="text-left text-base-content/80 hover:text-primary transition-colors cursor-pointer"
                        >
                          • {cat}
                        </button>
                      ))}
                    </div>
                  </ul>
                </div>

                <div className="p-5 rounded-2xl bg-base-200/60 border border-base-300 space-y-3">
                  <h3 className="font-bold text-sm text-base-content flex items-center gap-2">
                    <span>🎓</span> Học Tập & Phát Triển Kỹ Năng
                  </h3>
                  <ul className="space-y-2 text-xs">
                    <li>
                      <button
                        onClick={() => onNavigate && onNavigate("roadmaps")}
                        className="text-primary font-bold hover:underline cursor-pointer"
                      >
                        → Lộ trình nghề nghiệp (Roadmaps)
                      </button>
                    </li>
                    <li>
                      <button
                        onClick={() => onNavigate && onNavigate("courses")}
                        className="text-primary font-bold hover:underline cursor-pointer"
                      >
                        → Khóa học & Bài giảng thực chiến (Courses)
                      </button>
                    </li>
                    <li>
                      <button
                        onClick={() => onNavigate && onNavigate("quiz")}
                        className="text-primary font-bold hover:underline cursor-pointer"
                      >
                        → Trắc nghiệm kỹ thuật & Luyện phỏng vấn (Quiz)
                      </button>
                    </li>
                    <li>
                      <button
                        onClick={() => onNavigate && onNavigate("jobs")}
                        className="text-primary font-bold hover:underline cursor-pointer"
                      >
                        → Cơ hội việc làm IT & Tuyển dụng (Jobs)
                      </button>
                    </li>
                    <li>
                      <button
                        onClick={() => onNavigate && onNavigate("events")}
                        className="text-primary font-bold hover:underline cursor-pointer"
                      >
                        → Sự kiện công nghệ & Hackathon (Events)
                      </button>
                    </li>
                    <li>
                      <button
                        onClick={() => onNavigate && onNavigate("leaderboard")}
                        className="text-primary font-bold hover:underline cursor-pointer"
                      >
                        → Bảng xếp hạng tác giả xuất sắc (Leaderboard)
                      </button>
                    </li>
                  </ul>
                </div>
              </div>
            </div>
          )}

          {/* SECTION 7: RSS FEEDS */}
          {activeSection === "rss" && (
            <div className="p-6 sm:p-10 bg-base-100 rounded-3xl border border-base-300 shadow-sm space-y-6 text-sm leading-relaxed animate-fade-in">
              <div className="border-b border-base-200 pb-4">
                <span className="badge badge-warning text-amber-950 font-black text-xs mb-2">Chuẩn RSS 2.0 XML</span>
                <h2 className="text-xl sm:text-2xl font-black text-base-content">
                  Nguồn Cấp Tin Tức Kỹ Thuật (RSS Feeds)
                </h2>
                <p className="text-xs text-base-content/60 mt-1">
                  Đăng ký nhận bài viết kỹ thuật mới nhất tự động qua các ứng dụng đọc tin RSS Reader
                </p>
              </div>

              <div className="p-5 rounded-2xl bg-base-200/80 border border-base-300 space-y-4">
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                  <div>
                    <h3 className="font-bold text-base text-base-content flex items-center gap-2">
                      <span>📰</span> Luồng Tin Chính Thức IT Blog (All Posts)
                    </h3>
                    <p className="text-xs text-base-content/60">
                      Cập nhật theo thời gian thực tất cả bài viết đã phê duyệt và xuất bản.
                    </p>
                  </div>
                  <div className="flex items-center gap-2">
                    <a
                      href={api.seo.getRssUrl()}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="btn btn-sm btn-primary text-white font-bold text-xs gap-1.5 cursor-pointer"
                    >
                      <span>Mở XML</span> ↗
                    </a>
                    <button
                      type="button"
                      onClick={copyRssUrl}
                      className="btn btn-sm btn-outline text-xs font-bold cursor-pointer"
                      title="Sao chép link RSS"
                    >
                      Sao chép link
                    </button>
                  </div>
                </div>

                <div className="p-3 rounded-xl bg-base-100 border border-base-300 font-mono text-xs text-primary break-all">
                  {api.seo.getRssUrl()}
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Right Side: Quick Contact & Help Sidebar */}
        <div className="space-y-6">
          <div className="p-6 rounded-3xl bg-base-100 border border-base-300 shadow-xs space-y-4">
            <h3 className="font-bold text-sm text-base-content flex items-center gap-2">
              <span>📞</span> Kênh Hỗ Trợ Trực Tiếp
            </h3>

            <div className="space-y-3 text-xs">
              <div className="flex items-start gap-2.5">
                <span className="text-base">📧</span>
                <div>
                  <p className="font-bold text-base-content">Email Hỗ Trợ:</p>
                  <a href="mailto:support@itblog.local" className="text-primary hover:underline font-semibold">
                    support@itblog.local
                  </a>
                </div>
              </div>

              <div className="flex items-start gap-2.5">
                <span className="text-base">☎️</span>
                <div>
                  <p className="font-bold text-base-content">Hotline Kỹ Thuật:</p>
                  <a href="tel:02838354409" className="text-primary hover:underline font-semibold">
                    (028) 3835 4409
                  </a>
                </div>
              </div>

              <div className="flex items-start gap-2.5">
                <span className="text-base">🕒</span>
                <div>
                  <p className="font-bold text-base-content">Thời Gian Làm Việc:</p>
                  <p className="text-base-content/70">Thứ 2 - Thứ 7: 8:00 - 17:30</p>
                </div>
              </div>

              <div className="flex items-start gap-2.5">
                <span className="text-base">📍</span>
                <div>
                  <p className="font-bold text-base-content">Địa Chỉ:</p>
                  <p className="text-base-content/70">Khoa Công nghệ Thông tin - Trường Đại học</p>
                </div>
              </div>
            </div>

            <div className="pt-2 border-t border-base-200">
              <button
                type="button"
                onClick={() => setActiveSection("feedback")}
                className="btn btn-sm btn-primary w-full text-white font-bold rounded-xl text-xs gap-1.5 shadow-xs cursor-pointer"
              >
                <span>💬</span> Mở Biểu Mẫu Báo Lỗi
              </button>
            </div>
          </div>

          <div className="p-5 rounded-3xl bg-emerald-500/10 border border-emerald-500/20 text-xs space-y-2">
            <div className="flex items-center justify-between">
              <span className="font-bold text-emerald-700 dark:text-emerald-400 flex items-center gap-1.5">
                <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
                Hệ thống Hoạt động 100%
              </span>
              <span className="badge badge-xs badge-success text-white font-bold">Online</span>
            </div>
            <p className="text-base-content/70 leading-relaxed text-[11px]">
              Tất cả các dịch vụ: Web Portal, RESTful API, PostgreSQL Database và Gemini AI Mod Service đang hoạt động bình thường.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
