# F-CAMS Simple - Hệ Thống Quản Lý Tài Sản Phòng Học

> **Hệ thống Quản lý Tài sản Phòng học Trường Đại học FPT Hòa Lạc (PRJ301 - Java Web Application)**

---

## 📌 Giới thiệu Dự án
Dự án **F-CAMS Simple** (FPT Classroom Asset Management System) được xây dựng theo mô hình **MVC (Model - View - Controller)** kết hợp các tiêu chuẩn bảo mật phân quyền (RBAC), quản trị tài sản và phòng học theo yêu cầu của môn học PRJ301.

Hệ thống được thiết kế tinh gọn, tập trung hoàn toàn vào **4 tính năng cốt lõi**:
1. **Quản lý Đăng nhập & Bảo mật (Authentication & Authorization)**:
   - Đăng nhập xác thực tài khoản và phân quyền người dùng (`Admin` / `Staff`).
   - Lưu trữ phiên làm việc an toàn với `HttpSession`.
   - Bộ lọc `AuthFilter` tự động chặn truy cập trái phép vào các trang quản trị khi chưa đăng nhập và hạn chế quyền thực hiện chức năng nguy hiểm (`Xóa tài sản`, `Sửa/Xóa phòng`) đối với vai trò Staff.
   - Đăng xuất an toàn, xóa Session và chuyển hướng về trang đăng nhập.
2. **Bảng điều khiển Thống kê (Dashboard)**:
   - Hiển thị các chỉ số tổng quan: Tổng số tài sản, tổng số phòng học, giá trị tài sản ước tính (VNĐ).
   - Thống kê tỷ lệ tình trạng tài sản (`Đang hoạt động tốt`, `Đang bảo trì`, `Hỏng hóc`).
   - Danh sách tài sản mới được thêm và danh sách tài sản cần bảo trì khẩn cấp.
3. **Quản lý Tài sản (Asset Management - CRUD)**:
   - Danh sách & Tìm kiếm: Liệt kê toàn bộ tài sản kèm bộ lọc theo Phòng học, Danh mục, Trạng thái và tìm kiếm theo Tên hoặc Mã tài sản.
   - Thêm mới: Ghi nhận tài sản mới với đầy đủ thông số (Mã, Tên, Số serial, Ngày bàn giao, Đơn giá, Phòng bố trí).
   - Cập nhật: Sửa thông tin tài sản, chuyển dời tài sản giữa các phòng học.
   - Xóa: Xóa bản ghi tài sản (chỉ dành cho quyền `Admin`).
4. **Quản lý Phòng học (Room Management - CRUD & Room Detail)**:
   - Danh mục phòng học: Quản lý danh sách các phòng học của trường (Mã phòng, Tòa nhà, Tầng, Số chỗ ngồi, Mục đích sử dụng).
   - Thêm / Sửa / Xóa phòng: Cập nhật thông tin phòng học (Sửa/Xóa chỉ dành cho quyền `Admin`).
   - Xem chi tiết phòng (`Room Detail`): Xem toàn bộ các tài sản và trang thiết bị hiện đang được trang bị bên trong phòng học được chọn.

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
Cơ sở dữ liệu gồm 4 bảng chuẩn hóa:
1. `users`: Tài khoản và vai trò (`ADMIN`, `STAFF`).
2. `rooms`: Danh mục phòng học, sức chứa và mục đích sử dụng.
3. `assets`: Danh mục tài sản, số serial, đơn giá, ngày bàn giao, phòng bố trí, trạng thái (`ACTIVE`, `MAINTENANCE`, `BROKEN`).
4. `room_assets`: Danh mục thiết bị cố định trang bị trong từng phòng học.

*Script khởi tạo và nạp dữ liệu mẫu nằm tại:* `DATASQL/fcams_simple.sql`

---

## 🚀 Hướng dẫn Cài đặt & Chạy trên NetBeans

### 1. Khởi tạo Cơ sở Dữ liệu
1. Mở **SQL Server Management Studio (SSMS)** hoặc công cụ quản trị SQL Server.
2. Chạy toàn bộ nội dung file `DATASQL/fcams_simple.sql`.
3. Kiểm tra cấu hình kết nối trong file: `src/java/dal/DBContext.java`:
   - URL: `jdbc:sqlserver://localhost:1433;databaseName=FCAMS_DB;encrypt=false;trustServerCertificate=true`
   - User: `sa`
   - Password: `123` *(thay đổi theo cấu hình máy của bạn nếu cần)*

### 2. Mở dự án trong NetBeans 17
1. Mở **NetBeans IDE 17** -> Chọn **File** -> **Open Project...**
2. Trỏ đến thư mục `FCAMS-Simple`.
3. Nhấp chuột phải vào dự án `FCAMS-Simple` -> Chọn **Clean and Build**.

### 3. Chạy Dự án trên Tomcat 10.1
1. Nhấp chuột phải vào dự án -> Chọn **Run** (hoặc nhấn `F6`).
2. Chọn Server: **Apache Tomcat 10.1**.
3. Đường dẫn truy cập mặc định: `http://localhost:8080/FCAMS-Simple/login`

---

## 👥 Tài khoản Kiểm thử Mặc định

| Username | Password | Vai trò | Quyền hạn |
| :--- | :--- | :--- | :--- |
| `admin` | `123` | **Quản trị viên (Admin)** | Toàn quyền thêm, sửa, xóa tài sản & phòng học |
| `it_beta` | `123` | **Nhân viên (Staff)** | Xem, tìm kiếm, thêm/sửa tài sản; Bị chặn xóa tài sản và sửa/xóa phòng học |
| `hungnq` | `123` | **Nhân viên (Staff)** | Xem, tìm kiếm tài sản và phòng học |

---

## 📂 Cấu trúc Thư mục Nguồn
```text
FCAMS-Simple/
├── DATASQL/
│   └── fcams_simple.sql         # Script SQL Server 2022
├── src/
│   └── java/
│       ├── controller/          # Servlet điều hướng (Login, Dashboard, Asset, Room, RoomDetail, Logout)
│       ├── dal/                 # Data Access Layer (DBContext, UserDAO, AssetDAO, RoomDAO)
│       ├── filter/              # AuthFilter phân quyền & bảo vệ endpoint
│       ├── model/               # Java Beans (User, Asset, Room, RoomAsset, DashboardStats)
│       └── tag/                 # Custom JSTL tags định dạng dữ liệu
├── web/
│   ├── includes/                # Header (Home | Tài sản | Phòng học) & Footer
│   ├── WEB-INF/                 # web.xml, tlds, lib (.jar JSTL, JDBC)
│   ├── assets.jsp               # Quản lý tài sản (CRUD & Bộ lọc)
│   ├── dashboard.jsp            # Bảng điều khiển thống kê
│   ├── login.jsp                # Trang đăng nhập bảo mật
│   ├── room-detail.jsp          # Chi tiết thiết bị trong phòng
│   ├── rooms.jsp                # Quản lý phòng học
│   └── style.css                # Giao diện CSS chuẩn HTML/JSP
├── build.xml                    # Ant build script cho NetBeans
└── README.md
```
