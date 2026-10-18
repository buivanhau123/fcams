package controller;

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
import model.Room;
import model.User;

/**
 * Controller quản lý Danh sách phòng học & Quản lý Phòng học (Room Management - CRUD):
 * Danh mục phòng học, Thêm / Sửa / Xóa phòng, số lượng chỗ ngồi và mục đích sử dụng.
 * Hạn chế chức năng nguy hiểm (Xóa / Sửa phòng): Chỉ dành riêng cho quyền Admin.
 */
@WebServlet(name = "RoomServlet", urlPatterns = {"/rooms"})
public class RoomServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        RoomDAO roomDAO = new RoomDAO();
        String building = request.getParameter("building");
        String status = request.getParameter("status");
        String search = request.getParameter("search");
        String editId = request.getParameter("editId");

        User currentUser = (User) request.getSession().getAttribute("currentUser");

        // Nếu Staff cố truy cập tham số editId để sửa phòng -> từ chối
        if (editId != null && !editId.isBlank()) {
            if (currentUser != null && !currentUser.isAdmin()) {
                response.sendRedirect(request.getContextPath() + "/rooms?error=" + encode("Quyền hạn bị từ chối: Chức năng SỬA phòng học là chức năng nguy hiểm, chỉ Quản trị viên (Admin) mới có quyền thực hiện!"));
                return;
            }
            Room editRoom = roomDAO.getRoomById(editId);
            request.setAttribute("editRoom", editRoom);
        }

        List<Room> allRooms = roomDAO.getAllRooms();
        List<Room> rooms = roomDAO.filterRooms(building, status, search);

        request.setAttribute("rooms", rooms);
        request.setAttribute("totalSystemRooms", allRooms.size());
        request.setAttribute("selectedBuilding", building);
        request.setAttribute("selectedStatus", status);
        request.setAttribute("searchQuery", search);
        request.setAttribute("activeTab", "rooms");

        request.getRequestDispatcher("/rooms.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        RoomDAO roomDAO = new RoomDAO();
        User currentUser = (User) request.getSession().getAttribute("currentUser");

        if ("reset".equalsIgnoreCase(action)) {
            boolean ok = roomDAO.resetSampleData();
            String msg = ok ? "Đã đặt lại dữ liệu mẫu ban đầu thành công!" : "Không thể đặt lại dữ liệu mẫu.";
            response.sendRedirect(request.getContextPath() + "/rooms?msg=" + encode(msg));
            return;
        }

        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // Chức năng THÊM PHÒNG
        if ("create".equalsIgnoreCase(action)) {
            if (!currentUser.isAdmin()) {
                response.sendRedirect(request.getContextPath() + "/rooms?error=" + encode("Quyền hạn bị từ chối: Chức năng THÊM phòng học chỉ dành cho Quản trị viên (Admin)!"));
                return;
            }

            String id = request.getParameter("id");
            String building = request.getParameter("building");
            String floorStr = request.getParameter("floor");
            String stdStr = request.getParameter("stdChairs");
            String curStr = request.getParameter("curChairs");
            String purpose = request.getParameter("purpose");
            String projName = request.getParameter("projName");
            String acLeftName = request.getParameter("acLeftName");
            String acRightName = request.getParameter("acRightName");
            String tvName = request.getParameter("tvName");

            if (id == null || !id.trim().toUpperCase().matches("^[A-Z]{2}-\\d{3}$")) {
                response.sendRedirect(request.getContextPath() + "/rooms?error=" + encode("Ràng buộc dữ liệu: Mã phòng phải đúng định dạng [2 chữ cái]-[3 chữ số] (VD: BE-303, AL-102)!"));
                return;
            }
            if (roomDAO.getRoomById(id.trim()) != null) {
                response.sendRedirect(request.getContextPath() + "/rooms?error=" + encode("Ràng buộc dữ liệu: Mã phòng " + id.trim().toUpperCase() + " đã tồn tại trong hệ thống!"));
                return;
            }

            try {
                int floor = Integer.parseInt(floorStr);
                int std = Integer.parseInt(stdStr);
                int cur = Integer.parseInt(curStr);
                if (floor < 1 || floor > 10 || std < 10 || std > 100 || cur < 0 || cur > 150) {
                    response.sendRedirect(request.getContextPath() + "/rooms?error=" + encode("Ràng buộc dữ liệu: Tầng (1-10), Ghế chuẩn (10-100), Ghế hiện có (0-150)!"));
                    return;
                }
                boolean ok = roomDAO.createRoom(id.trim().toUpperCase(), building, floor, std, cur, purpose, projName, acLeftName, acRightName, tvName);
                if (ok) {
                    response.sendRedirect(request.getContextPath() + "/rooms?msg=" + encode("Đã thêm mới phòng học " + id.trim().toUpperCase() + " (" + purpose + ") kèm tài sản thành công!"));
                } else {
                    response.sendRedirect(request.getContextPath() + "/rooms?error=" + encode("Lỗi CSDL khi thêm phòng học mới!"));
                }
            } catch (NumberFormatException ex) {
                response.sendRedirect(request.getContextPath() + "/rooms?error=" + encode("Ràng buộc dữ liệu: Tầng và số lượng ghế phải là số nguyên hợp lệ!"));
            }
            return;
        }

        // Chức năng SỬA PHÒNG (Admin only - hạn chế chức năng nguy hiểm đối với Staff)
        if ("update".equalsIgnoreCase(action)) {
            if (!currentUser.isAdmin()) {
                response.sendRedirect(request.getContextPath() + "/rooms?error=" + encode("Quyền hạn bị từ chối: Chức năng SỬA phòng học là chức năng nguy hiểm, chỉ Quản trị viên (Admin) mới có quyền thực hiện!"));
                return;
            }
            String id = request.getParameter("id");
            String building = request.getParameter("building");
            String purpose = request.getParameter("purpose");
            try {
                int floor = Integer.parseInt(request.getParameter("floor"));
                int std = Integer.parseInt(request.getParameter("stdChairs"));
                int cur = Integer.parseInt(request.getParameter("curChairs"));
                if (floor < 1 || floor > 10 || std < 10 || std > 100 || cur < 0 || cur > 150) {
                    response.sendRedirect(request.getContextPath() + "/rooms?error=" + encode("Ràng buộc dữ liệu: Tầng (1-10), Ghế chuẩn (10-100), Ghế hiện có (0-150)!"));
                    return;
                }
                boolean ok = roomDAO.updateRoom(id, building, floor, std, cur, purpose);
                if (ok) {
                    response.sendRedirect(request.getContextPath() + "/rooms?msg=" + encode("Đã cập nhật thông tin phòng học " + id + " thành công!"));
                } else {
                    response.sendRedirect(request.getContextPath() + "/rooms?error=" + encode("Không tìm thấy phòng học để cập nhật!"));
                }
            } catch (NumberFormatException ex) {
                response.sendRedirect(request.getContextPath() + "/rooms?error=" + encode("Dữ liệu số không hợp lệ!"));
            }
            return;
        }

        // Chức năng XÓA PHÒNG (Admin only - hạn chế chức năng nguy hiểm đối với Staff)
        if ("delete".equalsIgnoreCase(action)) {
            if (!currentUser.isAdmin()) {
                response.sendRedirect(request.getContextPath() + "/rooms?error=" + encode("Quyền hạn bị từ chối: Chức năng XÓA phòng học là chức năng nguy hiểm, chỉ Quản trị viên (Admin) mới có quyền thực hiện!"));
                return;
            }
            String id = request.getParameter("id");
            boolean ok = roomDAO.deleteRoom(id);
            if (ok) {
                response.sendRedirect(request.getContextPath() + "/rooms?msg=" + encode("Đã xóa phòng học " + id + " khỏi hệ thống!"));
            } else {
                response.sendRedirect(request.getContextPath() + "/rooms?error=" + encode("Không thể xóa phòng học " + id + "!"));
            }
            return;
        }

        response.sendRedirect(request.getContextPath() + "/rooms");
    }

    private String encode(String text) {
        return URLEncoder.encode(text, StandardCharsets.UTF_8);
    }
}
