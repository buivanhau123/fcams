package controller;

import dal.AssetDAO;
import dal.RoomDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import model.DashboardStats;
import model.Room;

/**
 * Controller Bảng điều khiển Thống kê (DashboardServlet - Tiêu chí 2):
 * - Hiển thị các chỉ số tổng quan: Tổng số tài sản, tổng số phòng học, giá trị tài sản ước tính.
 * - Thống kê tỷ lệ tình trạng tài sản (Đang hoạt động tốt, Đang bảo trì, Hỏng hóc).
 * - Danh sách tài sản mới được thêm và tài sản cần bảo trì khẩn cấp.
 */
@WebServlet(name = "DashboardServlet", urlPatterns = {"/dashboard"})
public class DashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        RoomDAO roomDAO = new RoomDAO();
        AssetDAO assetDAO = new AssetDAO();

        List<Room> rooms = roomDAO.getAllRooms();
        DashboardStats stats = assetDAO.getDashboardStats(rooms.size());

        request.setAttribute("stats", stats);
        request.setAttribute("rooms", rooms);
        request.setAttribute("activeTab", "dashboard");

        request.getRequestDispatcher("/dashboard.jsp").forward(request, response);
    }
}

