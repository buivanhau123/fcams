package test;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * Kiểm thử toàn diện qua giao thức HTTP (Port 8080 - Apache Tomcat):
 * 1. Chặn người dùng chưa đăng nhập (AuthFilter)
 * 2. Đăng nhập Giảng viên (hungnq / 123 - LECTURER)
 * 3. Kiểm tra quyền của Giảng viên: xem phòng, xem tài sản, gửi báo cáo đồ hỏng hóc
 * 4. Kiểm tra AuthFilter chặn Giảng viên truy cập trang Lịch sử (/history)
 * 5. Đăng nhập Cán bộ Quản lý (it_beta / 123 - STAFF)
 * 6. Kiểm tra quyền của Staff: xem lịch sử thay đổi (hiển thị báo cáo của Giảng viên)
 * 7. Kiểm tra AuthFilter chặn Staff xóa tài sản (chức năng nguy hiểm)
 * 8. Đăng nhập Quản trị viên (admin / 123 - ADMIN)
 * 9. Kiểm tra toàn quyền CRUD của Admin
 */
public class HttpLiveTester {

    private static final String BASE_URL = "http://localhost:8080/FCAMS-Simple";
    private static int totalHttp = 0;
    private static int passedHttp = 0;
    private static int failedHttp = 0;

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("  BẮT ĐẦU KIỂM THỬ GIAO DIỆN & PHÂN QUYỀN HTTP SERVLET / FILTER (PORT 8080)");
        System.out.println("================================================================================\n");

        try {
            testUnauthenticatedAccess();
            testLecturerFlowAndPermissions();
            testStaffFlowAndPermissions();
            testAdminFlowAndPermissions();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("\n================================================================================");
        System.out.printf("  KẾT QUẢ KIỂM THỬ HTTP: %d/%d passed (Thành công: %.1f%%)\n",
                passedHttp, totalHttp, (totalHttp > 0 ? (passedHttp * 100.0 / totalHttp) : 0));
        if (failedHttp == 0) {
            System.out.println("  >>> TẤT CẢ CÁC KỊCH BẢN KIỂM THỬ HTTP / RBAC ĐỀU ĐẠT CHUẨN 100%! <<<");
        } else {
            System.out.printf("  >>> CẢNH BÁO: CÓ %d KỊCH BẢN HTTP BỊ THẤT BẠI! <<<\n", failedHttp);
        }
        System.out.println("================================================================================");

        if (failedHttp > 0) {
            System.exit(1);
        }
    }

    private static void assertHttp(String testName, boolean condition, String details) {
        totalHttp++;
        if (condition) {
            passedHttp++;
            System.out.printf("  [PASS] %-60s | %s\n", testName, details);
        } else {
            failedHttp++;
            System.err.printf("  [FAIL] %-60s | %s\n", testName, details);
        }
    }

    private static HttpClient createClient() {
        CookieManager cm = new CookieManager();
        cm.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        return HttpClient.newBuilder()
                .cookieHandler(cm)
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
    }

    // 1. Chưa đăng nhập
    private static void testUnauthenticatedAccess() throws Exception {
        System.out.println("--- 1. KIỂM THỬ BẢO MẬT KHI CHƯA ĐĂNG NHẬP (AUTHFILTER) ---");
        HttpClient client = createClient();
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/dashboard"))
                .GET()
                .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        String loc = resp.headers().firstValue("Location").orElse("");
        boolean isRedirectLogin = (resp.statusCode() == 302 && loc.contains("/login"));
        assertHttp("Chưa đăng nhập truy cập /dashboard bị redirect về /login",
                isRedirectLogin, "HTTP " + resp.statusCode() + " -> " + loc);
    }

