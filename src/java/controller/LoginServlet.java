package controller;

import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import model.User;

/**
 * Controller xử lý đăng nhập và hiển thị danh sách tác nhân hệ thống (Tiêu chí 1.1 & 1.5).
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("currentUser") != null) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            request.setAttribute("errorMsg", "Vui lòng nhập đầy đủ Username và Password!");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }

        UserDAO userDAO = new UserDAO();
        User user = userDAO.authenticate(username.trim(), password.trim());
        if (user != null) {
            HttpSession session = request.getSession(true);
            session.setAttribute("currentUser", user);
            response.sendRedirect(request.getContextPath() + "/dashboard");
        } else {
            request.setAttribute("errorMsg", "Sai Username hoặc Password! Vui lòng thử lại.");
            request.setAttribute("enteredUsername", username.trim());
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }
}

