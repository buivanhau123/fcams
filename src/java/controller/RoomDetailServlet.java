package controller;

import dal.RoomDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import model.Room;

/**
 * Controller quản lý Xem chi tiết phòng học (Room Detail - Tiêu chí 4):
 * Xem toàn bộ các tài sản và trang thiết bị hiện đang được trang bị bên trong phòng học được chọn,
 * đồng thời hỗ trợ Giảng viên / Staff gửi báo cáo đồ hỏng hóc ngay tại phòng.
 */
@WebServlet(name = "RoomDetailServlet", urlPatterns = {"/room-detail"})
public class RoomDetailServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String roomId = request.getParameter("id");
        if (roomId == null || roomId.isBlank()) {
            roomId = "BE-301";
        }
        String reportCode = request.getParameter("reportCode");

        RoomDAO roomDAO = new RoomDAO();
        Room room = roomDAO.getRoomById(roomId);

        request.setAttribute("room", room);
        request.setAttribute("roomId", roomId);
        request.setAttribute("selectedReportCode", reportCode != null ? reportCode : "");
        request.setAttribute("activeTab", "rooms");
        request.getRequestDispatcher("/room-detail.jsp").forward(request, response);
    }
}
