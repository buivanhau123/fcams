package controller;

import dal.AssetDAO;
import dal.HistoryDAO;
import dal.RoomDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import model.Asset;
import model.Room;
import model.User;

/**
 * Controller Quản lý Tài sản (Asset Management - CRUD - Tiêu chí 3) & Báo cáo đồ hỏng hóc:
 * - Danh sách & Tìm kiếm: Liệt kê toàn bộ tài sản kèm bộ lọc (Phòng học, Danh mục, Trạng thái) và tìm kiếm theo tên/mã tài sản.
 * - Báo cáo đồ hỏng hóc (Giảng viên / Staff / Admin): Báo cáo sự cố thiết bị trong phòng và tự động ghi vào Lịch sử thay đổi.
 * - Thêm mới / Cập nhật / Chuyển phòng (Staff / Admin): Tự động lưu vết vào bảng Lịch sử thay đổi.
 * - Xóa: Xóa bản ghi tài sản (chỉ dành cho quyền Admin) và lưu vết vào bảng Lịch sử thay đổi.
 */
@WebServlet(name = "AssetServlet", urlPatterns = {"/assets"})
public class AssetServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        AssetDAO assetDAO = new AssetDAO();
        RoomDAO roomDAO = new RoomDAO();

        String roomId = request.getParameter("roomId");
        String category = request.getParameter("category");
        String status = request.getParameter("status");
        String keyword = request.getParameter("keyword");

        String editCode = request.getParameter("editCode");
        String transferCode = request.getParameter("transferCode");
        String reportCode = request.getParameter("reportCode");

        List<Asset> assets = assetDAO.filterAssets(roomId, category, status, keyword);
        List<Room> rooms = roomDAO.getAllRooms();
        List<String> categories = assetDAO.getCategories();

        User currentUser = (User) request.getSession().getAttribute("currentUser");

        if (editCode != null && !editCode.isBlank() && currentUser != null && currentUser.isStaffOrAdmin()) {
            request.setAttribute("editAsset", assetDAO.getAssetByCode(editCode));
        }
        if (transferCode != null && !transferCode.isBlank() && currentUser != null && currentUser.isStaffOrAdmin()) {
            request.setAttribute("transferAsset", assetDAO.getAssetByCode(transferCode));
        }
        if (reportCode != null && !reportCode.isBlank()) {
            request.setAttribute("reportAsset", assetDAO.getAssetByCode(reportCode));
        }

        request.setAttribute("assets", assets);
        request.setAttribute("rooms", rooms);
        request.setAttribute("categories", categories);
        request.setAttribute("selectedRoom", roomId != null ? roomId : "ALL");
        request.setAttribute("selectedCategory", category != null ? category : "ALL");
        request.setAttribute("selectedStatus", status != null ? status : "ALL");
        request.setAttribute("keyword", keyword != null ? keyword : "");
        request.setAttribute("activeTab", "assets");

        request.getRequestDispatcher("/assets.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        AssetDAO assetDAO = new AssetDAO();
        HistoryDAO historyDAO = new HistoryDAO();
        User currentUser = (User) request.getSession().getAttribute("currentUser");

        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // 0. Báo cáo đồ hỏng hóc (Dành cho Giảng viên LECTURER, STAFF và ADMIN)
        if ("reportDamage".equalsIgnoreCase(action)) {
            String assetCode = request.getParameter("assetCode");
            String damageNote = request.getParameter("damageNote");
            String returnRoomId = request.getParameter("returnRoomId");

            String redirectBase = (returnRoomId != null && !returnRoomId.isBlank())
                    ? (request.getContextPath() + "/room-detail?id=" + encode(returnRoomId.trim()) + "&")
                    : (request.getContextPath() + "/assets?");

            if (assetCode == null || assetCode.isBlank()) {
                response.sendRedirect(redirectBase + "error=" + encode("Vui lòng chọn thiết bị / đồ vật cần báo hỏng!"));
                return;
            }
            if (damageNote == null || damageNote.trim().length() < 3) {
                response.sendRedirect(redirectBase + "error=" + encode("Vui lòng nhập mô tả chi tiết tình trạng hỏng hóc (ít nhất 3 ký tự)!"));
                return;
            }

            Asset before = assetDAO.getAssetByCode(assetCode.trim());
            if (before == null) {
                response.sendRedirect(redirectBase + "error=" + encode("Không tìm thấy thiết bị có mã " + assetCode + "!"));
                return;
            }

            boolean ok = assetDAO.reportAssetDamage(before.getCode(), damageNote.trim(), currentUser.getFullName());
            if (ok) {
                String oldVal = "Trạng thái: " + before.getStatusDisplayName() + " (" + before.getStatus() + ")";
                String newVal = "Trạng thái: Hỏng hóc (FAULTY)";
                String desc = "Báo cáo đồ hỏng hóc tại phòng " + before.getRoomId() + ": " + damageNote.trim();
                historyDAO.logChange("REPORT_DAMAGE", "ASSET", before.getCode(), before.getName(),
                        before.getRoomId(), oldVal, newVal, desc, currentUser);

                response.sendRedirect(redirectBase + "msg=" + encode("Đã gửi báo cáo hỏng hóc cho thiết bị [" + before.getCode() + " - " + before.getName() + "] tới Cán bộ Quản lý (Staff)!"));
            } else {
                response.sendRedirect(redirectBase + "error=" + encode("Không thể gửi báo cáo hỏng hóc. Vui lòng thử lại!"));
            }
            return;
        }

        // Kiểm tra quyền Staff hoặc Admin cho các thao tác quản trị bên dưới
        if (!currentUser.isStaffOrAdmin()) {
            response.sendRedirect(request.getContextPath() + "/assets?error=" + encode("Quyền hạn bị từ chối: Giảng viên chỉ có quyền xem tình trạng đồ vật và gửi báo cáo hỏng hóc!"));
            return;
        }

        // 1. Thêm mới tài sản (Admin / Staff)
        if ("create".equalsIgnoreCase(action)) {
            String code = request.getParameter("code");
            String name = request.getParameter("name");
            String category = request.getParameter("category");
            String serialNumber = request.getParameter("serialNumber");
            String handoverDate = request.getParameter("handoverDate");
            String priceStr = request.getParameter("price");
            String roomId = request.getParameter("roomId");
            String status = request.getParameter("status");
            String note = request.getParameter("note");

            if (code == null || code.trim().length() < 3) {
                response.sendRedirect(request.getContextPath() + "/assets?error=" + encode("Mã tài sản phải có ít nhất 3 ký tự!"));
                return;
            }
            if (name == null || name.trim().length() < 2) {
                response.sendRedirect(request.getContextPath() + "/assets?error=" + encode("Tên tài sản không được để trống!"));
                return;
            }
            if (assetDAO.getAssetByCode(code.trim()) != null) {
                response.sendRedirect(request.getContextPath() + "/assets?error=" + encode("Mã tài sản " + code.trim().toUpperCase() + " đã tồn tại trên hệ thống!"));
                return;
            }

            double price = 0;
            try {
                if (priceStr != null && !priceStr.isBlank()) {
                    price = Double.parseDouble(priceStr.replace(",", "").replace(".", "").trim());
                }
            } catch (NumberFormatException ex) {
                price = 0;
            }

            Asset a = new Asset(
                code.trim().toUpperCase(),
                name.trim(),
                category != null ? category.trim() : "Khác",
                serialNumber != null ? serialNumber.trim() : "",
                handoverDate != null && !handoverDate.isBlank() ? handoverDate.trim() : java.time.LocalDate.now().toString(),
                price,
                roomId != null ? roomId.trim() : "BE-301",
                status != null ? status.trim() : "ACTIVE",
                note != null ? note.trim() : ""
            );

            boolean ok = assetDAO.addAsset(a);
            if (ok) {
                String oldVal = "Chưa có";
                String newVal = "Phòng " + a.getRoomId() + " | Trạng thái: " + a.getStatusDisplayName();
                String desc = "Ghi nhận tài sản mới vào phòng " + a.getRoomId() + " (Danh mục: " + a.getCategory() + ", Đơn giá: " + a.getFormattedPrice() + ")";
                historyDAO.logChange("CREATE_ASSET", "ASSET", a.getCode(), a.getName(),
                        a.getRoomId(), oldVal, newVal, desc, currentUser);

                response.sendRedirect(request.getContextPath() + "/assets?msg=" + encode("Ghi nhận tài sản mới " + a.getCode() + " thành công!"));
            } else {
                response.sendRedirect(request.getContextPath() + "/assets?error=" + encode("Lỗi khi thêm tài sản vào cơ sở dữ liệu!"));
            }
            return;
        }

        // 2. Cập nhật thông tin tài sản (Admin / Staff)
        if ("update".equalsIgnoreCase(action)) {
            String code = request.getParameter("code");
            String name = request.getParameter("name");
            String category = request.getParameter("category");
            String serialNumber = request.getParameter("serialNumber");
            String handoverDate = request.getParameter("handoverDate");
            String priceStr = request.getParameter("price");
            String roomId = request.getParameter("roomId");
            String status = request.getParameter("status");
            String note = request.getParameter("note");

            Asset before = assetDAO.getAssetByCode(code != null ? code.trim() : "");

            double price = 0;
            try {
                if (priceStr != null && !priceStr.isBlank()) {
                    price = Double.parseDouble(priceStr.replace(",", "").replace(".", "").trim());
                }
            } catch (NumberFormatException ex) {
                price = 0;
            }

            Asset a = new Asset(
                code.trim(),
                name.trim(),
                category.trim(),
                serialNumber != null ? serialNumber.trim() : "",
                handoverDate != null ? handoverDate.trim() : "",
                price,
                roomId.trim(),
                status != null ? status.trim() : "ACTIVE",
                note != null ? note.trim() : ""
            );

            boolean ok = assetDAO.updateAsset(a);
            if (ok) {
                String oldVal = (before != null)
                        ? ("Phòng " + before.getRoomId() + " | " + before.getStatusDisplayName() + (before.getNote().isBlank() ? "" : " | " + before.getNote()))
                        : "Không xác định";
                String newVal = "Phòng " + a.getRoomId() + " | " + a.getStatusDisplayName() + (a.getNote().isBlank() ? "" : " | " + a.getNote());
                String desc = "Cập nhật thông tin / tình trạng tài sản " + a.getCode() + " (" + a.getName() + ")";
                historyDAO.logChange("UPDATE_ASSET", "ASSET", a.getCode(), a.getName(),
                        a.getRoomId(), oldVal, newVal, desc, currentUser);

                response.sendRedirect(request.getContextPath() + "/assets?msg=" + encode("Đã cập nhật thông tin tài sản " + a.getCode() + "!"));
            } else {
                response.sendRedirect(request.getContextPath() + "/assets?error=" + encode("Không thể cập nhật tài sản!"));
            }
            return;
        }

        // 3. Chuyển dời tài sản giữa các phòng học (Admin / Staff)
        if ("transfer".equalsIgnoreCase(action)) {
            String assetCode = request.getParameter("assetCode");
            String targetRoom = request.getParameter("targetRoom");
            Asset before = assetDAO.getAssetByCode(assetCode);
            boolean ok = assetDAO.transferAsset(assetCode, targetRoom);
            if (ok) {
                String fromRoom = (before != null) ? before.getRoomId() : "N/A";
                String assetName = (before != null) ? before.getName() : assetCode;
                String oldVal = "Phòng " + fromRoom;
                String newVal = "Phòng " + targetRoom;
                String desc = "Chuyển dời tài sản từ phòng " + fromRoom + " sang phòng " + targetRoom;
                historyDAO.logChange("TRANSFER_ASSET", "ASSET", assetCode, assetName,
                        targetRoom, oldVal, newVal, desc, currentUser);

                response.sendRedirect(request.getContextPath() + "/assets?msg=" + encode("Đã chuyển dời tài sản " + assetCode + " sang phòng " + targetRoom + " thành công!"));
            } else {
                response.sendRedirect(request.getContextPath() + "/assets?error=" + encode("Không thể chuyển dời tài sản!"));
            }
            return;
        }

        // 4. Xóa bản ghi tài sản (CHỈ ADMIN MỚI ĐƯỢC PHÉP XÓA)
        if ("delete".equalsIgnoreCase(action)) {
            if (!currentUser.isAdmin()) {
                response.sendRedirect(request.getContextPath() + "/assets?error=" + encode("Quyền hạn bị từ chối: Chức năng XÓA tài sản là chức năng nguy hiểm, chỉ Quản trị viên (Admin) mới có quyền thực hiện!"));
                return;
            }
            String code = request.getParameter("code");
            Asset before = assetDAO.getAssetByCode(code);
            boolean ok = assetDAO.deleteAsset(code);
            if (ok) {
                String room = (before != null) ? before.getRoomId() : "";
                String name = (before != null) ? before.getName() : code;
                String oldVal = (before != null) ? ("Phòng " + before.getRoomId() + " | " + before.getStatusDisplayName()) : "Tồn tại";
                String newVal = "Đã xóa khỏi hệ thống";
                String desc = "Xóa bản ghi tài sản " + code + " (" + name + ") khỏi phòng " + room;
                historyDAO.logChange("DELETE_ASSET", "ASSET", code, name,
                        room, oldVal, newVal, desc, currentUser);

                response.sendRedirect(request.getContextPath() + "/assets?msg=" + encode("Đã xóa bản ghi tài sản " + code + " khỏi hệ thống!"));
            } else {
                response.sendRedirect(request.getContextPath() + "/assets?error=" + encode("Không thể xóa tài sản " + code + "!"));
            }
            return;
        }

        response.sendRedirect(request.getContextPath() + "/assets");
    }

    private String encode(String text) {
        return URLEncoder.encode(text, StandardCharsets.UTF_8);
    }
}
