package controller;

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
import model.Room;
import model.User;

/**
 * Controller quản lý Danh sách phòng học & Quản lý Phòng học (Room Management - CRUD):
 * Danh mục phòng học, Thêm / Sửa / Xóa phòng, số lượng chỗ ngồi và mục đích sử dụng.
 * Tự động ghi lịch sử thay đổi vào bảng change_history.
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

        // Nếu Staff hoặc Lecturer cố truy cập tham số editId để sửa phòng -> từ chối
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
        HistoryDAO historyDAO = new HistoryDAO();
        User currentUser = (User) request.getSession().getAttribute("currentUser");

        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        if ("reset".equalsIgnoreCase(action)) {
            if (!currentUser.isStaffOrAdmin()) {
                response.sendRedirect(request.getContextPath() + "/rooms?error=" + encode("Quyền hạn bị từ chối: Chỉ Quản trị viên hoặc Cán bộ Quản lý mới được đặt lại dữ liệu!"));
                return;
            }
            boolean ok = roomDAO.resetSampleData();
            String msg = ok ? "Đã đặt lại dữ liệu mẫu ban đầu thành công!" : "Không thể đặt lại dữ liệu mẫu.";
            response.sendRedirect(request.getContextPath() + "/rooms?msg=" + encode(msg));
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
                String roomCode = id.trim().toUpperCase();
                boolean ok = roomDAO.createRoom(roomCode, building, floor, std, cur, purpose, projName, acLeftName, acRightName, tvName);
                if (ok) {
                    String newVal = "Tòa " + building + " - Tầng " + floor + " | Ghế: " + cur + "/" + std + " | " + purpose;
                    historyDAO.logChange("CREATE_ROOM", "ROOM", roomCode, "Phòng học " + roomCode + " (Tòa " + building + ")",
                            roomCode, "Chưa có", newVal,
                            "Khởi tạo phòng học mới " + roomCode + " kèm 4 thiết bị tiêu chuẩn", currentUser);

                    response.sendRedirect(request.getContextPath() + "/rooms?msg=" + encode("Đã thêm mới phòng học " + roomCode + " (" + purpose + ") kèm tài sản thành công!"));
                } else {
                    response.sendRedirect(request.getContextPath() + "/rooms?error=" + encode("Lỗi CSDL khi thêm phòng học mới!"));
                }
            } catch (NumberFormatException ex) {
                response.sendRedirect(request.getContextPath() + "/rooms?error=" + encode("Ràng buộc dữ liệu: Tầng và số lượng ghế phải là số nguyên hợp lệ!"));
            }
            return;
        }

        // Chức năng SỬA PHÒNG (Admin only)
        if ("update".equalsIgnoreCase(action)) {
            if (!currentUser.isAdmin()) {
                response.sendRedirect(request.getContextPath() + "/rooms?error=" + encode("Quyền hạn bị từ chối: Chức năng SỬA phòng học là chức năng nguy hiểm, chỉ Quản trị viên (Admin) mới có quyền thực hiện!"));
                return;
            }
            String id = request.getParameter("id");
            String building = request.getParameter("building");
            String purpose = request.getParameter("purpose");
            Room before = roomDAO.getRoomById(id);
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
                    String oldVal = (before != null)
                            ? ("Tòa " + before.getBuilding() + " - Tầng " + before.getFloor() + " | Ghế: " + before.getCur() + "/" + before.getStd() + " | " + before.getPurpose())
                            : "Không xác định";
                    String newVal = "Tòa " + building + " - Tầng " + floor + " | Ghế: " + cur + "/" + std + " | " + purpose;
                    historyDAO.logChange("UPDATE_ROOM", "ROOM", id, "Phòng học " + id + " (Tòa " + building + ")",
                            id, oldVal, newVal,
                            "Cập nhật thông số phòng học " + id, currentUser);

                    response.sendRedirect(request.getContextPath() + "/rooms?msg=" + encode("Đã cập nhật thông tin phòng học " + id + " thành công!"));
                } else {
                    response.sendRedirect(request.getContextPath() + "/rooms?error=" + encode("Không tìm thấy phòng học để cập nhật!"));
                }
            } catch (NumberFormatException ex) {
                response.sendRedirect(request.getContextPath() + "/rooms?error=" + encode("Dữ liệu số không hợp lệ!"));
            }
            return;
        }

        // Chức năng XÓA PHÒNG (Admin only)
        if ("delete".equalsIgnoreCase(action)) {
            if (!currentUser.isAdmin()) {
                response.sendRedirect(request.getContextPath() + "/rooms?error=" + encode("Quyền hạn bị từ chối: Chức năng XÓA phòng học là chức năng nguy hiểm, chỉ Quản trị viên (Admin) mới có quyền thực hiện!"));
                return;
            }
            String id = request.getParameter("id");
            Room before = roomDAO.getRoomById(id);
            boolean ok = roomDAO.deleteRoom(id);
            if (ok) {
                String oldVal = (before != null)
                        ? ("Tòa " + before.getBuilding() + " - Tầng " + before.getFloor() + " | Ghế: " + before.getCur() + "/" + before.getStd())
                        : "Tồn tại";
                historyDAO.logChange("DELETE_ROOM", "ROOM", id, "Phòng học " + id,
                        id, oldVal, "Đã xóa khỏi hệ thống",
                        "Xóa phòng học " + id + " và các tài sản trực thuộc", currentUser);

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
