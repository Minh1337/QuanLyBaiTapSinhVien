# Ứng dụng quản lý bài tập sinh viên

Ứng dụng Android hỗ trợ quản trị viên quản lý tài khoản và lớp học phần, giáo viên giao và chấm bài, sinh viên nộp bài và theo dõi việc học. Đây là đồ án môn học, ưu tiên Java, XML, Activity, ListView/Adapter, Intent và SQLite.

## Chức năng

### Quản trị viên

- Tạo tài khoản giáo viên và sinh viên; khóa hoặc mở khóa tài khoản.
- Tạo lớp học phần, chọn giáo viên phụ trách và thêm nhiều sinh viên trong một lần.
- Chặn trùng mã lớp và sinh viên trong cùng lớp; gỡ sinh viên hoặc đổi giáo viên phụ trách.
- Lưu trữ lớp đã sử dụng để giữ lịch sử.

### Giáo viên

- Xem các lớp được phân công và danh sách sinh viên trong lớp.
- Tạo, sửa hoặc thu hồi bài tập; nhập yêu cầu, hạn nộp và đính kèm tệp hướng dẫn.
- Xem trạng thái nộp và tiến độ theo từng sinh viên.
- Mở bài nộp, chấm điểm, ghi nhận xét hoặc yêu cầu sinh viên nộp lại.

### Sinh viên

- Xem lớp đang tham gia, bài được giáo viên giao, yêu cầu, hạn nộp và tệp hướng dẫn.
- Nộp câu trả lời hoặc tệp; xem trạng thái, điểm và nhận xét; sửa bài theo điều kiện của bài tập.
- Tự tạo, sửa, xóa và theo dõi việc cá nhân; tìm kiếm, lọc và sắp xếp danh sách.
- Xem lịch tuần, thống kê và chia sẻ dữ liệu CSV của tài khoản đang đăng nhập.

## Đăng nhập và tài khoản

Ứng dụng chỉ có chức năng đăng nhập, không có đăng ký công khai hoặc chọn vai trò demo. Sau khi đăng nhập, ứng dụng mở khu vực tương ứng với vai trò `ADMIN`, `TEACHER` hoặc `STUDENT`. Quản trị viên tạo tài khoản giáo viên và sinh viên từ màn hình quản lý tài khoản.

Khi khởi tạo cơ sở dữ liệu mới, ứng dụng tạo sẵn tài khoản quản trị viên để chạy thử:

- Email: `admin@gmail.com`
- Mật khẩu: `admin123`

Đây là thông tin dành cho bản demo học tập. Hãy đổi hoặc thay tài khoản trước khi dùng dữ liệu thật.

## Công nghệ

- Android, Java 11 và XML.
- SQLite cục bộ thông qua `SQLiteOpenHelper`.
- Gradle Wrapper; thư viện Android được khai báo trong Version Catalog.
- Mã gói: `com.example.quanlybaitapsinhvien`.
- Phiên bản ứng dụng: 1.1 (`versionCode` 2); phiên bản SQLite: 7.
- Android tối thiểu: API 24.

## Mở và chạy ứng dụng

1. Mở Android Studio và chọn **Open**.
2. Chọn thư mục gốc của dự án (thư mục chứa `settings.gradle.kts`).
3. Chờ Gradle Sync hoàn tất, sau đó chọn cấu hình chạy `app`.
4. Kết nối điện thoại Android hoặc mở máy ảo rồi nhấn **Run**.
5. Đăng nhập bằng tài khoản quản trị mặc định ở trên; tạo tài khoản giáo viên, sinh viên và lớp để thử các luồng của ứng dụng.

## Cơ sở dữ liệu

SQLite lưu dữ liệu trong thiết bị. Các bảng chính:

| Bảng | Nội dung |
|---|---|
| `tai_khoan` | Email, mật khẩu demo, vai trò, trạng thái, họ tên và mã số |
| `lop_hoc_phan` | Mã lớp, học phần, học kỳ, năm học, giáo viên phụ trách và trạng thái lưu trữ |
| `thanh_vien_lop` | Quan hệ sinh viên tham gia lớp |
| `bai_tap` | Đề bài, yêu cầu, tệp hướng dẫn, hạn nộp và trạng thái |
| `bai_nop` | Câu trả lời/tệp sinh viên nộp, thời gian nộp, trạng thái, điểm và nhận xét |
| `bai_ca_nhan` | Việc riêng do sinh viên tự quản lý |

Thông tin bài tập cá nhân, lịch, thống kê và dữ liệu CSV được giới hạn theo tài khoản sinh viên hiện đăng nhập. Báo cáo giáo viên chỉ lấy tiến độ của các lớp giáo viên được phân công.

Khi nâng cấp từ bản 1.0 trên nhánh `master` (SQLite phiên bản 6), ứng dụng giữ tài khoản và các bảng dữ liệu cũ, sau đó tạo các bảng cho chức năng mới. Môn học, bài trắc nghiệm và tiến độ theo cấu trúc cũ không tự chuyển thành lớp học phần và bài tập mới. Quản trị viên cần tạo lớp và xếp sinh viên lại trong giao diện mới; dữ liệu bảng cũ không bị xóa.

## Giới hạn của bản đồ án

- Cơ sở dữ liệu SQLite nằm trên thiết bị. Dữ liệu không tự đồng bộ giữa điện thoại hoặc máy tính khác nhau; demo các vai trò trên cùng một thiết bị.
- Tệp đính kèm được ứng dụng tham chiếu qua URI do Android cung cấp. Tệp cần còn truy cập được trên thiết bị đang dùng.
- Mật khẩu hiện được lưu theo cách đơn giản để phục vụ bản demo học tập, không phù hợp cho hệ thống triển khai thật.
- CSV hiện được chia sẻ dưới dạng nội dung qua chức năng chia sẻ của Android.

## Tài liệu dự án

- [PROMPT_DU_AN.md](PROMPT_DU_AN.md): yêu cầu và phạm vi chức năng.
- [TIEN_DO_DU_AN.md](TIEN_DO_DU_AN.md): tiến độ, nội dung đã triển khai và các việc cần rà soát.