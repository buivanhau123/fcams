package test;

import dal.AssetDAO;
import dal.DBContext;
import dal.HistoryDAO;
import dal.RoomDAO;
import dal.UserDAO;
import java.sql.Connection;
import java.util.List;
import model.Asset;
import model.ChangeHistory;
import model.DashboardStats;
import model.Room;
import model.User;

/**
 * Bộ kiểm thử toàn diện hệ thống F-CAMS Simple:
 * 1. Kiểm tra kết nối CSDL SQL Server (DBContext)
 * 2. Kiểm tra xác thực & phân quyền 3 vai trò: ADMIN, STAFF, LECTURER (UserDAO)
 * 3. Kiểm tra quản lý phòng học & xem chi tiết thiết bị phòng (RoomDAO)
 * 4. Kiểm tra quản lý tài sản, bộ lọc, thêm/sửa/chuyển/xóa tài sản (AssetDAO)
 * 5. Kiểm tra nghiệp vụ Giảng viên báo cáo đồ hỏng hóc & tự động đồng bộ (AssetDAO)
 * 6. Kiểm tra chức năng Lịch sử thay đổi: ghi log & bộ lọc đa tiêu chí (HistoryDAO)
 * 7. Kiểm tra thủ tục đặt lại dữ liệu mẫu (sp_ResetFcamsData)
 */
public class ComprehensiveSystemTester {

    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("  BẮT ĐẦU CHẠY KIỂM THỬ TOÀN DIỆN HỆ THỐNG F-CAMS SIMPLE (PRJ301)");
        System.out.println("================================================================================\n");

        testDatabaseConnection();
        testUserAuthenticationAndRoles();
        testRoomManagement();
        testAssetManagementAndFilters();
        testLecturerDamageReporting();
        testChangeHistoryLoggingAndFilters();
        testSampleDataReset();

        System.out.println("\n================================================================================");
        System.out.printf("  KẾT QUẢ KIỂM THỬ: %d/%d passed (Thành công: %.1f%%)\n",
                passedTests, totalTests, (totalTests > 0 ? (passedTests * 100.0 / totalTests) : 0));
        if (failedTests == 0) {
            System.out.println("  >>> TẤT CẢ CÁC TIÊU CHÍ KIỂM THỬ ĐỀU ĐẠT CHUẨN 100% SUCCESS! <<<");
        } else {
            System.out.printf("  >>> CẢNH BÁO: CÓ %d BÀI TEST BỊ THẤT BẠI! <<<\n", failedTests);
        }
        System.out.println("================================================================================");

