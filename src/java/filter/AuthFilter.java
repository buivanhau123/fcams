package filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import model.User;

/**
 * Filter phân quyền và bảo mật hệ thống (Tiêu chí 1: Authentication & Authorization):
 * - ADMIN: Toàn quyền hệ thống (Dashboard, Tài sản CRUD + Xóa, Phòng học CRUD + Xóa, Xem Lịch sử thay đổi).
 * - STAFF: Quản lý Tài sản (Thêm, Sửa, Chuyển phòng, Xử lý báo hỏng), Xem Phòng học, Xem Lịch sử thay đổi. Bị hạn chế Xóa tài sản và Sửa/Xóa phòng.
 * - LECTURER (Giảng viên): Xem tình trạng phòng học & các đồ vật trong phòng, Gửi báo cáo đồ hỏng hóc cho Staff. Không được Thêm/Sửa/Xóa/Chuyển tài sản và không được xem trang Lịch sử.
 */
@WebFilter(filterName = "AuthFilter", urlPatterns = {"/*"})
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        req.setCharacterEncoding("UTF-8");
        res.setCharacterEncoding("UTF-8");

        String contextPath = req.getContextPath();
        String uri = req.getRequestURI();
        String path = uri.substring(contextPath.length());

        // 1. Cho phép truy cập trang đăng nhập, đăng xuất và tài nguyên tĩnh
        if (path.startsWith("/login")
                || path.startsWith("/logout")
                || path.endsWith(".css")
                || path.endsWith(".js")
                || path.endsWith(".ico")
                || path.endsWith(".png")
                || path.endsWith(".jpg")) {
            chain.doFilter(request, response);
            return;
        }

        // 2. Chặn truy cập trực tiếp file .jsp hoặc .html (bắt buộc đi qua Servlet Controller)
        if (path.endsWith(".html") || path.endsWith(".jsp")) {
            String clean = path.replace(".html", "").replace(".jsp", "");
            if ("/index".equals(clean)) {
                res.sendRedirect(contextPath + "/dashboard");
            } else {
                res.sendRedirect(contextPath + clean + (req.getQueryString() != null ? "?" + req.getQueryString() : ""));
            }
            return;
        }

        // 3. Kiểm tra xác thực đăng nhập trong HttpSession (Authentication)
        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;

        if (currentUser == null) {
            res.sendRedirect(contextPath + "/login");
            return;
        }

        // 4. Phân quyền theo vai trò (Authorization & Security)
        String action = req.getParameter("action");
        String editId = req.getParameter("editId");
        String editCode = req.getParameter("editCode");
        String transferCode = req.getParameter("transferCode");

        // 4.1. Chặn Giảng viên (LECTURER) truy cập trang Lịch sử (/history) - Chỉ ADMIN và STAFF được xem
        if (path.startsWith("/history")) {
            if (!currentUser.isStaffOrAdmin()) {
                String errMsg = URLEncoder.encode("Quyền hạn bị từ chối: Trang Lịch sử thay đổi chỉ dành cho Quản trị viên (Admin) và Cán bộ Quản lý (Staff)!", StandardCharsets.UTF_8);
                res.sendRedirect(contextPath + "/dashboard?error=" + errMsg);
                return;
            }
        }

        // 4.2. Chặn Giảng viên (LECTURER) thực hiện Thêm / Sửa / Chuyển / Xóa tài sản (chỉ được Xem và Báo hỏng)
        if (path.startsWith("/assets")) {
            if (currentUser.isLecturer()) {
                if ((editCode != null && !editCode.isBlank()) || (transferCode != null && !transferCode.isBlank())) {
                    String errMsg = URLEncoder.encode("Quyền hạn bị từ chối: Giảng viên chỉ được xem tình trạng thiết bị và gửi báo cáo hỏng hóc!", StandardCharsets.UTF_8);
                    res.sendRedirect(contextPath + "/assets?error=" + errMsg);
                    return;
                }
                if ("POST".equalsIgnoreCase(req.getMethod()) && !"reportDamage".equalsIgnoreCase(action)) {
                    String errMsg = URLEncoder.encode("Quyền hạn bị từ chối: Giảng viên chỉ có quyền gửi Báo cáo đồ hỏng hóc!", StandardCharsets.UTF_8);
                    res.sendRedirect(contextPath + "/assets?error=" + errMsg);
                    return;
                }
            }
        }

        // 4.3. Ngăn chặn chức năng NGUY HIỂM 1: XÓA tài sản hoặc XÓA phòng học (Chỉ Admin mới có quyền)
        if ("delete".equalsIgnoreCase(action)) {
            if (!currentUser.isAdmin()) {
                String referer = req.getHeader("Referer");
                String redirectTarget = (referer != null && !referer.isBlank()) ? referer : (contextPath + "/dashboard");
                String separator = redirectTarget.contains("?") ? "&" : "?";
                String errMsg = URLEncoder.encode("Quyền hạn bị từ chối: Chức năng XÓA (tài sản / phòng học) là thao tác nguy hiểm, chỉ Quản trị viên (Admin) mới có quyền thực hiện!", StandardCharsets.UTF_8);
                res.sendRedirect(redirectTarget + separator + "error=" + errMsg);
                return;
            }
        }

        // 4.4. Ngăn chặn chức năng NGUY HIỂM 2: SỬA hoặc THAY ĐỔI thông số phòng học (Chỉ Admin mới có quyền)
        if (path.startsWith("/rooms")) {
            if (("update".equalsIgnoreCase(action) || "create".equalsIgnoreCase(action) || (editId != null && !editId.isBlank()))
                    && !currentUser.isAdmin()) {
                String errMsg = URLEncoder.encode("Quyền hạn bị từ chối: Thao tác Thêm / Sửa thông số phòng học là chức năng nguy hiểm, chỉ Quản trị viên (Admin) mới có quyền thực hiện!", StandardCharsets.UTF_8);
                res.sendRedirect(contextPath + "/rooms?error=" + errMsg);
                return;
            }
            if ("reset".equalsIgnoreCase(action) && !currentUser.isStaffOrAdmin()) {
                String errMsg = URLEncoder.encode("Quyền hạn bị từ chối: Chỉ Quản trị viên hoặc Cán bộ Quản lý mới được đặt lại dữ liệu!", StandardCharsets.UTF_8);
                res.sendRedirect(contextPath + "/rooms?error=" + errMsg);
                return;
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