    // 2. Kịch bản Giảng viên (hungnq / 123)
    private static void testLecturerFlowAndPermissions() throws Exception {
        System.out.println("\n--- 2. KIỂM THỬ NGHIỆP VỤ & PHÂN QUYỀN GIẢNG VIÊN (hungnq - LECTURER) ---");
        HttpClient client = createClient();

        // 2.1 Đăng nhập
        String loginBody = "username=hungnq&password=123";
        HttpRequest loginReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/login"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(loginBody))
                .build();
        HttpResponse<String> loginResp = client.send(loginReq, HttpResponse.BodyHandlers.ofString());
        String loginLoc = loginResp.headers().firstValue("Location").orElse("");
        boolean loginOk = (loginResp.statusCode() == 302 && loginLoc.contains("/dashboard"));
        assertHttp("Giảng viên hungnq đăng nhập thành công", loginOk, "Redirect: " + loginLoc);

        // 2.2 Xem Dashboard
        HttpRequest dashReq = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/dashboard")).GET().build();
        HttpResponse<String> dashResp = client.send(dashReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        String dashHtml = dashResp.body();
        boolean dashHasLecturer = dashHtml.contains("Thầy Nguyễn Quốc Hùng") && dashHtml.contains("Giảng viên");
        boolean dashNoHistoryTab = !dashHtml.contains("href=\"" + "/FCAMS-Simple/history\"");
        assertHttp("Dashboard hiển thị thông tin Giảng viên", dashHasLecturer, "Tên: Thầy Nguyễn Quốc Hùng");
        assertHttp("Menu của Giảng viên không có tab Lịch sử", dashNoHistoryTab, "Ẩn tab Lịch sử đối với Giảng viên");

        // 2.3 Xem trang Tài sản (/assets)
        HttpRequest assetsReq = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/assets")).GET().build();
        HttpResponse<String> assetsResp = client.send(assetsReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        String assetsHtml = assetsResp.body();
        boolean hasReportBtn = assetsHtml.contains("[Báo hỏng]");
        boolean noAddAsset = !assetsHtml.contains("Ghi nhận tài sản mới vào hệ thống");
        assertHttp("Trang Tài sản của Giảng viên có nút [Báo hỏng]", hasReportBtn, "");
        assertHttp("Trang Tài sản của Giảng viên ẩn form Thêm mới", noAddAsset, "Bảo mật phân quyền tốt");

        // 2.4 Xem chi tiết phòng (/room-detail?id=BE-301)
        HttpRequest roomReq = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/room-detail?id=BE-301")).GET().build();
        HttpResponse<String> roomResp = client.send(roomReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        String roomHtml = roomResp.body();
        boolean hasRoomReportForm = roomHtml.contains("Gửi báo cáo đồ hỏng hóc trong phòng BE-301");
        assertHttp("Trang Chi tiết phòng có form báo hỏng cho Giảng viên", hasRoomReportForm, "Room: BE-301");

        // 2.5 Gửi báo cáo đồ hỏng hóc
        String damageDesc = "Smart TV Samsung bị sọc màn hình ngang, không hiển thị HDMI";
        String postData = "action=reportDamage"
                + "&assetCode=" + URLEncoder.encode("AST-BE301-TV", StandardCharsets.UTF_8)
                + "&damageNote=" + URLEncoder.encode(damageDesc, StandardCharsets.UTF_8)
                + "&returnRoomId=BE-301";
        HttpRequest reportReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/assets"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(postData))
                .build();
        HttpResponse<String> reportResp = client.send(reportReq, HttpResponse.BodyHandlers.ofString());
        String reportLoc = reportResp.headers().firstValue("Location").orElse("");
        boolean reportOk = (reportResp.statusCode() == 302 && reportLoc.contains("msg="));
        assertHttp("Giảng viên gửi báo cáo đồ hỏng hóc thành công", reportOk, "Redirect with confirmation message");

        // 2.6 Giảng viên cố tình truy cập /history -> BỊ CHẶN BỞI AUTHFILTER
        HttpRequest histReq = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/history")).GET().build();
        HttpResponse<String> histResp = client.send(histReq, HttpResponse.BodyHandlers.ofString());
        String histLoc = histResp.headers().firstValue("Location").orElse("");
        boolean histBlocked = (histResp.statusCode() == 302 && histLoc.contains("error="));
        assertHttp("AuthFilter chặn Giảng viên truy cập trang Lịch sử /history", histBlocked, "Redirect with error message");
    }

    // 3. Kịch bản Cán bộ Quản lý (it_beta / 123 - STAFF)
    private static void testStaffFlowAndPermissions() throws Exception {
        System.out.println("\n--- 3. KIỂM THỬ NGHIỆP VỤ & PHÂN QUYỀN CÁN BỘ QUẢN LÝ (it_beta - STAFF) ---");
        HttpClient client = createClient();

        // 3.1 Đăng nhập
        String loginBody = "username=it_beta&password=123";
        HttpRequest loginReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/login"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(loginBody))
                .build();
        HttpResponse<String> loginResp = client.send(loginReq, HttpResponse.BodyHandlers.ofString());
        assertHttp("Staff it_beta đăng nhập thành công", loginResp.statusCode() == 302, "");

        // 3.2 Staff truy cập trang Lịch sử (/history) -> ĐƯỢC PHÉP
        HttpRequest histReq = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/history")).GET().build();
        HttpResponse<String> histResp = client.send(histReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        String histHtml = histResp.body();
        boolean canViewHistory = (histResp.statusCode() == 200 && histHtml.contains("Nhật ký Lịch sử Thay đổi"));
        boolean seesLecturerReport = histHtml.contains("Smart TV Samsung bị sọc màn hình");
        assertHttp("Staff được phép truy cập trang Lịch sử (/history)", canViewHistory, "HTTP 200 OK");
        assertHttp("Lịch sử hiển thị ngay báo cáo hỏng hóc của Giảng viên", seesLecturerReport, "Tích hợp đồng bộ LECTURER -> STAFF");

        // 3.3 Staff xem trang Tài sản: có quyền Thêm nhưng KHÔNG có nút Xóa
        HttpRequest assetsReq = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/assets")).GET().build();
        HttpResponse<String> assetsResp = client.send(assetsReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        String assetsHtml = assetsResp.body();
        boolean staffCanAdd = assetsHtml.contains("Ghi nhận tài sản mới vào hệ thống");
        boolean staffNoDelete = !assetsHtml.contains("Xác nhận xóa tài sản");
        assertHttp("Staff có quyền Thêm tài sản nhưng không có nút Xóa", staffCanAdd && staffNoDelete, "Hạn chế thao tác nguy hiểm");

        // 3.4 Staff cố gửi POST Xóa tài sản -> BỊ AUTHFILTER CHẶN
        String deleteData = "action=delete&code=AST-BE301-PJ";
        HttpRequest delReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/assets"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(deleteData))
                .build();
        HttpResponse<String> delResp = client.send(delReq, HttpResponse.BodyHandlers.ofString());
        String delLoc = delResp.headers().firstValue("Location").orElse("");
        boolean deleteBlocked = (delResp.statusCode() == 302 && delLoc.contains("error="));
        assertHttp("AuthFilter chặn Staff thực hiện thao tác Xóa tài sản", deleteBlocked, "Chặn chức năng nguy hiểm");
    }

    // 4. Kịch bản Quản trị viên (admin / 123 - ADMIN)
    private static void testAdminFlowAndPermissions() throws Exception {
        System.out.println("\n--- 4. KIỂM THỬ TOÀN QUYỀN QUẢN TRỊ VIÊN (admin - ADMIN) ---");
        HttpClient client = createClient();

        // 4.1 Đăng nhập
        String loginBody = "username=admin&password=123";
        HttpRequest loginReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/login"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(loginBody))
                .build();
        HttpResponse<String> loginResp = client.send(loginReq, HttpResponse.BodyHandlers.ofString());
        assertHttp("Admin đăng nhập thành công", loginResp.statusCode() == 302, "");

        // 4.2 Admin có nút Xóa tài sản
        HttpRequest assetsReq = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/assets")).GET().build();
        HttpResponse<String> assetsResp = client.send(assetsReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        boolean adminHasDeleteAsset = assetsResp.body().contains("Xác nhận xóa tài sản");
        assertHttp("Admin có toàn quyền CRUD tài sản (bao gồm Xóa)", adminHasDeleteAsset, "Full Asset CRUD");

        // 4.3 Admin có toàn quyền CRUD phòng học
        HttpRequest roomsReq = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/rooms")).GET().build();
        HttpResponse<String> roomsResp = client.send(roomsReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        String roomsHtml = roomsResp.body();
        boolean adminHasRoomCrud = roomsHtml.contains("Thêm phòng học mới") && roomsHtml.contains("Xác nhận xóa phòng học");
        assertHttp("Admin có toàn quyền CRUD phòng học (Thêm/Sửa/Xóa phòng)", adminHasRoomCrud, "Full Room CRUD");

        // 4.4 Admin truy cập Lịch sử (/history)
        HttpRequest histReq = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/history")).GET().build();
        HttpResponse<String> histResp = client.send(histReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertHttp("Admin có toàn quyền xem Lịch sử thay đổi toàn hệ thống", histResp.statusCode() == 200, "HTTP 200 OK");
    }
}
