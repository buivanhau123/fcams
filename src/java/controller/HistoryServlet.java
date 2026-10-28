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
import model.ChangeHistory;
import model.Room;
import model.User;

/**
 * Controller quản lý Nhật ký Lịch sử thay đổi & Báo cáo hỏng hóc (/history):
 * Dành riêng cho Quản trị viên (ADMIN) và Cán bộ Quản lý (STAFF) xem lịch sử thay đổi
 * tài sản, phòng học và các báo cáo đồ hỏng hóc từ Giảng viên.
 */
@WebServlet(name = "HistoryServlet", urlPatterns = {"/history"})
public class HistoryServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User currentUser = (User) request.getSession().getAttribute("currentUser");
        if (currentUser == null || !currentUser.isStaffOrAdmin()) {
            String errMsg = URLEncoder.encode("Quyền hạn bị từ chối: Trang Lịch sử thay đổi chỉ dành cho Quản trị viên (Admin) và Cán bộ Quản lý (Staff)!", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/dashboard?error=" + errMsg);
            return;
        }

        String actionType = request.getParameter("actionType");
        String roomId = request.getParameter("roomId");
        String roleFilter = request.getParameter("roleFilter");
        String keyword = request.getParameter("keyword");

        HistoryDAO historyDAO = new HistoryDAO();
        RoomDAO roomDAO = new RoomDAO();

        List<ChangeHistory> historyList = historyDAO.filterHistory(actionType, roomId, roleFilter, keyword);
        List<Room> rooms = roomDAO.getAllRooms();

        request.setAttribute("historyList", historyList);
        request.setAttribute("rooms", rooms);
        request.setAttribute("selectedAction", actionType != null ? actionType : "ALL");
        request.setAttribute("selectedRoom", roomId != null ? roomId : "ALL");
        request.setAttribute("selectedRole", roleFilter != null ? roleFilter : "ALL");
        request.setAttribute("keyword", keyword != null ? keyword : "");
        request.setAttribute("activeTab", "history");

        request.getRequestDispatcher("/history.jsp").forward(request, response);
    }
}
