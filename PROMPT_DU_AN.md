# Prompt triển khai App quản lý bài tập sinh viên

Xây dựng đồ án Android bằng Java, XML và SQLite, giao diện tiếng Việt, tông hồng và tím theo thiết kế Figma của nhóm: https://www.figma.com/design/cp51Sh3FmT73Y0S7OCX72q?node-id=3-2. Ưu tiên Activity, ListView, Adapter, Intent, biểu mẫu và CRUD đã học. Demo ba vai trò trên cùng một thiết bị vì dữ liệu hiện lưu cục bộ.

## Đăng nhập và phân quyền

Chỉ có màn hình đăng nhập bằng email và mật khẩu. Không có đăng ký công khai và không có lối vào demo chọn vai trò. Quản trị viên cấp tài khoản giáo viên, sinh viên qua biểu mẫu; không có nút tạo hàng loạt tài khoản mẫu. Sau đăng nhập, đọc vai trò từ tài khoản trong SQLite: ADMIN vào trang quản trị, TEACHER vào trang giáo viên, STUDENT vào trang sinh viên. Tài khoản bị khóa hoặc vai trò không hợp lệ không được truy cập. Đăng xuất xóa phiên và quay về màn hình chào.

## Chức năng theo vai trò

- **Quản trị viên:** cấp tài khoản giáo viên/sinh viên, khóa/mở tài khoản, đặt lại mật khẩu; tạo lớp học phần với mã duy nhất và chọn giáo viên phụ trách; thêm nhiều sinh viên trong một lần, tránh thành viên trùng; gỡ sinh viên, đổi giáo viên và lưu trữ lớp. Lớp lưu trữ giữ lịch sử và không còn nhận bài mới.
- **Giáo viên:** xem các lớp đang phụ trách; giao bài với tên, yêu cầu, hạn nộp và tệp hướng dẫn tùy chọn; sửa hoặc thu hồi bài; xem từng sinh viên, trạng thái và tỷ lệ nộp; mở bài nộp, chấm điểm 0–10, nhận xét hoặc yêu cầu nộp lại. Khi quản trị viên đổi giáo viên, quyền thao tác lớp chuyển sang người mới.
- **Sinh viên:** xem lớp đã được xếp và bài giáo viên giao. Đề bài, hạn và tệp hướng dẫn chỉ đọc. Trong chi tiết bài có một nút Nộp bài ở cuối màn hình; nút mở biểu mẫu nhập nội dung hoặc chọn tệp. Sau khi nộp, chi tiết hiển thị trạng thái, thời gian, nội dung/tệp, điểm và nhận xét. Nút đổi thành Sửa bài tập khi còn trong hạn hoặc giáo viên yêu cầu nộp lại. Sinh viên cũng quản lý việc cá nhân, xem lịch tuần, thống kê và chia sẻ dữ liệu CSV của chính mình.

## Dữ liệu và quy tắc

Các bảng SQLite chính: tai_khoan, lop_hoc_phan, thanh_vien_lop, bai_tap, bai_nop, bai_ca_nhan. Khóa duy nhất ngăn trùng email, mã tài khoản, mã lớp và cặp lớp–sinh viên; cặp bài–sinh viên có một bài nộp hiện hành. Kiểm tra quyền dựa trên ID tài khoản đang đăng nhập, vai trò, lớp đang hoạt động và quan hệ thành viên. Tệp đính kèm lưu URI và tên, được chọn qua trình chọn tài liệu của Android. Chỉ bài nộp của chính sinh viên được sửa; nội dung giáo viên giao luôn chỉ đọc đối với sinh viên.

Lần nộp đầu có thể bị đánh dấu nộp trễ. Sau khi đã nộp, sinh viên chỉ sửa trước hạn hoặc khi trạng thái là Cần nộp lại. Khi sửa, nội dung/tệp và thời gian nộp được cập nhật; điểm cũ được xóa để giáo viên chấm lại. Bài thu hồi và lớp lưu trữ không xuất hiện trong danh sách bài đang hoạt động. Báo cáo giáo viên không hiển thị việc riêng của sinh viên. Thống kê và CSV của sinh viên chỉ lấy dữ liệu tài khoản hiện tại.

## Sáu phần công việc

| Phần | Phụ trách | Bàn giao |
|---|---|---|
| 1 | Tài khoản và đăng nhập | SQLite tài khoản, tài khoản quản trị ban đầu, cấp/khóa/mở/đặt lại mật khẩu, kiểm tra phiên và mở đúng trang theo vai trò. |
| 2 | Quản lý lớp | Tạo lớp, chặn trùng mã, đổi giáo viên, chọn nhiều sinh viên, gỡ thành viên và lưu trữ lớp; kiểm tra quyền theo lớp. |
| 3 | Giao bài giáo viên | Danh sách lớp/bài của giáo viên, tạo/sửa/thu hồi bài, chọn và mở tệp hướng dẫn. |
| 4 | Theo dõi và chấm bài | Danh sách sinh viên, tiến độ theo bài, chi tiết bài nộp, điểm, nhận xét và yêu cầu nộp lại. |
| 5 | Bài tập sinh viên | Danh sách lớp/bài, chi tiết chỉ đọc, nộp nội dung/tệp, xem trạng thái và sửa bài nộp đúng điều kiện. |
| 6 | Việc riêng và báo cáo sinh viên | CRUD việc cá nhân, tìm kiếm/lọc/sắp xếp, lịch tuần, thống kê và CSV đúng phạm vi tài khoản. |

Mỗi phần tự kiểm tra trường hợp thành công, dữ liệu rỗng và quyền truy cập sai. Cuối cùng ghép trên một lớp demo: quản trị viên cấp hai tài khoản và xếp lớp; giáo viên giao bài; sinh viên nộp/sửa; giáo viên chấm hoặc yêu cầu nộp lại; sinh viên xem kết quả.

## Phạm vi phù hợp với đồ án môn học

- Mục tiêu là demo trọn luồng ba vai trò trên **một thiết bị** bằng Java, XML và SQLite. Cách lưu mật khẩu hiện tại chỉ dùng cho dữ liệu demo của đồ án. Khi trình bày cần nói rõ đây là bản học tập.
- Máy chủ/API hoặc Firebase để đồng bộ nhiều điện thoại, xác thực cho sản phẩm thật và thông báo Android tự động **không thuộc phạm vi bắt buộc** của đồ án này.
- Checklist, nhắc hạn, lọc riêng theo môn/lớp và thống kê chi tiết theo môn/tuần là ý tưởng mở rộng từ yêu cầu cũ. Chỉ bổ sung nếu giảng viên yêu cầu và nhóm còn thời gian; không xem chúng là lỗi của bản demo cơ bản.
- CSV hiện được chia sẻ ở dạng nội dung qua Intent. Nếu cần tệp `.csv` thật, bổ sung sau khi các luồng chính ổn định.
- Xem trạng thái thực tế và các tình huống còn cần kiểm tra trong TIEN_DO_DU_AN.md.
