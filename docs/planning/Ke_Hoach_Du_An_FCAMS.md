# F-CAMS Simple – Báo cáo, Quy ước, Kế hoạch & Phân công

> Môn PRJ301 – Java Web (Servlet/JSP, MVC, SQL Server). Nhóm 3 người: **Front**, **Back**, **Data**.

## 1. Báo cáo tổng quan

**Mục tiêu:** Hệ thống quản lý tài sản phòng học FPT, phân quyền Admin/Staff.

**Chức năng chính (4 module):**
1. Đăng nhập / phân quyền / đăng xuất (`LoginServlet`, `LogoutServlet`, `AuthFilter`)
2. Dashboard thống kê (`DashboardServlet`, `DashboardStats`)
3. Quản lý tài sản CRUD + lọc/tìm kiếm (`AssetServlet`, `AssetDAO`)
4. Quản lý phòng học CRUD + chi tiết phòng (`RoomServlet`, `RoomDetailServlet`, `RoomDAO`)

**Công nghệ:** Java 17, Servlet/JSP (Jakarta EE 10), Tomcat 10.1, SQL Server 2022, JDBC 13.2, JSTL + custom tag, NetBeans 17.

**Kiến trúc MVC:**

| Tầng | Package / thư mục | Nội dung |
|---|---|---|
| Model | `model` | Asset, Room, RoomAsset, User, DashboardStats |
| DAL | `dal` | DBContext, AssetDAO, RoomDAO, UserDAO |
| Controller | `controller`, `filter` | 6 Servlet + AuthFilter |
| View | `web/*.jsp`, `web/includes`, `style.css`, `tag` | login, dashboard, assets, rooms, room-detail |

**Phân quyền:** Admin: toàn quyền. Staff: không được xóa tài sản, không sửa/xóa phòng.

**Nội dung báo cáo nộp (mục lục gợi ý):**
1. Giới thiệu, mục tiêu, phạm vi
2. Phân tích yêu cầu (use case, actor Admin/Staff)
3. Thiết kế (ERD, kiến trúc MVC, sơ đồ lớp, sơ đồ tuần tự đăng nhập/CRUD)
4. Thiết kế CSDL (bảng, khóa, ràng buộc)
5. Giao diện (ảnh chụp từng màn hình)
6. Kiểm thử (bảng test case)
7. Phân công & đánh giá đóng góp
8. Kết luận, hạn chế, hướng phát triển

> File hiện có `Bao_Cao_Yeu_Cau_Va_Thiet_Ke_FCAMS.doc` có thể dùng làm gốc cho mục 2–4.

## 2. Bảng quy ước chung

### 2.1 Đặt tên
| Đối tượng | Quy tắc | Ví dụ |
|---|---|---|
| Class | PascalCase | `AssetDAO`, `RoomServlet` |
| Method / biến | camelCase | `getAllAssets()` |
| Hằng số | UPPER_SNAKE | `ROLE_ADMIN` |
| Package | chữ thường | `controller`, `dal` |
| Servlet | `<Entity>Servlet`, URL chữ thường | `/assets`, `/rooms` |
| JSP | kebab-case | `room-detail.jsp` |
| Bảng SQL | PascalCase số ít/nhiều thống nhất | `Assets`, `Rooms`, `Users` |
| Cột SQL | PascalCase | `AssetId`, `RoomId` |
| CSS class | kebab-case | `.btn-primary` |

### 2.2 Code
- Indent 4 spaces, UTF-8, mỗi file 1 class public; Javadoc cho class/public method.
- DAO chỉ truy vấn DB; Servlet không viết SQL; JSP không viết scriptlet `<% %>` – dùng JSTL/EL.
- Dùng `PreparedStatement` (chống SQL Injection), đóng tài nguyên bằng try-with-resources.
- Kiểm tra quyền ở `AuthFilter` và kiểm tra lại role trong Servlet với thao tác nguy hiểm.
- Thông báo lỗi/thành công qua attribute `message` / `error`; POST xong redirect (PRG).
- Không commit mật khẩu thật: `ConnectDB.properties` dùng giá trị chung của nhóm, ghi chú trong README.

### 2.3 Git
- Nhánh: `main` (ổn định), `dev`, `feature/<tên>-<việc>` (vd `feature/front-assets-ui`).
- Commit: `<type>: <mô tả>` với type = `feat|fix|docs|style|refactor|test`.
- Không push thẳng `main`; tạo Pull Request, ít nhất 1 người review.
- Pull `dev` trước khi bắt đầu việc; commit nhỏ, thường xuyên.
- Script SQL đánh số: `01_schema.sql`, `02_seed.sql`… trong `database/`.

### 2.4 Họp & giao tiếp
- Daily sync 10 phút (chat); họp tuần 1 lần cuối tuần demo + review.
- Việc bị chặn > 1 ngày phải báo ngay trong nhóm.

## 3. Phân chia công việc

