# -*- coding: utf-8 -*-
"""Sinh Bao_Cao_FCAMS.docx, Quy_Uoc_Chung.docx và Ke_Hoach_Phan_Cong_FCAMS.xlsx."""
import os
from docx import Document
from docx.shared import Pt
from docx.enum.text import WD_ALIGN_PARAGRAPH
from openpyxl import Workbook
from openpyxl.styles import Font, PatternFill, Alignment, Border, Side
from openpyxl.utils import get_column_letter
from openpyxl.worksheet.datavalidation import DataValidation

OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..")
TEAM = ["Front", "Back", "Data"]


# ---------------------------------------------------------------- WORD
def new_doc():
    d = Document()
    st = d.styles["Normal"]
    st.font.name = "Times New Roman"
    st.font.size = Pt(13)
    return d


def table(d, header, rows):
    t = d.add_table(rows=1, cols=len(header))
    t.style = "Table Grid"
    for i, h in enumerate(header):
        c = t.rows[0].cells[i]
        c.text = ""
        c.paragraphs[0].add_run(h).bold = True
    for r in rows:
        cells = t.add_row().cells
        for i, v in enumerate(r):
            cells[i].text = v
    d.add_paragraph()


def cover(d, title, sub):
    for t, size in [("TRƯỜNG ĐẠI HỌC FPT", 14), (title, 22), (sub, 14)]:
        p = d.add_paragraph()
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r = p.add_run(t)
        r.bold = True
        r.font.size = Pt(size)
    d.add_paragraph("\nMôn học: PRJ301 – Java Web Application\n"
                    "Giảng viên hướng dẫn: ..............................\n"
                    "Nhóm thực hiện: ..............................\n"
                    "Thành viên:\n  1. ............ (Front)\n  2. ............ (Back)\n  3. ............ (Data)\n"
                    "Học kỳ: ........  Năm học: ........")
    d.add_page_break()


