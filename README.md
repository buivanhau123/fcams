# F-CAMS Simple - Hệ Thống Quản Lý Tài Sản Phòng Học

> **Hệ thống Quản lý Tài sản Phòng học Trường Đại học FPT Hòa Lạc (PRJ301 - Java Web Application)**

---

## 📌 Giới thiệu Dự án
Dự án **F-CAMS Simple** (FPT Classroom Asset Management System) được xây dựng theo mô hình **MVC (Model - View - Controller)** kết hợp các tiêu chuẩn bảo mật phân quyền (RBAC), quản trị tài sản, phòng học, báo cáo hỏng hóc cho giảng viên và nhật ký lịch sử thay đổi theo yêu cầu của môn học PRJ301.

Hệ thống tập trung vào **5 tính năng cốt lõi**:
1. **Quản lý Đăng nhập & Bảo mật Phân quyền (Authentication & Authorization)**:
   - Đăng nhập xác thực tài khoản và phân quyền 3 vai trò: `ADMIN` (Quản trị viên), `STAFF` (Cán bộ Quản lý), `LECTURER` (Giảng viên).
   - Lưu trữ phiên làm việc an toàn với `HttpSession`.
   - Bộ lọc `AuthFilter` tự động chặn truy cập trái phép:
     - **Giảng viên (`LECTURER`)**: Xem tình trạng các đồ vật/thiết bị trong các phòng học và gửi **Báo cáo đồ hỏng hóc** cho Staff. Không được Thêm/Sửa/Xóa/Chuyển tài sản, không được Sửa/Xóa phòng và không được xem trang Lịch sử.
     - **Cán bộ Quản lý (`STAFF`)**: Quản lý tài sản (Xem, Thêm, Sửa, Chuyển phòng, Xử lý báo hỏng), Xem phòng học và Xem Lịch sử thay đổi. Bị hạn chế quyền `Xóa tài sản` và `Thêm/Sửa/Xóa phòng`.
     - **Quản trị viên (`ADMIN`)**: Toàn quyền hệ thống.
2. **Bảng điều khiển Thống kê (Dashboard)**:
   - Hiển thị các chỉ số tổng quan: Tổng số tài sản, tổng số phòng học, giá trị tài sản ước tính (VNĐ).
   - Thống kê tỷ lệ tình trạng tài sản (`Hoạt động tốt`, `Đang bảo trì`, `Hỏng hóc`).
   - Danh sách tài sản mới được thêm và danh sách tài sản cần bảo trì khẩn cấp.
3. **Quản lý Tài sản & Báo cáo Đồ Hỏng hóc (Asset Management & Damage Reporting)**:
   - Danh sách & Tìm kiếm: Liệt kê toàn bộ tài sản/đồ vật kèm bộ lọc theo Phòng học, Danh mục, Trạng thái và tìm kiếm theo Tên hoặc Mã tài sản.
   - **Báo cáo đồ hỏng hóc (Dành cho Giảng viên / Staff / Admin)**: Gửi báo cáo sự cố đồ vật trong phòng cho Staff xử lý, tự động cập nhật trạng thái thiết bị sang `Hỏng hóc (FAULTY)` và lưu vào Lịch sử thay đổi.
   - Thêm mới / Cập nhật / Chuyển phòng (`Admin` & `Staff`) và Xóa tài sản (chỉ `Admin`).
4. **Quản lý Phòng học (Room Management - CRUD & Room Detail)**:
   - Danh mục phòng học: Quản lý danh sách các phòng học của trường (Mã phòng, Tòa nhà, Tầng, Số chỗ ngồi, Mục đích sử dụng).
   - Thêm / Sửa / Xóa phòng: Chỉ dành cho quyền `Admin`.
   - Xem chi tiết phòng (`Room Detail`): Xem tình trạng toàn bộ các đồ vật/trang thiết bị bên trong phòng học và gửi phiếu báo cáo đồ hỏng ngay tại phòng.
5. **Nhật ký Lịch sử Thay đổi (Change History - Chỉ dành cho `Admin` & `Staff`)**:
   - Ghi nhận tự động mọi thay đổi trên hệ thống: Thêm/Sửa/Xóa/Chuyển phòng tài sản, Thêm/Sửa/Xóa phòng học và Báo cáo đồ hỏng hóc từ Giảng viên.
   - Cho phép lọc theo Loại thao tác, Phòng học, Vai trò người thực hiện và Tìm kiếm từ khóa, kèm nút chuyển nhanh sang xử lý thiết bị.

---

## 🛠️ Công nghệ Sử dụng
- **Ngôn ngữ:** Java 17 (JDK 17 LTS)
- **Kiến trúc:** Mô hình MVC (Model - View - Controller), Servlet & JSP, Tag Library (Custom JSTL Tags)
- **Web Server:** Apache Tomcat 10.1 (Jakarta EE 10 / Servlet 6.0)
- **Cơ sở dữ liệu:** Microsoft SQL Server 2022 (T-SQL)
- **JDBC Driver:** Microsoft JDBC Driver 13.2.0 for SQL Server
- **IDE hỗ trợ:** Apache NetBeans IDE 17

---

## 🗄️ Cấu trúc Cơ sở Dữ liệu (`FCAMS_DB`)
Cơ sở dữ liệu gồm 5 bảng chuẩn hóa:
1. `users`: Tài khoản và vai trò (`ADMIN`, `STAFF`, `LECTURER`).
2. `rooms`: Danh mục phòng học, sức chứa và mục đích sử dụng.
3. `assets`: Danh mục tài sản, số serial, đơn giá, ngày bàn giao, phòng bố trí, trạng thái (`ACTIVE`, `REPAIRING`, `FAULTY`).
4. `room_assets`: Danh mục thiết bị cố định trang bị trong từng phòng học.
5. `change_history`: Nhật ký lịch sử thay đổi hệ thống và báo cáo hỏng hóc thiết bị.

*Script khởi tạo và nạp dữ liệu mẫu nằm tại:* `database/fcams_simple.sql`

---

## 👥 Tài khoản Kiểm thử Mặc định

| Username | Password | Vai trò | Quyền hạn |
| :--- | :--- | :--- | :--- |
| `admin` | `123` | **Quản trị viên (`ADMIN`)** | Toàn quyền thêm, sửa, xóa tài sản & phòng học, xem Lịch sử thay đổi |
| `it_beta` | `123` | **Cán bộ Quản lý (`STAFF`)** | Xem, tìm kiếm, thêm/sửa/chuyển phòng tài sản, xử lý báo hỏng, xem Lịch sử thay đổi; Bị chặn xóa tài sản và sửa/xóa phòng học |
| `hungnq` | `123` | **Giảng viên (`LECTURER`)** | Xem danh sách phòng học, xem tình trạng các đồ vật trong phòng, gửi Báo cáo đồ hỏng hóc cho Staff; Không thấy tab Lịch sử và không được chỉnh sửa/xóa dữ liệu |
