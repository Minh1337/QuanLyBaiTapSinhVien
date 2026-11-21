# Tiến độ ứng dụng quản lý bài tập sinh viên

## Yêu cầu đã thống nhất

- Ứng dụng Android dùng Java và XML, tông hồng và tím theo thiết kế Figma.
- Có ba quyền: `ADMIN`, `TEACHER`, `STUDENT`. Đăng nhập đọc vai trò từ cơ sở dữ liệu rồi mở đúng khu vực.
- Không có đăng ký công khai. Quản trị viên cấp tài khoản giáo viên và sinh viên.
- Quản trị viên quản lý tài khoản, lớp học phần, giáo viên phụ trách và thành viên; lớp đã dùng được lưu trữ để giữ lịch sử.
- Giáo viên quản lý lớp được phân công, giao/sửa/thu hồi bài, xem tiến độ, chấm điểm và phản hồi bài nộp.
- Sinh viên xem bài giáo viên giao, nộp câu trả lời hoặc tệp, xem điểm/nhận xét; đồng thời tự quản lý việc cá nhân, lịch tuần, thống kê và dữ liệu CSV của chính mình.
- Ưu tiên tên biến, hàm, lớp và chú thích bằng tiếng Việt; chỉ giữ chú thích khi giúp giải thích phần cần thiết.
- Đây là đồ án học tập, chỉ cần demo đủ ba vai trò trên một thiết bị. Ưu tiên kiến thức đã học; SQLite là phần mở rộng tối thiểu để lưu tài khoản, lớp và bài nộp.

## Ràng buộc kiến thức môn học