def report():
    d = new_doc()
    cover(d, "BÁO CÁO DỰ ÁN F-CAMS SIMPLE",
          "Hệ thống quản lý tài sản phòng học")

    d.add_heading("MỤC LỤC", 1)
    for s in ["1. Giới thiệu", "2. Phân tích yêu cầu", "3. Thiết kế hệ thống",
              "4. Thiết kế cơ sở dữ liệu", "5. Thiết kế giao diện", "6. Kiểm thử",
              "7. Phân công và đóng góp", "8. Kết luận", "Phụ lục"]:
        d.add_paragraph(s)
    d.add_page_break()

    d.add_heading("1. Giới thiệu", 1)
    d.add_heading("1.1 Bối cảnh và mục tiêu", 2)
    d.add_paragraph("F-CAMS (FPT Classroom Asset Management System) giúp quản lý tài sản "
                    "và phòng học của trường, thay thế việc ghi chép thủ công. Bản Simple "
                    "tập trung vào 4 chức năng cốt lõi theo yêu cầu môn PRJ301.")
    d.add_heading("1.2 Công nghệ", 2)
    table(d, ["Hạng mục", "Công nghệ"], [
        ["Ngôn ngữ", "Java 17"],
        ["Kiến trúc", "MVC, Servlet, JSP, JSTL, Custom Tag"],
        ["Web server", "Apache Tomcat 10.1 (Jakarta EE 10)"],
        ["CSDL", "Microsoft SQL Server 2022"],
        ["JDBC", "mssql-jdbc 13.2.0"],
        ["IDE", "Apache NetBeans 17"],
    ])

    d.add_heading("2. Phân tích yêu cầu", 1)
    d.add_heading("2.1 Tác nhân", 2)
    table(d, ["Tác nhân", "Mô tả"], [
        ["Admin", "Toàn quyền quản lý tài sản, phòng học"],
        ["Staff", "Xem, thêm, sửa tài sản; không được xóa tài sản, không sửa/xóa phòng"],
    ])
    d.add_heading("2.2 Yêu cầu chức năng", 2)
    table(d, ["Mã", "Chức năng", "Mô tả", "Quyền"], [
        ["FR01", "Đăng nhập", "Xác thực tài khoản, lưu HttpSession", "Tất cả"],
        ["FR02", "Đăng xuất", "Hủy session, về trang đăng nhập", "Tất cả"],
        ["FR03", "Dashboard", "Tổng tài sản, tổng phòng, tổng giá trị, tỷ lệ tình trạng, tài sản mới, cần bảo trì", "Tất cả"],
        ["FR04", "Danh sách và tìm kiếm tài sản", "Lọc theo phòng, danh mục, trạng thái; tìm theo tên/mã", "Tất cả"],
        ["FR05", "Thêm tài sản", "Mã, tên, serial, ngày bàn giao, đơn giá, phòng", "Tất cả"],
        ["FR06", "Sửa tài sản", "Cập nhật thông tin, chuyển phòng", "Tất cả"],
        ["FR07", "Xóa tài sản", "Xóa bản ghi tài sản", "Admin"],
        ["FR08", "Danh sách phòng", "Mã phòng, tòa nhà, tầng, số chỗ, mục đích", "Tất cả"],
        ["FR09", "Thêm phòng", "Thêm phòng học mới", "Admin"],
        ["FR10", "Sửa/Xóa phòng", "Cập nhật, xóa phòng", "Admin"],
        ["FR11", "Chi tiết phòng", "Xem tài sản/thiết bị trong phòng", "Tất cả"],
    ])
    d.add_paragraph("(Nhóm đối chiếu lại bảng này với code thực tế và chỉnh sửa nếu cần.)")
    d.add_heading("2.3 Yêu cầu phi chức năng", 2)
    for s in ["Bảo mật: AuthFilter chặn truy cập khi chưa đăng nhập; PreparedStatement chống SQL Injection.",
              "Hỗ trợ tiếng Việt (UTF-8) toàn hệ thống.",
              "Giao diện thân thiện, dễ dùng.",
              "Mã nguồn theo MVC, dễ bảo trì."]:
        d.add_paragraph(s, style="List Bullet")
    d.add_paragraph("[Chèn sơ đồ Use Case tại đây]")

    d.add_heading("3. Thiết kế hệ thống", 1)
    d.add_heading("3.1 Kiến trúc MVC", 2)
    table(d, ["Tầng", "Thành phần", "Trách nhiệm"], [
        ["Model", "Asset, Room, RoomAsset, User, DashboardStats", "Đối tượng dữ liệu"],
        ["DAL", "DBContext, AssetDAO, RoomDAO, UserDAO", "Truy cập CSDL"],
        ["Controller", "LoginServlet, LogoutServlet, DashboardServlet, AssetServlet, RoomServlet, RoomDetailServlet, AuthFilter", "Điều phối, nghiệp vụ, phân quyền"],
        ["View", "login, dashboard, assets, rooms, room-detail (.jsp), style.css, custom tag", "Hiển thị"],
    ])
    d.add_heading("3.2 Sơ đồ", 2)
    for s in ["[Chèn sơ đồ lớp]", "[Chèn sơ đồ tuần tự: đăng nhập]", "[Chèn sơ đồ tuần tự: thêm tài sản]"]:
        d.add_paragraph(s)

    d.add_heading("4. Thiết kế cơ sở dữ liệu", 1)
    d.add_paragraph("[Chèn ERD tại đây]")
    d.add_heading("4.1 Từ điển dữ liệu", 2)
    table(d, ["Bảng", "Cột", "Kiểu", "Ràng buộc", "Mô tả"],
          [["........", "........", "........", "........", "Data điền theo schema thực tế"]])

    d.add_heading("5. Thiết kế giao diện", 1)
    for s in ["Đăng nhập", "Dashboard", "Danh sách tài sản", "Thêm/Sửa tài sản",
              "Danh sách phòng", "Chi tiết phòng"]:
        d.add_heading("5.x " + s, 2)
        d.add_paragraph("[Ảnh chụp màn hình và mô tả]")

    d.add_heading("6. Kiểm thử", 1)
    table(d, ["ID", "Chức năng", "Bước thực hiện", "Kết quả mong đợi", "Kết quả thực tế", "Trạng thái"], [
        ["TC01", "Đăng nhập", "Nhập đúng tài khoản", "Vào Dashboard", "", ""],
        ["TC02", "Đăng nhập", "Nhập sai mật khẩu", "Báo lỗi", "", ""],
        ["TC03", "Phân quyền", "Chưa login truy cập /assets", "Chuyển về login", "", ""],
        ["TC04", "Phân quyền", "Staff xóa tài sản", "Bị từ chối", "", ""],
        ["TC05", "Tài sản", "Thêm tài sản hợp lệ", "Xuất hiện trong danh sách", "", ""],
        ["TC06", "Tìm kiếm", "Lọc theo trạng thái", "Đúng kết quả", "", ""],
    ])
    d.add_paragraph("(Bảng đầy đủ xem file Excel, sheet Test Case.)")

    d.add_heading("7. Phân công và đóng góp", 1)
    table(d, ["Thành viên", "Vai trò", "Công việc", "% đóng góp"], [
        ["........", "Front", "JSP, CSS, custom tag", ""],
        ["........", "Back", "Servlet, Filter, phân quyền", ""],
        ["........", "Data", "SQL, DAO, Model", ""],
    ])

    d.add_heading("8. Kết luận", 1)
    d.add_heading("8.1 Kết quả đạt được", 2)
    d.add_paragraph("........")
    d.add_heading("8.2 Hạn chế", 2)
    d.add_paragraph("........")
    d.add_heading("8.3 Hướng phát triển", 2)
    d.add_paragraph("........")
    d.add_heading("Phụ lục", 1)
    d.add_paragraph("A. Hướng dẫn cài đặt\nB. Tài khoản demo\nC. Tài liệu tham khảo")
    d.save(os.path.join(OUT, "report", "Bao_Cao_FCAMS.docx"))


