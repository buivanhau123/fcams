package dal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.ChangeHistory;
import model.User;

/**
 * DAO quản lý Nhật ký Lịch sử thay đổi hệ thống & Báo cáo hỏng hóc (dbo.change_history).
 */
public class HistoryDAO extends DBContext {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public HistoryDAO() {
        super();
        ensureTableExists();
    }

    private void ensureTableExists() {
        if (connection == null) {
            return;
        }
        String sql = "IF OBJECT_ID('dbo.change_history', 'U') IS NULL "
                + "BEGIN "
                + "CREATE TABLE dbo.change_history ("
                + "id INT IDENTITY(1,1) PRIMARY KEY, "
                + "action_type VARCHAR(30) NOT NULL, "
                + "target_type VARCHAR(20) NOT NULL DEFAULT 'ASSET', "
                + "target_code VARCHAR(50) NOT NULL, "
                + "target_name NVARCHAR(150) NOT NULL, "
                + "room_id VARCHAR(20) NULL, "
                + "old_value NVARCHAR(300) NULL, "
                + "new_value NVARCHAR(300) NULL, "
                + "description NVARCHAR(500) NOT NULL, "
                + "performed_by VARCHAR(50) NOT NULL, "
                + "performer_name NVARCHAR(100) NOT NULL, "
                + "performer_role VARCHAR(20) NOT NULL, "
                + "created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME()"
                + "); "
                + "END";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.execute();
        } catch (SQLException ex) {
            Logger.getLogger(HistoryDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean logChange(String actionType, String targetType, String targetCode,
                             String targetName, String roomId, String oldValue,
                             String newValue, String description, User user) {
        if (connection == null) {
            return false;
        }
        String username = (user != null && user.getUsername() != null) ? user.getUsername() : "system";
        String fullName = (user != null && user.getFullName() != null) ? user.getFullName() : "Hệ thống";
        String role = (user != null && user.getRole() != null) ? user.getRole() : "STAFF";

        String sql = "INSERT INTO dbo.change_history "
                + "(action_type, target_type, target_code, target_name, room_id, old_value, new_value, description, performed_by, performer_name, performer_role) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, actionType != null ? actionType : "UPDATE_ASSET");
            ps.setString(2, targetType != null ? targetType : "ASSET");
            ps.setString(3, targetCode != null ? targetCode : "");
            ps.setNString(4, targetName != null ? targetName : "");
            ps.setString(5, roomId != null ? roomId : "");
            ps.setNString(6, oldValue != null ? oldValue : "");
            ps.setNString(7, newValue != null ? newValue : "");
            ps.setNString(8, description != null ? description : "");
            ps.setString(9, username);
            ps.setNString(10, fullName);
            ps.setString(11, role);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            Logger.getLogger(HistoryDAO.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public List<ChangeHistory> filterHistory(String actionType, String roomId, String roleFilter, String keyword) {
        List<ChangeHistory> list = new ArrayList<>();
        if (connection == null) {
            return list;
        }
        StringBuilder sql = new StringBuilder(
                "SELECT id, action_type, target_type, target_code, target_name, room_id, old_value, new_value, "
                + "description, performed_by, performer_name, performer_role, created_at "
                + "FROM dbo.change_history WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();

        if (actionType != null && !actionType.isBlank() && !"ALL".equalsIgnoreCase(actionType)) {
            sql.append("AND action_type = ? ");
            params.add(actionType.trim());
        }
        if (roomId != null && !roomId.isBlank() && !"ALL".equalsIgnoreCase(roomId)) {
            sql.append("AND room_id = ? ");
            params.add(roomId.trim());
        }
        if (roleFilter != null && !roleFilter.isBlank() && !"ALL".equalsIgnoreCase(roleFilter)) {
            sql.append("AND performer_role = ? ");
            params.add(roleFilter.trim());
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND (target_code LIKE ? OR target_name LIKE ? OR description LIKE ? OR performer_name LIKE ? OR performed_by LIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }

        sql.append("ORDER BY created_at DESC, id DESC");

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapHistory(rs));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(HistoryDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return list;
    }

    private ChangeHistory mapHistory(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("created_at");
        String timeStr = (ts != null) ? ts.toLocalDateTime().format(FMT) : "";
        return new ChangeHistory(
                rs.getInt("id"),
                rs.getString("action_type"),
                rs.getString("target_type"),
                rs.getString("target_code"),
                rs.getNString("target_name"),
                rs.getString("room_id"),
                rs.getNString("old_value"),
                rs.getNString("new_value"),
                rs.getNString("description"),
                rs.getString("performed_by"),
                rs.getNString("performer_name"),
                rs.getString("performer_role"),
                timeStr
        );
    }
}
