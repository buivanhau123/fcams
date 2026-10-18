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
 * Xem toàn bộ các tài sản và trang thiết bị hiện đang được trang bị bên trong phòng học được chọn.
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

        RoomDAO roomDAO = new RoomDAO();
        Room room = roomDAO.getRoomById(roomId);

        request.setAttribute("room", room);
        request.setAttribute("roomId", roomId);
        request.setAttribute("activeTab", "rooms");
        request.getRequestDispatcher("/room-detail.jsp").forward(request, response);
    }
}