def conventions():
    d = new_doc()
    cover(d, "BẢNG QUY ƯỚC CHUNG", "Dự án F-CAMS Simple")
    d.add_heading("1. Quy ước đặt tên", 1)
    table(d, ["Đối tượng", "Quy tắc", "Ví dụ"], [
        ["Class", "PascalCase", "AssetDAO, RoomServlet"],
        ["Method/biến", "camelCase", "getAllAssets()"],
        ["Hằng số", "UPPER_SNAKE_CASE", "ROLE_ADMIN"],
        ["Package", "chữ thường", "controller, dal"],
        ["Servlet", "<Entity>Servlet; URL chữ thường", "/assets, /rooms"],
        ["JSP", "kebab-case", "room-detail.jsp"],
        ["Bảng/cột SQL", "PascalCase", "Assets, AssetId"],
        ["CSS class", "kebab-case", ".btn-primary"],
    ])
    d.add_heading("2. Quy ước code", 1)
    for s in ["Indent 4 spaces, encoding UTF-8.",
              "Javadoc cho class và public method.",
              "DAO chỉ truy vấn DB; Servlet không viết SQL; JSP không dùng scriptlet, dùng JSTL/EL.",
              "Luôn dùng PreparedStatement và try-with-resources.",
              "Kiểm tra quyền ở AuthFilter và lặp lại trong Servlet với thao tác nguy hiểm.",
              "POST xong redirect (Post-Redirect-Get); thông báo qua attribute message/error."]:
        d.add_paragraph(s, style="List Bullet")
    d.add_heading("3. Quy ước Git", 1)
    table(d, ["Mục", "Quy tắc"], [
        ["Nhánh", "main (ổn định), dev, feature/<tên>-<việc>"],
        ["Commit", "<type>: <mô tả>; type = feat|fix|docs|style|refactor|test"],
        ["Merge", "Không push thẳng main; Pull Request + 1 người review"],
        ["Script SQL", "Đánh số 01_schema.sql, 02_seed.sql trong thư mục database/"],
    ])
    d.add_heading("4. Quy ước làm việc nhóm", 1)
    for s in ["Daily sync 10 phút qua chat.",
              "Họp tuần 1 lần: demo, review, cập nhật Excel kế hoạch.",
              "Việc bị chặn quá 1 ngày phải báo ngay.",
              "Cập nhật trạng thái công việc trong file Excel hằng ngày."]:
        d.add_paragraph(s, style="List Bullet")
    d.save(os.path.join(OUT, "planning", "Quy_Uoc_Chung_FCAMS.docx"))


