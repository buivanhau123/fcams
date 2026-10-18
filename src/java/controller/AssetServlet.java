package controller;

import dal.AssetDAO;
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
 * Controller Quản lý Tài sản (Asset Management - CRUD - Tiêu chí 3):
 * - Danh sách & Tìm kiếm: Liệt kê toàn bộ tài sản kèm bộ lọc (Phòng học, Danh mục, Trạng thái) và tìm kiếm theo tên/mã tài sản.
 * - Thêm mới: Ghi nhận tài sản mới với đầy đủ thông số (Mã, Tên, Số serial, Ngày bàn giao, Đơn giá, Phòng bố trí).
 * - Cập nhật: Sửa thông tin tài sản, chuyển dời tài sản giữa các phòng học.
 * - Xóa: Xóa bản ghi tài sản (chỉ dành cho quyền Admin).
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

        List<Asset> assets = assetDAO.filterAssets(roomId, category, status, keyword);
        List<Room> rooms = roomDAO.getAllRooms();
        List<String> categories = assetDAO.getCategories();

        if (editCode != null && !editCode.isBlank()) {
            request.setAttribute("editAsset", assetDAO.getAssetByCode(editCode));
        }
        if (transferCode != null && !transferCode.isBlank()) {
            request.setAttribute("transferAsset", assetDAO.getAssetByCode(transferCode));
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
        User currentUser = (User) request.getSession().getAttribute("currentUser");

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
            boolean ok = assetDAO.transferAsset(assetCode, targetRoom);
            if (ok) {
                response.sendRedirect(request.getContextPath() + "/assets?msg=" + encode("Đã chuyển dời tài sản " + assetCode + " sang phòng " + targetRoom + " thành công!"));
            } else {
                response.sendRedirect(request.getContextPath() + "/assets?error=" + encode("Không thể chuyển dời tài sản!"));
            }
            return;
        }

        // 4. Xóa bản ghi tài sản (CHỈ ADMIN MỚI ĐƯỢC PHÉP XÓA)
        if ("delete".equalsIgnoreCase(action)) {
            if (currentUser == null || !currentUser.isAdmin()) {
                response.sendRedirect(request.getContextPath() + "/assets?error=" + encode("Quyền hạn bị từ chối: Chức năng XÓA tài sản là chức năng nguy hiểm, chỉ Quản trị viên (Admin) mới có quyền thực hiện!"));
                return;
            }
            String code = request.getParameter("code");
            boolean ok = assetDAO.deleteAsset(code);
            if (ok) {
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