| Vai trò | Phạm vi chính | Thư mục / file phụ trách |
|---|---|---|
| **Data** (DB) | Thiết kế ERD, schema, seed data, DBContext, DAO, Model | `database/*.sql`, `dal/*`, `model/*`, `ConnectDB.properties` |
| **Back** (Controller) | Servlet, Filter, nghiệp vụ, phân quyền, validate, session | `controller/*`, `filter/AuthFilter`, `web.xml` |
| **Front** (View) | JSP, CSS, layout, custom tag, UX, validate phía client | `web/*.jsp`, `web/includes`, `style.css`, `tag/*`, `fcams.tld` |

### Chi tiết theo module
| Module | Data | Back | Front |
|---|---|---|---|
| Đăng nhập / Auth | Bảng Users, `UserDAO.login()` | `LoginServlet`, `LogoutServlet`, `AuthFilter` | `login.jsp`, header/footer |
| Dashboard | Query thống kê, `DashboardStats`, view/SP | `DashboardServlet` | `dashboard.jsp`, card/biểu đồ |
| Tài sản | Bảng Assets, `AssetDAO` (CRUD, filter, search) | `AssetServlet`, validate, check role xóa | `assets.jsp`, form thêm/sửa, bộ lọc, `AssetStatusTag` |
| Phòng học | Bảng Rooms, RoomAssets, `RoomDAO` | `RoomServlet`, `RoomDetailServlet`, check role | `rooms.jsp`, `room-detail.jsp`, `ChairStatusTag` |
| Chung | Seed data, backup script | Xử lý lỗi, encoding filter | `style.css`, `RoleNameTag`, responsive |

### Việc chung (cả nhóm)
- Cùng viết báo cáo: mỗi người viết phần của mình + review chéo.
- Cùng kiểm thử tích hợp và chuẩn bị demo/slide.

| Phần báo cáo | Người phụ trách |
|---|---|
| Giới thiệu, yêu cầu, use case | Back |
| ERD, thiết kế CSDL, từ điển dữ liệu | Data |
| Thiết kế giao diện, ảnh màn hình | Front |
| Kiểm thử, kết luận, tổng hợp & định dạng | Luân phiên / thống nhất nhóm |

## 4. Kế hoạch (4 tuần – điều chỉnh theo deadline thực tế)

| Tuần | Data | Back | Front | Mốc |
|---|---|---|---|---|
| **1** Khởi động | ERD, schema, seed data, DBContext, Model | Cấu trúc project, `AuthFilter`, Login/Logout | Layout chung, header/footer, `login.jsp`, CSS nền | Chạy được đăng nhập |
| **2** Core CRUD | `AssetDAO`, `RoomDAO` | `AssetServlet`, `RoomServlet` | `assets.jsp`, `rooms.jsp` + form | CRUD tài sản/phòng chạy |
| **3** Mở rộng | Query dashboard, `RoomAsset`, tối ưu truy vấn | `DashboardServlet`, `RoomDetailServlet`, phân quyền | `dashboard.jsp`, `room-detail.jsp`, custom tag | Đủ 4 module |
| **4** Hoàn thiện | Kiểm tra dữ liệu, script final | Fix bug, validate, xử lý lỗi | Polish UI, responsive | Báo cáo, test, demo |

### Mốc kiểm soát
| Mốc | Tiêu chí hoàn thành |
|---|---|
| M1 (cuối tuần 1) | DB dựng được từ script, login/logout + filter hoạt động |
| M2 (cuối tuần 2) | CRUD tài sản & phòng, tìm kiếm/lọc chạy |
| M3 (cuối tuần 3) | Dashboard + chi tiết phòng, phân quyền Admin/Staff đúng |
| M4 (cuối tuần 4) | 0 bug nghiêm trọng, báo cáo + slide + demo xong |

## 5. Kiểm thử & rủi ro

**Test case tối thiểu:** đăng nhập đúng/sai; Staff truy cập thao tác xóa (bị chặn); truy cập trang khi chưa login (redirect); thêm/sửa/xóa tài sản; thêm/sửa/xóa phòng; lọc theo phòng/danh mục/trạng thái; dữ liệu trống; ký tự tiếng Việt/UTF-8.

| Rủi ro | Giải pháp |
|---|---|
| Xung đột Git | Nhánh riêng theo feature, pull `dev` thường xuyên |
| Lệch schema DB giữa các máy | Một nguồn script `database/`, ai đổi schema phải thông báo |
| Lỗi font tiếng Việt | UTF-8 toàn bộ (file, `request.setCharacterEncoding`, `nvarchar`) |
| Trễ tiến độ một vai trò | Họp tuần, đổi việc hỗ trợ chéo |
| Cấu hình DB/Tomcat khác nhau | Ghi hướng dẫn cài đặt trong README |

## 6. Checklist nộp bài
- [ ] Source code chạy được trên máy sạch (kèm script DB)
- [ ] README hướng dẫn cài đặt, tài khoản demo
- [ ] Báo cáo (.doc/.pdf) đầy đủ mục lục
- [ ] Slide demo + kịch bản demo 3 vai trò
- [ ] Bảng đóng góp từng thành viên