Đã đọc `So_tay_on_tap_lap_trinh_di_dong_4_tai_lieu.docx` tại `C:\Users\Admin\Desktop\Project\Web_TT\Music\`.

Kiến thức sổ tay tập trung vào Activity, XML layout, View, EditText, kiểm tra dữ liệu, Toast, ListView/Adapter, ArrayList, CRUD, tìm kiếm/sắp xếp, Menu và Intent. Sổ tay ghi rõ SQLite hoặc Room, lưu trạng thái, API/HTTP, bảo mật và kiến trúc ứng dụng chưa được dạy sâu. Vì vậy chỉ dùng SQLite ở mức cần thiết cho tài khoản, lớp và bài nộp; các màn hình và luồng thao tác tiếp tục theo ví dụ Java/XML căn bản trong môn. Không yêu cầu máy chủ, Firebase hay kiến trúc phức tạp để hoàn thành đồ án.

## Trạng thái mã nguồn lúc lưu

- Nhánh hiện tại: `feature/homepage-branding-and-report`.
- `DatabaseHelper.java` dùng SQLite phiên bản 3, có migration giữ dữ liệu cũ và các bảng tài khoản, lớp, thành viên, bài tập, bài nộp, việc cá nhân.
- `HomeActivity.java` có màn hình theo quyền quản trị viên, giáo viên, sinh viên; danh sách dùng `ListView` và `ArrayAdapter`. Sinh viên có tìm kiếm, lọc trạng thái, sắp xếp, lịch tuần, thống kê và chia sẻ dữ liệu CSV.
- Luồng đăng ký công khai đã được gỡ khỏi `WelcomeActivity`, `LoginActivity` và `AndroidManifest.xml`; `RegisterActivity.java` cùng các layout đăng ký đã xóa. Chỉ tài khoản quản trị viên tạo tài khoản mới.
- Đã biên dịch toàn bộ APK debug bằng JDK 21 giải nén tạm trong `C:\Users\Admin\AppData\Local\Temp\codex-jdk21`; `:app:assembleDebug` thành công. Máy chưa cấu hình `JAVA_HOME` cố định. Trước đó đã sửa ba lỗi truyền kiểu dữ liệu ở tổng quan quản trị.
- Theo cơ chế CST người dùng mô tả (không yêu cầu đăng nhập hệ thống CST): quản trị viên cấp tài khoản, tạo lớp, chọn giáo viên và xếp sinh viên. Đã làm rõ lối vào “Xếp lớp / quản lý lớp”, hướng dẫn ngay trên màn hình lớp, cho phép chạm trạng thái chưa có lớp để tạo lớp, đổi nút thành “Xếp sinh viên” và sau khi tạo lớp chuyển về danh sách lớp.
- Sửa lỗi chạm vào thẻ lớp không mở chi tiết: bỏ `clickable` và `focusable` khỏi hàng con của `ListView` để sự kiện được xử lý bởi `OnItemClickListener` của danh sách. Cần chạy lại ứng dụng để xác nhận trên điện thoại.
- Xếp sinh viên hỗ trợ chọn nhiều người bằng danh sách checkbox, đếm số đã chọn, chọn/bỏ chọn tất cả và thêm vào lớp bằng một thao tác. Danh sách chỉ gồm sinh viên đang hoạt động chưa thuộc lớp; kết quả hiển thị số người thêm thành công.
- Đã gỡ nút và hàm tạo 60 tài khoản mẫu theo yêu cầu mới. Quản trị viên cấp từng tài khoản qua biểu mẫu; dữ liệu tài khoản từng được tạo trên thiết bị vẫn được giữ nguyên.
- Cơ sở dữ liệu nâng lên phiên bản 3, thêm URI và tên tệp hướng dẫn cho bài tập cùng migration giữ dữ liệu cũ. Giáo viên có thể chọn/đổi/gỡ tệp khi giao hoặc sửa bài; giáo viên và sinh viên mở được tệp ở trang chi tiết. Sinh viên được sửa bài nộp của mình trước hạn hoặc khi giáo viên yêu cầu nộp lại; đề bài và hạn nộp vẫn chỉ đọc.
- Trang chi tiết bài giáo viên giao hiển thị hạn, yêu cầu, tệp hướng dẫn, tình trạng nộp, điểm/nhận xét, thời gian còn lại và bài làm/tệp đã nộp. Trước khi nộp chỉ có nút cuối trang “Nộp bài”; nút mở biểu mẫu nhập nội dung hoặc đính kèm tệp. Sau khi lưu, trang chi tiết cập nhật thông tin và đổi nút thành “Sửa bài tập” khi còn trong hạn hoặc giáo viên yêu cầu nộp lại. Hủy biểu mẫu giữ nguyên bài nộp đã lưu; nộp lại cập nhật bài làm mới.
- Đợt rà soát: đã kiểm tra điều kiện sửa bài ở SQLite ngay khi lưu, chặn tài khoản sinh viên/giáo viên không còn hoạt động, gộp xếp nhiều sinh viên trong một giao dịch, sửa thống kê chỉ đếm bài đang hoạt động, không tính bài chờ nộp lại vào tiến độ, sửa nhãn `ACTIVE`, che mật khẩu cấp tài khoản, giữ biểu mẫu khi nhập thiếu và sửa nút Back sau đăng nhập. Đã chia cột CSV thành “Điểm” và “Ưu tiên”.
- Trên máy ảo Pixel 7a đã kiểm tra đăng nhập đủ ba quyền; quản trị viên tạo thủ công một giáo viên/một sinh viên, tạo lớp và xếp sinh viên; giáo viên giao bài; sinh viên nộp rồi sửa nội dung; giáo viên xem bài và yêu cầu nộp lại; tỷ lệ tiến độ về 0/1; giáo viên chọn tệp hướng dẫn qua DocumentsUI và lưu lại. Đã sửa khoảng cách với thanh trạng thái Android, xác nhận nút Đăng xuất hoạt động sau sửa.
- `PROMPT_DU_AN.md` đã đồng bộ lại với yêu cầu mới: chỉ quản trị viên cấp tài khoản, giáo viên giao/chấm bài, sinh viên nộp/sửa bài; tính năng chưa làm được ghi riêng.
- Dữ liệu hiện lưu trong SQLite cục bộ trên một thiết bị. Chưa có máy chủ/API để đồng bộ giữa điện thoại của giáo viên và sinh viên.
- Lịch tuần lấy hạn nộp trực tiếp từ cơ sở dữ liệu nên tự phản ánh khi giáo viên sửa hạn hoặc thu hồi bài; thông báo đẩy của Android chưa triển khai.
- Các tệp chưa được Git theo dõi như `.idea/deviceManager.xml`, `.slide-build/`, `BaoCao/Bao_cao_tien_do_tuan_10.pptx`, `Docs/Bao_cao_tien_do_tuan_10.pptx` là dữ liệu cục bộ có sẵn, cần giữ nguyên và không đưa vào commit nếu không được yêu cầu.

## Việc cần làm tiếp

1. Cấu hình JDK ổn định cho Android Studio/Gradle vì JDK dùng để kiểm tra hiện nằm trong thư mục tạm.
2. Rà tiếp các trường hợp chưa thử trên thiết bị: nộp bài kèm tệp sinh viên, nộp trễ, chấm điểm 0, sửa bài sau hạn khi giáo viên yêu cầu nộp lại, khóa tài khoản và đổi giáo viên.
3. Kiểm tra nâng cấp từ cơ sở dữ liệu phiên bản 1, trùng mã lớp/thành viên và bảo vệ quản trị viên duy nhất.

## Giới hạn của bản demo, không phải việc bắt buộc

- SQLite chỉ chia sẻ dữ liệu trong cùng một bản cài ứng dụng; demo đổi tài khoản trên một thiết bị. Đồng bộ nhiều điện thoại bằng máy chủ/API hoặc Firebase là ý tưởng mở rộng.
- Mật khẩu đơn giản chỉ phù hợp dữ liệu demo. Bảo mật cho hệ thống dùng thật nằm ngoài phạm vi môn học hiện tại.
- CSV hiện chia sẻ nội dung dạng văn bản qua Intent. Checklist, thông báo nhắc hạn, lọc riêng theo lớp/môn và thống kê chi tiết theo tuần/môn chưa hoàn thiện; chỉ làm thêm nếu giảng viên yêu cầu.