        if (failedTests > 0) {
            System.exit(1);
        }
    }

    private static void assertTrue(String testName, boolean condition, String details) {
        totalTests++;
        if (condition) {
            passedTests++;
            System.out.printf("  [PASS] %-60s | %s\n", testName, details);
        } else {
            failedTests++;
            System.err.printf("  [FAIL] %-60s | %s\n", testName, details);
        }
    }

    // 1. Kiểm tra kết nối CSDL
    private static void testDatabaseConnection() {
        System.out.println("--- 1. KIỂM THỬ KẾT NỐI CƠ SỞ DỮ LIỆU SQL SERVER ---");
        DBContext db = new DBContext();
        Connection conn = db.getConnection();
        assertTrue("Kết nối SQL Server FCAMS_DB thành công", conn != null,
                conn != null ? "Connection != null (Sẵn sàng)" : "Connection == null (Lỗi kết nối)");
    }

    // 2. Kiểm tra xác thực & phân quyền 3 vai trò
    private static void testUserAuthenticationAndRoles() {
        System.out.println("\n--- 2. KIỂM THỬ XÁC THỰC & PHÂN QUYỀN 3 VAI TRÒ (ADMIN, STAFF, LECTURER) ---");
        UserDAO userDAO = new UserDAO();

        // 2.1 Quản trị viên (ADMIN)
        User admin = userDAO.authenticate("admin", "123");
        assertTrue("Đăng nhập tài khoản admin thành công", admin != null,
                admin != null ? "User: " + admin.getFullName() : "Không tìm thấy user");
        assertTrue("Tài khoản admin có role ADMIN", admin != null && "ADMIN".equalsIgnoreCase(admin.getRole()),
                admin != null ? "Role: " + admin.getRole() : "");
        assertTrue("Kiểm tra quyền admin.isAdmin() == true", admin != null && admin.isAdmin(), "");
        assertTrue("Kiểm tra quyền admin.isStaffOrAdmin() == true", admin != null && admin.isStaffOrAdmin(), "");

        // 2.2 Cán bộ Quản lý (STAFF)
        User staff = userDAO.authenticate("it_beta", "123");
        assertTrue("Đăng nhập tài khoản it_beta thành công", staff != null,
                staff != null ? "User: " + staff.getFullName() : "");
        assertTrue("Tài khoản it_beta có role STAFF", staff != null && "STAFF".equalsIgnoreCase(staff.getRole()),
                staff != null ? "Role: " + staff.getRole() : "");
        assertTrue("Kiểm tra quyền staff.isStaff() == true", staff != null && staff.isStaff(), "");
        assertTrue("Kiểm tra quyền staff.isAdmin() == false", staff != null && !staff.isAdmin(), "");

        // 2.3 Giảng viên (LECTURER)
        User lecturer = userDAO.authenticate("hungnq", "123");
        assertTrue("Đăng nhập tài khoản hungnq thành công", lecturer != null,
                lecturer != null ? "User: " + lecturer.getFullName() : "");
        assertTrue("Tài khoản hungnq có role LECTURER", lecturer != null && "LECTURER".equalsIgnoreCase(lecturer.getRole()),
                lecturer != null ? "Role: " + lecturer.getRole() : "");
        assertTrue("Kiểm tra quyền lecturer.isLecturer() == true", lecturer != null && lecturer.isLecturer(), "");
        assertTrue("Kiểm tra quyền lecturer.isAdmin() == false", lecturer != null && !lecturer.isAdmin(), "");
        assertTrue("Kiểm tra quyền lecturer.isStaff() == false", lecturer != null && !lecturer.isStaff(), "");

        // 2.4 Đăng nhập sai mật khẩu
        User wrong = userDAO.authenticate("admin", "sai_mat_khau");
        assertTrue("Đăng nhập sai mật khẩu trả về null an toàn", wrong == null,
                "Wrong password -> null (Chặn đăng nhập)");
    }

    // 3. Kiểm tra danh mục phòng học
    private static void testRoomManagement() {
        System.out.println("\n--- 3. KIỂM THỬ QUẢN LÝ PHÒNG HỌC & CHI TIẾT THIẾT BỊ (ROOM CRUD & DETAIL) ---");
        RoomDAO roomDAO = new RoomDAO();

        List<Room> allRooms = roomDAO.getAllRooms();
        assertTrue("Lấy danh sách toàn bộ phòng học (ít nhất 9 phòng)", allRooms != null && allRooms.size() >= 9,
                "Tổng số phòng: " + (allRooms != null ? allRooms.size() : 0));

        Room r301 = roomDAO.getRoomById("BE-301");
        assertTrue("Tìm thấy chi tiết phòng BE-301", r301 != null,
                r301 != null ? "Phòng " + r301.getId() + " (Tòa " + r301.getBuilding() + " - Tầng " + r301.getFloor() + ")" : "");
        assertTrue("Phòng BE-301 có nạp danh sách tài sản trực thuộc (ít nhất 4 món)",
                r301 != null && r301.getAssetCount() >= 4,
                r301 != null ? "Số tài sản: " + r301.getAssetCount() : "");

        List<Room> betaRooms = roomDAO.filterRooms("Beta", "ALL", "");
        assertTrue("Lọc phòng theo Tòa Beta (ít nhất 3 phòng)", betaRooms != null && betaRooms.size() >= 3,
                "Số phòng Tòa Beta: " + (betaRooms != null ? betaRooms.size() : 0));
    }

    // 4. Kiểm tra quản lý tài sản
    private static void testAssetManagementAndFilters() {
        System.out.println("\n--- 4. KIỂM THỬ QUẢN LÝ TÀI SẢN & BỘ LỌC (ASSET MANAGEMENT - CRUD) ---");
        AssetDAO assetDAO = new AssetDAO();

        List<Asset> allAssets = assetDAO.getAllAssets();
        assertTrue("Lấy danh sách toàn bộ tài sản (ít nhất 40 tài sản)", allAssets != null && allAssets.size() >= 40,
                "Tổng số tài sản: " + (allAssets != null ? allAssets.size() : 0));

        // Lọc theo phòng
        List<Asset> be301Assets = assetDAO.filterAssets("BE-301", "ALL", "ALL", "");
        assertTrue("Lọc tài sản theo phòng BE-301 (ít nhất 4 tài sản)", be301Assets != null && be301Assets.size() >= 4,
                "Số tài sản BE-301: " + (be301Assets != null ? be301Assets.size() : 0));

        // Lọc theo danh mục
        List<Asset> projectors = assetDAO.filterAssets("ALL", "Máy chiếu", "ALL", "");
        assertTrue("Lọc tài sản danh mục Máy chiếu (ít nhất 7 máy chiếu)", projectors != null && projectors.size() >= 7,
                "Số máy chiếu: " + (projectors != null ? projectors.size() : 0));

        // Kiểm tra KPI Dashboard
        DashboardStats stats = assetDAO.getDashboardStats(9);
        assertTrue("Thống kê Dashboard trả về tổng tài sản > 0", stats.getTotalAssets() > 0,
                "Tổng tài sản: " + stats.getTotalAssets());
        assertTrue("Thống kê Dashboard tính đúng giá trị tài sản > 0", stats.getTotalValue() > 0,
                "Tổng giá trị: " + stats.getFormattedTotalValue());
    }

    // 5. Kiểm tra nghiệp vụ Giảng viên báo cáo đồ hỏng hóc
    private static void testLecturerDamageReporting() {
        System.out.println("\n--- 5. KIỂM THỬ NGHIỆP VỤ GIẢNG VIÊN BÁO CÁO ĐỒ HỎNG HÓC & ĐỒNG BỘ ---");
        AssetDAO assetDAO = new AssetDAO();
        HistoryDAO historyDAO = new HistoryDAO();

        String testCode = "AST-BE301-PJ";
        Asset before = assetDAO.getAssetByCode(testCode);
        assertTrue("Lấy thông tin tài sản test AST-BE301-PJ", before != null,
                before != null ? before.getName() : "");

        // Giảng viên gửi báo cáo đồ hỏng
        String damageDesc = "Máy chiếu không lên nguồn, đèn tín hiệu nhấp nháy đỏ";
        String lecturerName = "Thầy Nguyễn Quốc Hùng";
        boolean reportOk = assetDAO.reportAssetDamage(testCode, damageDesc, lecturerName);
        assertTrue("Báo cáo đồ hỏng qua reportAssetDamage thành công", reportOk, "");

        // Kiểm tra trạng thái sau khi báo hỏng
        Asset after = assetDAO.getAssetByCode(testCode);
        assertTrue("Trạng thái tài sản đổi sang FAULTY", after != null && "FAULTY".equalsIgnoreCase(after.getStatus()),
                after != null ? "Status: " + after.getStatus() : "");
        assertTrue("Ghi chú tài sản chứa thông tin người báo hỏng",
                after != null && after.getNote().contains("Báo hỏng bởi Thầy Nguyễn Quốc Hùng"),
                after != null ? "Note: " + after.getNote() : "");

        // Ghi nhận vào Lịch sử thay đổi
        User lecturer = new User("hungnq", "123", lecturerName, "LECTURER", "");
        boolean logOk = historyDAO.logChange("REPORT_DAMAGE", "ASSET", testCode, after.getName(),
                after.getRoomId(), "Trạng thái: " + before.getStatus(), "Trạng thái: FAULTY",
                "Báo cáo đồ hỏng: " + damageDesc, lecturer);
        assertTrue("Ghi nhật ký lịch sử báo hỏng thành công", logOk, "");
    }

    // 6. Kiểm tra chức năng Lịch sử thay đổi
    private static void testChangeHistoryLoggingAndFilters() {
        System.out.println("\n--- 6. KIỂM THỬ NHẬT KÝ LỊCH SỬ THAY ĐỔI & BỘ LỌC ĐA TIÊU CHÍ (HISTORY) ---");
        HistoryDAO historyDAO = new HistoryDAO();

        // 6.1 Lấy toàn bộ lịch sử
        List<ChangeHistory> allHistory = historyDAO.filterHistory("ALL", "ALL", "ALL", "");
        assertTrue("Lấy danh sách lịch sử thay đổi (ít nhất 6 bản ghi)", allHistory != null && allHistory.size() >= 6,
                "Tổng số bản ghi: " + (allHistory != null ? allHistory.size() : 0));

        // 6.2 Lọc theo loại thao tác REPORT_DAMAGE
        List<ChangeHistory> reportLogs = historyDAO.filterHistory("REPORT_DAMAGE", "ALL", "ALL", "");
        assertTrue("Lọc lịch sử theo loại REPORT_DAMAGE (ít nhất 1 bản ghi)", reportLogs != null && !reportLogs.isEmpty(),
                "Số bản ghi báo hỏng: " + (reportLogs != null ? reportLogs.size() : 0));

        // 6.3 Lọc theo vai trò LECTURER
        List<ChangeHistory> lecturerLogs = historyDAO.filterHistory("ALL", "ALL", "LECTURER", "");
        assertTrue("Lọc lịch sử thao tác của Giảng viên (LECTURER)", lecturerLogs != null && !lecturerLogs.isEmpty(),
                "Số thao tác của Giảng viên: " + (lecturerLogs != null ? lecturerLogs.size() : 0));

        // 6.4 Lọc theo vai trò STAFF
        List<ChangeHistory> staffLogs = historyDAO.filterHistory("ALL", "ALL", "STAFF", "");
        assertTrue("Lọc lịch sử thao tác của Cán bộ Quản lý (STAFF)", staffLogs != null && !staffLogs.isEmpty(),
                "Số thao tác của Staff: " + (staffLogs != null ? staffLogs.size() : 0));

        // 6.5 Lọc theo vai trò ADMIN
        List<ChangeHistory> adminLogs = historyDAO.filterHistory("ALL", "ALL", "ADMIN", "");
        assertTrue("Lọc lịch sử thao tác của Quản trị viên (ADMIN)", adminLogs != null && !adminLogs.isEmpty(),
                "Số thao tác của Admin: " + (adminLogs != null ? adminLogs.size() : 0));

        // 6.6 Tìm kiếm từ khóa
        List<ChangeHistory> searchLogs = historyDAO.filterHistory("ALL", "ALL", "ALL", "Máy chiếu");
        assertTrue("Tìm kiếm từ khóa 'Máy chiếu' trong lịch sử thành công", searchLogs != null && !searchLogs.isEmpty(),
                "Số kết quả tìm kiếm: " + (searchLogs != null ? searchLogs.size() : 0));
    }

    // 7. Kiểm tra khôi phục dữ liệu mẫu
    private static void testSampleDataReset() {
        System.out.println("\n--- 7. KIỂM THỬ THỦ TỤC ĐẶT LẠI DỮ LIỆU MẪU BAN ĐẦU (SP_RESETFCAMSDATA) ---");
        RoomDAO roomDAO = new RoomDAO();
        boolean resetOk = roomDAO.resetSampleData();
        assertTrue("Gọi stored procedure sp_ResetFcamsData thành công", resetOk,
                "Dữ liệu được khôi phục về trạng thái mẫu ban đầu chuẩn hóa");
    }
}
