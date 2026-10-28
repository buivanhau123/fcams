package dal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.User;

/**
 * DAO quản lý người dùng & xác thực đăng nhập trên SQL Server (ADMIN, STAFF, LECTURER).
 */
public class UserDAO extends DBContext {

    public User authenticate(String username, String password) {
        if (connection != null) {
            String sql = "SELECT username, password, full_name, role, description FROM dbo.users WHERE username = ? AND password = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, username);
                ps.setString(2, password);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String rawRole = rs.getString("role");
                        String normalizedRole = "IT_STAFF".equalsIgnoreCase(rawRole) ? "STAFF" : rawRole;
                        return new User(
                                rs.getString("username"),
                                rs.getString("password"),
                                rs.getNString("full_name"),
                                normalizedRole,
                                rs.getNString("description")
                        );
                    }
                }
            } catch (SQLException ex) {
                Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        // Fallback nếu SQL Server chưa khởi động
        for (User u : getFallbackUsers()) {
            if (u.getUsername().equalsIgnoreCase(username) && u.getPassword().equals(password)) {
                return u;
            }
        }
        return null;
    }

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        if (connection != null) {
            String sql = "SELECT username, password, full_name, role, description FROM dbo.users ORDER BY CASE role WHEN 'LECTURER' THEN 1 WHEN 'STAFF' THEN 2 ELSE 3 END";
            try (PreparedStatement ps = connection.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String rawRole = rs.getString("role");
                    String normalizedRole = "IT_STAFF".equalsIgnoreCase(rawRole) ? "STAFF" : rawRole;
                    list.add(new User(
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getNString("full_name"),
                            normalizedRole,
                            rs.getNString("description")
                    ));
                }
                if (!list.isEmpty()) {
                    return list;
                }
            } catch (SQLException ex) {
                Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return getFallbackUsers();
    }

    private List<User> getFallbackUsers() {
        List<User> list = new ArrayList<>();
        list.add(new User("hungnq", "123", "Thầy Nguyễn Quốc Hùng", "LECTURER",
                "Giảng viên (Lecturer): Xem danh sách phòng học, xem tình trạng các đồ vật trong phòng và gửi báo cáo đồ hỏng hóc cho Cán bộ Quản lý (Staff)."));
        list.add(new User("it_beta", "123", "Trần Văn Tuấn (Cán bộ Quản lý)", "STAFF",
                "Cán bộ Quản lý (Staff): Quản lý tài sản, xử lý báo cáo hỏng hóc, chuyển phòng tài sản, xem phòng học và lịch sử thay đổi."));
        list.add(new User("admin", "123", "Quản trị viên Hệ thống", "ADMIN",
                "Quản trị viên (Admin): Toàn quyền CRUD phòng học, tài sản, xử lý sự cố và xem lịch sử thay đổi toàn hệ thống."));
        return list;
    }
}
