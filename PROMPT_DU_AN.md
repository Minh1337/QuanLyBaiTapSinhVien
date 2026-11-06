# Prompt triển khai App quản lý bài tập sinh viên

Xây dựng đồ án Android bằng Java, XML và SQLite cho nhóm 6 người trong 4 tuần. Giao diện tiếng Việt. Ba tài khoản trên cùng một thiết bị lần lượt demo việc xếp lớp, giao bài và cập nhật tiến độ. Mỗi lớp học phần đại diện cho một môn trong một học kỳ, có một giáo viên và nhiều sinh viên. Một sinh viên có thể học nhiều lớp.

## Giao diện và điều hướng

Dùng thiết kế Figma của nhóm làm mẫu. [Mở file thiết kế](https://www.figma.com/design/cp51Sh3FmT73Y0S7OCX72q?node-id=3-2). Frame mẫu 390 × 800, Roboto, nền #FBF5FA, nút tím #713E87, khối nhấn hồng #F6DFEC, chữ #382841. Dùng nhãn chữ cho trạng thái, cho phép cuộn và hiển thị lỗi ngay cạnh ô nhập.

Dùng một màn hình đăng nhập chung chỉ yêu cầu tên đăng nhập và mật khẩu. Tự đăng ký luôn tạo tài khoản có role STUDENT. Sau khi xác minh mật khẩu và trạng thái hoạt động, đọc role của bản ghi tài khoản trong SQLite. ADMIN mở Quản trị ứng dụng, TEACHER mở Lớp giảng dạy, STUDENT mở Trang chủ sinh viên. Dùng cùng quy tắc khi khôi phục phiên. Với tài khoản bị khóa hoặc role không hợp lệ, xóa phiên và giữ người dùng ở Đăng nhập. Không nhận role từ biểu mẫu hoặc tham số điều hướng.

## Ba khu quản lý

- **Quản trị viên** quản lý tài khoản và lớp học phần. Tạo tài khoản giáo viên, khóa/mở tài khoản giáo viên hoặc sinh viên, tạo lớp, chọn một giáo viên phụ trách và thêm/bớt sinh viên đã đăng ký. Chặn trùng mã lớp và thành viên. Không cho tự khóa quản trị viên duy nhất. Khi đổi giáo viên, quyền xem và giao bài chuyển sang giáo viên mới. Lưu lớp đã dùng ở trạng thái lưu trữ thay vì xóa mất lịch sử.
- **Giáo viên** thấy các lớp mình phụ trách, tạo bài với tên, yêu cầu và hạn nộp, sửa bài của lớp, xem danh sách sinh viên cùng tiến độ từng người. Có thể thu hồi bài sau khi xác nhận. Đổi hạn hoặc thu hồi phải cập nhật lịch nhắc liên quan. Báo cáo thuộc phạm vi lớp và chỉ hiển thị trạng thái, phần trăm tiến độ của bài được giao. Việc riêng của sinh viên chỉ sinh viên đó quản lý.
- **Sinh viên** xem lớp mình tham gia và bài giáo viên giao. Nội dung, yêu cầu và hạn giao ở chế độ chỉ đọc. Sinh viên tự quản lý checklist, trạng thái, mức ưu tiên và nhắc hạn của mình. Giữ chức năng tạo môn và bài cá nhân, thêm/sửa/xóa, tìm theo tên, lọc theo môn, lớp hoặc trạng thái, sắp theo hạn hoặc ưu tiên. Gắn nhãn “Cá nhân” và “Giáo viên giao” để phân biệt hai nguồn bài. Lịch tuần, thống kê theo môn/tuần và CSV chỉ chứa dữ liệu của sinh viên đang đăng nhập. Hoàn thành là trạng thái tự báo cáo tiến độ.

## Tài khoản và dữ liệu

Trong lần thiết lập đầu tiên trên máy demo, người phụ trách tạo quản trị viên duy nhất và đặt mật khẩu. Sau đó đóng luồng thiết lập. Đăng ký sinh viên yêu cầu tên đăng nhập duy nhất, mật khẩu ít nhất 8 ký tự và xác nhận trùng khớp. Chuẩn hóa tên bằng cách bỏ khoảng trắng đầu/cuối và so sánh không phân biệt hoa thường. Lưu giá trị băm mật khẩu bằng PBKDF2 với salt ngẫu nhiên riêng. Không ghi mật khẩu vào log. Chỉ lưu ID tài khoản cho phiên. Kiểm tra trạng thái hoạt động và vai trò khi khôi phục phiên, mở màn hình và thực hiện thao tác dữ liệu. Đăng xuất thì xóa phiên, lịch sử điều hướng và các nhắc hạn đang hiển thị hoặc đang chờ của tài khoản đó.

Mô hình SQLite tối thiểu gồm tài khoản (vai trò và trạng thái), lớp (giáo viên phụ trách), thành viên lớp (khóa duy nhất lớp + sinh viên), môn cá nhân (chủ sở hữu), bài tập, tiến độ từng sinh viên và mục checklist. Mỗi bài thuộc đúng một lớp hoặc một môn cá nhân. Tiến độ lưu theo cặp bài + sinh viên, gồm trạng thái, ưu tiên và nhắc hạn. Mỗi mục checklist thuộc đúng một bản ghi tiến độ. Với bài lớp chưa có tiến độ cá nhân, hiển thị Chưa làm. Thêm thành viên thì cho xem các bài đang giao của lớp. Rời lớp thì ngừng truy cập và nhắc hạn của lớp đó.

Kiểm tra quyền ngay trong lớp đọc/ghi dữ liệu theo ID phiên và quan hệ lớp/chủ sở hữu. Áp dụng cả khi truy cập bằng ID bài, mở thông báo, thống kê và xuất CSV. Dùng khóa ngoại và giao dịch cho thao tác nhiều bảng. Không cho tự chọn vai trò lúc đăng ký hoặc dựa vào tham số màn hình để cấp quyền.

## Sáu phần bàn giao

| Thành viên | Màn hình và phần phụ trách | Công việc bàn giao | Kiểm tra khi ghép |
|---|---|---|---|
| 1 | Đăng ký, đăng nhập và phân quyền | Khởi tạo quản trị viên, tự đăng ký sinh viên, xác thực, đăng xuất và khôi phục phiên. Đọc role từ tài khoản để mở đúng trang. Bàn giao cấu trúc SQLite và hàm kiểm tra quyền dùng chung. | Ba tài khoản mở đúng ba trang mà không chọn vai trò. Tài khoản bị khóa không khôi phục được phiên. Sinh viên mở chức năng quản trị bằng ID bị từ chối. |
| 2 | Quản trị ứng dụng, tài khoản và lớp học phần | Tạo giáo viên, khóa/mở tài khoản, bảo vệ quản trị viên duy nhất. Tạo lớp, chặn trùng mã, đổi giáo viên, thêm/bớt sinh viên đã đăng ký, chặn thành viên trùng. Lưu trữ lớp đã dùng và giữ lịch sử. | Đổi giáo viên MOB01: người cũ mất quyền, người mới xem và giao được bài. Thêm sinh viên hai lần chỉ tạo một thành viên. Lưu trữ lớp vẫn xem được lịch sử. |
| 3 | Lớp giảng dạy, giao bài và báo cáo lớp | Hiện lớp phụ trách và danh sách sinh viên. Tạo/sửa tên, yêu cầu, hạn nộp; xác nhận trước khi thu hồi. Hiện trạng thái và phần trăm tiến độ theo từng sinh viên, từng bài lớp. Báo thay đổi hạn hoặc thu hồi cho phần 5. | Giáo viên thấy A đạt 50%, B chưa làm. Bài riêng của A không xuất hiện trong báo cáo. Hủy xác nhận thu hồi thì bài vẫn còn. |
| 4 | Lớp của tôi, môn cá nhân, danh sách và chi tiết bài | Quản lý môn và bài cá nhân; đọc bài giáo viên giao. Gắn nhãn hai nguồn bài, tìm theo tên, lọc theo môn/lớp/trạng thái, sắp theo hạn/ưu tiên. Gắn phần tiến độ của người 5 vào chi tiết bài. | A sửa/xóa được bài cá nhân sau xác nhận xóa. Với bài lớp, A xem được yêu cầu và hạn nhưng không sửa được hai trường này, kể cả truy cập bằng ID. |
| 5 | Checklist, tiến độ của tôi, ưu tiên, lịch tuần và nhắc hạn | Lưu tùy chọn theo cặp bài + sinh viên. Tính tiến độ từ checklist, lưu trạng thái tự báo cáo độc lập. Cập nhật hoặc hủy nhắc khi đổi hạn, thu hồi, rời lớp, hoàn thành hoặc đăng xuất. | A tích 1/2 mục: trang A và báo cáo giáo viên cùng hiện 50%, B giữ tiến độ riêng. Đổi hạn không tạo nhắc trùng; thu hồi bài thì dừng nhắc. |
| 6 | Trang chủ sinh viên “Việc của tôi”, thống kê và CSV | Hiện bài gần hạn, hai nguồn bài và lối vào lớp/lịch/thống kê. Thống kê theo môn/tuần và xuất CSV cho tài khoản hiện tại. Chuẩn bị dữ liệu và điều phối buổi kiểm thử xuyên suốt ba vai trò. | Đối chiếu trang chủ, thống kê và CSV của A với dữ liệu mẫu, sau đó đổi sang B. Kiểm tra CSV tiếng Việt và tên bài có dấu phẩy. |

**Cách phối hợp.** Tuần 1, người 1 chốt ID tài khoản, lớp và bài cùng cả nhóm. Người 2 cập nhật phân công/thành viên; người 3 và 4 kiểm tra lại quyền trên dữ liệu mới. Người 3 chuyển thay đổi hạn/thu hồi cho người 5; người 5 cung cấp cùng một kết quả tiến độ cho người 3, 4 và 6. Mỗi người tự viết truy vấn, kiểm thử và sửa lỗi của phần mình. Cuối tuần 2, demo bằng cùng một lớp mẫu để điều chỉnh khối lượng trước khi ghép.

Checklist tùy chọn. Khi có mục thì tính số mục đã xong chia tổng số mục. Khi rỗng chỉ hiện trạng thái. Ba trạng thái là Chưa làm, Đang làm, Hoàn thành. Nhãn Quá hạn được tính từ hạn đã qua và bài chưa hoàn thành. Dùng chung quy tắc cho danh sách, lịch và thống kê.

## Nghiệm thu

Tuần 1 chốt dữ liệu, thiết kế và phân quyền. Tuần 2 làm sáu phần song song. Tuần 3 ghép và kiểm thử chéo vai trò. Tuần 4 sửa lỗi, build APK debug, làm báo cáo và demo trên một thiết bị.

1. Admin tạo giáo viên, tạo lớp MOB01 và thêm hai sinh viên A, B. Một sinh viên C ngoài lớp và giáo viên lớp khác đều không mở được bài MOB01 bằng ID.
2. Giáo viên giao một bài. A và B cùng thấy bài đó. A đánh dấu một trong hai mục checklist, giáo viên thấy tiến độ A là 50% và B vẫn Chưa làm. B không sửa được tiến độ A. A không sửa được hạn do giáo viên đặt.
3. A tạo một bài riêng, thử lọc, xem lịch và xuất CSV. Kiểm tra giáo viên và B không thấy bài riêng của A. Đăng xuất, thử nút Quay lại, chuyển tài khoản và mở một thông báo cũ.
4. Admin khóa tài khoản, đổi giáo viên hoặc bỏ thành viên khỏi lớp. Kiểm tra quyền truy cập, dữ liệu thống kê và nhắc hạn sau thay đổi. Thử khôi phục phiên, từ chối quyền thông báo, đăng ký trùng tên và đăng nhập sai.
5. Bàn giao mã, APK, hướng dẫn chạy, phân công, kết quả kiểm thử và kịch bản demo. Ghi rõ thao tác đã chạy trên thiết bị và mục còn chưa xác minh.