# ---------------------------------------------------------------- EXCEL
HFILL = PatternFill("solid", fgColor="1F4E78")
HFONT = Font(bold=True, color="FFFFFF")
THIN = Side(style="thin", color="999999")
BORDER = Border(left=THIN, right=THIN, top=THIN, bottom=THIN)


def sheet(wb, name, header, rows, widths, first=False):
    ws = wb.active if first else wb.create_sheet()
    ws.title = name
    ws.append(header)
    for r in rows:
        ws.append(r)
    for c in ws[1]:
        c.fill, c.font = HFILL, HFONT
        c.alignment = Alignment(horizontal="center", vertical="center", wrap_text=True)
    for row in ws.iter_rows():
        for c in row:
            c.border = BORDER
            if c.row > 1:
                c.alignment = Alignment(vertical="top", wrap_text=True)
    for i, w in enumerate(widths, 1):
        ws.column_dimensions[get_column_letter(i)].width = w
    ws.freeze_panes = "A2"
    return ws


def excel():
    wb = Workbook()

    # (Tuần, Module, Công việc, Người, Ngày công)
    tasks = [
        (1, "Chung", "Dựng project, cấu hình Git, họp thống nhất quy ước", "Back", 1),
        (1, "Data", "Thiết kế ERD và schema SQL", "Data", 2),
        (1, "Data", "Seed data, DBContext, Model", "Data", 2),
        (1, "Auth", "UserDAO.login()", "Data", 1),
        (1, "Auth", "LoginServlet, LogoutServlet, AuthFilter", "Back", 2),
        (1, "Front", "Layout chung: header/footer, style.css nền", "Front", 2),
        (1, "Auth", "login.jsp", "Front", 1),
        (2, "Tài sản", "AssetDAO: CRUD, lọc, tìm kiếm", "Data", 3),
        (2, "Tài sản", "AssetServlet + validate + check role xóa", "Back", 3),
        (2, "Tài sản", "assets.jsp: danh sách, bộ lọc, form thêm/sửa", "Front", 3),
        (2, "Phòng", "RoomDAO: CRUD", "Data", 2),
        (2, "Phòng", "RoomServlet + check role", "Back", 2),
        (2, "Phòng", "rooms.jsp + form", "Front", 2),
        (3, "Dashboard", "Query thống kê, DashboardStats", "Data", 2),
        (3, "Dashboard", "DashboardServlet", "Back", 2),
        (3, "Dashboard", "dashboard.jsp (thẻ số liệu, danh sách)", "Front", 3),
        (3, "Phòng", "RoomAsset, truy vấn chi tiết phòng", "Data", 2),
        (3, "Phòng", "RoomDetailServlet", "Back", 2),
        (3, "Phòng", "room-detail.jsp", "Front", 2),
        (3, "Chung", "Custom tag: AssetStatus, ChairStatus, RoleName", "Front", 2),
        (4, "Chung", "Rà soát dữ liệu, chốt script SQL", "Data", 1),
        (4, "Chung", "Fix bug, xử lý lỗi, UTF-8", "Back", 3),
        (4, "Chung", "Polish UI, responsive", "Front", 3),
        (4, "Báo cáo", "Viết phần ERD, từ điển dữ liệu", "Data", 1),
        (4, "Báo cáo", "Viết phần yêu cầu, use case, sơ đồ tuần tự", "Back", 1),
        (4, "Báo cáo", "Viết phần giao diện, chụp màn hình", "Front", 1),
        (4, "Chung", "Kiểm thử tích hợp, slide, demo", "Back", 2),
    ]
    rows = []
    for i, (w, m, t, p, d) in enumerate(tasks, 1):
        rows.append([i, f"Tuần {w}", m, t, p, d, None, None, "Chưa bắt đầu", 0, ""])
    ws = sheet(wb, "Phan Cong", ["STT", "Tuần", "Module", "Công việc", "Người phụ trách",
                                 "Ngày công", "Bắt đầu", "Hạn", "Trạng thái", "% hoàn thành", "Ghi chú"],
               rows, [6, 10, 12, 55, 16, 11, 13, 13, 15, 14, 25], first=True)
    dv = DataValidation(type="list", formula1='"Chưa bắt đầu,Đang làm,Hoàn thành,Bị chặn"')
    dv2 = DataValidation(type="list", formula1='"Front,Back,Data"')
    ws.add_data_validation(dv)
    ws.add_data_validation(dv2)
    dv.add(f"I2:I{len(rows)+1}")
    dv2.add(f"E2:E{len(rows)+1}")
    for r in range(2, len(rows) + 2):
        ws[f"G{r}"].number_format = ws[f"H{r}"].number_format = "dd/mm/yyyy"
        ws[f"J{r}"].number_format = "0%"
    ws.auto_filter.ref = f"A1:K{len(rows)+1}"
    last = len(rows) + 1

    # Tổng hợp theo người (công thức)
    s = wb.create_sheet("Tong Hop")
    s.append(["Người", "Số việc", "Hoàn thành", "Tổng ngày công", "% hoàn thành TB"])
    for n in TEAM:
        r = s.max_row + 1
        s.append([n,
                  f"=COUNTIF('Phan Cong'!E2:E{last},A{r})",
                  f"=COUNTIFS('Phan Cong'!E2:E{last},A{r},'Phan Cong'!I2:I{last},\"Hoàn thành\")",
                  f"=SUMIF('Phan Cong'!E2:E{last},A{r},'Phan Cong'!F2:F{last})",
                  f"=IFERROR(AVERAGEIF('Phan Cong'!E2:E{last},A{r},'Phan Cong'!J2:J{last}),0)"])
        s[f"E{r}"].number_format = "0%"
    s.append(["Tổng", "=SUM(B2:B4)", "=SUM(C2:C4)", "=SUM(D2:D4)",
              f"=AVERAGE('Phan Cong'!J2:J{last})"])
    s["E5"].number_format = "0%"
    for c in s[1]:
        c.fill, c.font = HFILL, HFONT
    for row in s.iter_rows():
        for c in row:
            c.border = BORDER
    for c in s[5]:
        c.font = Font(bold=True)
    for i, w in enumerate([14, 12, 14, 16, 18], 1):
        s.column_dimensions[get_column_letter(i)].width = w

    # Kế hoạch tuần
    sheet(wb, "Ke Hoach", ["Tuần", "Mục tiêu", "Front", "Back", "Data", "Mốc bàn giao"], [
        ["Tuần 1", "Khởi động", "Layout, login.jsp", "Project, Auth, Filter", "ERD, schema, seed, DBContext, Model", "M1: Đăng nhập chạy được"],
        ["Tuần 2", "CRUD cốt lõi", "assets.jsp, rooms.jsp", "AssetServlet, RoomServlet", "AssetDAO, RoomDAO", "M2: CRUD tài sản/phòng"],
        ["Tuần 3", "Mở rộng", "dashboard.jsp, room-detail.jsp, tag", "DashboardServlet, RoomDetailServlet", "Query thống kê, RoomAsset", "M3: Đủ 4 module, phân quyền đúng"],
        ["Tuần 4", "Hoàn thiện", "Polish UI, responsive", "Fix bug, validate", "Chốt dữ liệu, script final", "M4: Báo cáo + demo"],
    ], [10, 18, 32, 34, 36, 32])

    sheet(wb, "Phan Vai Tro", ["Vai trò", "Phạm vi", "Thư mục/file phụ trách", "Thành viên"], [
        ["Front", "JSP, CSS, layout, custom tag, UX", "web/*.jsp, web/includes, style.css, tag/*, fcams.tld", ""],
        ["Back", "Servlet, Filter, nghiệp vụ, phân quyền, validate", "controller/*, filter/AuthFilter, web.xml", ""],
        ["Data", "ERD, SQL, seed, DBContext, DAO, Model", "database/*.sql, dal/*, model/*, ConnectDB.properties", ""],
    ], [12, 45, 55, 20])

    sheet(wb, "Quy Uoc", ["Nhóm", "Quy tắc"], [
        ["Tên class", "PascalCase"], ["Tên method/biến", "camelCase"],
        ["Hằng số", "UPPER_SNAKE_CASE"], ["JSP", "kebab-case"],
        ["Bảng/cột SQL", "PascalCase"],
        ["Nhánh Git", "main, dev, feature/<tên>-<việc>"],
        ["Commit", "feat|fix|docs|style|refactor|test: mô tả"],
        ["Merge", "Pull Request + 1 người review, không push thẳng main"],
        ["Code", "PreparedStatement; không scriptlet; DAO chỉ truy vấn DB"],
        ["Họp", "Daily 10 phút; họp tuần cập nhật Excel"],
    ], [20, 70])

    sheet(wb, "Test Case", ["ID", "Chức năng", "Bước thực hiện", "Kết quả mong đợi",
                            "Kết quả thực tế", "Trạng thái", "Người test"], [
        ["TC01", "Đăng nhập", "Nhập đúng tài khoản", "Vào Dashboard", "", "", ""],
        ["TC02", "Đăng nhập", "Nhập sai mật khẩu", "Báo lỗi", "", "", ""],
        ["TC03", "Phân quyền", "Chưa login truy cập /assets", "Chuyển về login", "", "", ""],
        ["TC04", "Phân quyền", "Staff xóa tài sản", "Bị từ chối", "", "", ""],
        ["TC05", "Phân quyền", "Staff sửa/xóa phòng", "Bị từ chối", "", "", ""],
        ["TC06", "Tài sản", "Thêm tài sản hợp lệ", "Xuất hiện trong danh sách", "", "", ""],
        ["TC07", "Tài sản", "Thêm tài sản thiếu trường bắt buộc", "Báo lỗi validate", "", "", ""],
        ["TC08", "Tài sản", "Sửa/chuyển phòng", "Dữ liệu cập nhật", "", "", ""],
        ["TC09", "Tìm kiếm", "Lọc theo phòng/danh mục/trạng thái", "Đúng kết quả", "", "", ""],
        ["TC10", "Phòng", "Thêm/sửa/xóa phòng (Admin)", "Thành công", "", "", ""],
        ["TC11", "Chi tiết phòng", "Mở phòng", "Hiện đúng tài sản", "", "", ""],
        ["TC12", "Dashboard", "Mở dashboard", "Số liệu khớp DB", "", "", ""],
        ["TC13", "Tiếng Việt", "Nhập dữ liệu có dấu", "Hiển thị đúng", "", "", ""],
    ], [8, 16, 38, 28, 24, 14, 14])

    sheet(wb, "Rui Ro", ["Rủi ro", "Mức độ", "Giải pháp", "Người phụ trách"], [
        ["Xung đột Git", "Trung bình", "Nhánh feature riêng, pull dev thường xuyên", "Cả nhóm"],
        ["Lệch schema DB giữa các máy", "Cao", "Một nguồn script database/, thông báo khi đổi schema", "Data"],
        ["Lỗi font tiếng Việt", "Trung bình", "UTF-8 toàn bộ, dùng nvarchar", "Back"],
        ["Trễ tiến độ", "Trung bình", "Họp tuần, hỗ trợ chéo", "Cả nhóm"],
        ["Khác cấu hình DB/Tomcat", "Thấp", "Hướng dẫn cài đặt trong README", "Data"],
    ], [34, 14, 50, 18])

    wb.save(os.path.join(OUT, "planning", "Ke_Hoach_Phan_Cong_FCAMS.xlsx"))


if __name__ == "__main__":
    report()
    conventions()
    excel()
    print("done")


