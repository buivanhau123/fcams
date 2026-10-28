package dal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Asset;
import model.DashboardStats;

/**
 * Data Access Object quản lý toàn diện Tài sản phòng học (Asset Management - CRUD),
 * Báo cáo hỏng hóc cho Giảng viên (Lecturer) và cung cấp số liệu thống kê cho Dashboard.
 */
public class AssetDAO extends DBContext {

    public List<Asset> getAllAssets() {
        List<Asset> list = new ArrayList<>();
        if (connection == null) {
            return list;
        }
        String sql = "SELECT code, name, category, serial_number, handover_date, price, room_id, status, note, created_at "
                   + "FROM dbo.assets ORDER BY room_id, category, code";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Asset a = mapAsset(rs);
                list.add(a);
            }
        } catch (SQLException ex) {
            Logger.getLogger(AssetDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return list;
    }

    public List<Asset> filterAssets(String roomId, String category, String status, String keyword) {
        List<Asset> list = new ArrayList<>();
        if (connection == null) {
            return list;
        }
        StringBuilder sql = new StringBuilder(
            "SELECT code, name, category, serial_number, handover_date, price, room_id, status, note, created_at "
          + "FROM dbo.assets WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();

        if (roomId != null && !roomId.isBlank() && !"ALL".equalsIgnoreCase(roomId)) {
            sql.append("AND room_id = ? ");
            params.add(roomId.trim());
        }
        if (category != null && !category.isBlank() && !"ALL".equalsIgnoreCase(category)) {
            sql.append("AND category = ? ");
            params.add(category.trim());
        }
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            String st = status.trim().toUpperCase();
            if ("FAULTY".equals(st) || "BROKEN".equals(st)) {
                sql.append("AND status IN ('FAULTY', 'BROKEN') ");
            } else if ("REPAIRING".equals(st) || "MAINTENANCE".equals(st)) {
                sql.append("AND status IN ('REPAIRING', 'MAINTENANCE') ");
            } else {
                sql.append("AND status = ? ");
                params.add(st);
            }
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND (code LIKE ? OR name LIKE ? OR serial_number LIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }

        sql.append("ORDER BY room_id, category, code");

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapAsset(rs));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(AssetDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return list;
    }

    public Asset getAssetByCode(String code) {
        if (connection == null || code == null) {
            return null;
        }
        String sql = "SELECT code, name, category, serial_number, handover_date, price, room_id, status, note, created_at "
                   + "FROM dbo.assets WHERE code = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, code.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapAsset(rs);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(AssetDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public List<Asset> getAssetsByRoom(String roomId) {
        List<Asset> list = new ArrayList<>();
        if (connection == null || roomId == null) {
            return list;
        }
        String sql = "SELECT code, name, category, serial_number, handover_date, price, room_id, status, note, created_at "
                   + "FROM dbo.assets WHERE room_id = ? ORDER BY category, code";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, roomId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapAsset(rs));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(AssetDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return list;
    }

    public List<String> getCategories() {
        List<String> list = new ArrayList<>();
        if (connection == null) {
            return list;
        }
        String sql = "SELECT DISTINCT category FROM dbo.assets ORDER BY category";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(rs.getNString("category"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(AssetDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return list;
    }

    /**
     * CREATE: Ghi nhận tài sản mới với đầy đủ thông số
     */
    public boolean addAsset(Asset a) {
        if (connection == null || a == null) {
            return false;
        }
        String sql = "INSERT INTO dbo.assets (code, name, category, serial_number, handover_date, price, room_id, status, note) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, a.getCode().trim().toUpperCase());
            ps.setNString(2, a.getName().trim());
            ps.setNString(3, a.getCategory().trim());
            ps.setString(4, a.getSerialNumber() != null ? a.getSerialNumber().trim() : "");
            ps.setString(5, a.getHandoverDate() != null && !a.getHandoverDate().isBlank() ? a.getHandoverDate().trim() : java.time.LocalDate.now().toString());
            ps.setDouble(6, a.getPrice());
            ps.setString(7, a.getRoomId().trim());
            ps.setString(8, a.getStatus() != null ? a.getStatus().trim() : "ACTIVE");
            ps.setNString(9, a.getNote() != null ? a.getNote().trim() : "");
            boolean ok = ps.executeUpdate() > 0;
            if (ok) {
                syncRoomAssetStatus(a.getCode(), a.getRoomId(), a.getStatus(), a.getNote());
            }
            return ok;
        } catch (SQLException ex) {
            Logger.getLogger(AssetDAO.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    /**
     * UPDATE: Cập nhật thông tin tài sản
     */
    public boolean updateAsset(Asset a) {
        if (connection == null || a == null) {
            return false;
        }
        String sql = "UPDATE dbo.assets SET name = ?, category = ?, serial_number = ?, handover_date = ?, price = ?, room_id = ?, status = ?, note = ? "
                   + "WHERE code = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setNString(1, a.getName().trim());
            ps.setNString(2, a.getCategory().trim());
            ps.setString(3, a.getSerialNumber() != null ? a.getSerialNumber().trim() : "");
            ps.setString(4, a.getHandoverDate());
            ps.setDouble(5, a.getPrice());
            ps.setString(6, a.getRoomId().trim());
            ps.setString(7, a.getStatus());
            ps.setNString(8, a.getNote() != null ? a.getNote().trim() : "");
            ps.setString(9, a.getCode().trim());
            boolean ok = ps.executeUpdate() > 0;
            if (ok) {
                syncRoomAssetStatus(a.getCode(), a.getRoomId(), a.getStatus(), a.getNote());
            }
            return ok;
        } catch (SQLException ex) {
            Logger.getLogger(AssetDAO.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    /**
     * UPDATE: Giảng viên / Staff báo cáo đồ hỏng hóc trong phòng học
     */
    public boolean reportAssetDamage(String assetCode, String damageDescription, String reporterName) {
        if (connection == null || assetCode == null) {
            return false;
        }
        Asset current = getAssetByCode(assetCode);
        if (current == null) {
            return false;
        }
        String formattedNote = "[Báo hỏng bởi " + reporterName + "]: " + damageDescription.trim();
        String sql = "UPDATE dbo.assets SET status = 'FAULTY', note = ? WHERE code = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setNString(1, formattedNote);
            ps.setString(2, assetCode.trim());
            boolean ok = ps.executeUpdate() > 0;
            if (ok) {
                syncRoomAssetStatus(assetCode.trim(), current.getRoomId(), "FAULTY", formattedNote);
            }
            return ok;
        } catch (SQLException ex) {
            Logger.getLogger(AssetDAO.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    private void syncRoomAssetStatus(String assetCode, String roomId, String status, String note) {
        if (connection == null || assetCode == null || roomId == null) {
            return;
        }
        String upper = assetCode.trim().toUpperCase();
        String key = null;
        if (upper.endsWith("-PJ")) {
            key = "projector";
        } else if (upper.endsWith("-AC1")) {
            key = "acLeft";
        } else if (upper.endsWith("-AC2")) {
            key = "acRight";
        } else if (upper.endsWith("-TV")) {
            key = "tv";
        }
        if (key == null) {
            return;
        }
        String sql = "UPDATE dbo.room_assets SET status = ?, note = ? WHERE room_id = ? AND asset_key = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status != null ? status : "ACTIVE");
            ps.setNString(2, note != null ? note : "");
            ps.setString(3, roomId.trim());
            ps.setString(4, key);
            ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(AssetDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * UPDATE: Chuyển dời tài sản giữa các phòng học
     */
    public boolean transferAsset(String assetCode, String newRoomId) {
        if (connection == null || assetCode == null || newRoomId == null) {
            return false;
        }
        String sql = "UPDATE dbo.assets SET room_id = ?, note = note + N' [Đã chuyển đến phòng ' + ? + N']' WHERE code = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, newRoomId.trim());
            ps.setString(2, newRoomId.trim());
            ps.setString(3, assetCode.trim());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            Logger.getLogger(AssetDAO.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    /**
     * DELETE: Xóa bản ghi tài sản (chỉ dành cho quyền Admin)
     */
    public boolean deleteAsset(String assetCode) {
        if (connection == null || assetCode == null) {
            return false;
        }
        String sql = "DELETE FROM dbo.assets WHERE code = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, assetCode.trim());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            Logger.getLogger(AssetDAO.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    /**
     * Lấy các chỉ số KPI và danh sách cho Bảng điều khiển (Dashboard)
     */
    public DashboardStats getDashboardStats(int totalRooms) {
        DashboardStats stats = new DashboardStats();
        stats.setTotalRooms(totalRooms);
        if (connection == null) {
            return stats;
        }

        // 1. Tổng số tài sản, tổng giá trị
        String sqlSummary = "SELECT COUNT(*) AS total_count, "
                          + "COALESCE(SUM(price), 0) AS total_val, "
                          + "SUM(CASE WHEN status = 'ACTIVE' THEN 1 ELSE 0 END) AS active_cnt, "
                          + "SUM(CASE WHEN status IN ('REPAIRING', 'MAINTENANCE') THEN 1 ELSE 0 END) AS repairing_cnt, "
                          + "SUM(CASE WHEN status IN ('FAULTY', 'BROKEN') THEN 1 ELSE 0 END) AS faulty_cnt "
                          + "FROM dbo.assets";
        try (PreparedStatement ps = connection.prepareStatement(sqlSummary);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                stats.setTotalAssets(rs.getInt("total_count"));
                stats.setTotalValue(rs.getDouble("total_val"));
                stats.setActiveCount(rs.getInt("active_cnt"));
                stats.setRepairingCount(rs.getInt("repairing_cnt"));
                stats.setFaultyCount(rs.getInt("faulty_cnt"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(AssetDAO.class.getName()).log(Level.SEVERE, null, ex);
        }

        // 2. Danh sách 5 tài sản mới nhất được thêm
        String sqlRecent = "SELECT TOP 5 code, name, category, serial_number, handover_date, price, room_id, status, note, created_at "
                         + "FROM dbo.assets ORDER BY created_at DESC, handover_date DESC";
        try (PreparedStatement ps = connection.prepareStatement(sqlRecent);
             ResultSet rs = ps.executeQuery()) {
            List<Asset> recent = new ArrayList<>();
            while (rs.next()) {
                recent.add(mapAsset(rs));
            }
            stats.setRecentAssets(recent);
        } catch (SQLException ex) {
            Logger.getLogger(AssetDAO.class.getName()).log(Level.SEVERE, null, ex);
        }

        // 3. Danh sách tài sản cần bảo trì khẩn cấp (Hỏng hóc FAULTY/BROKEN hoặc Đang bảo trì REPAIRING/MAINTENANCE)
        String sqlUrgent = "SELECT code, name, category, serial_number, handover_date, price, room_id, status, note, created_at "
                         + "FROM dbo.assets WHERE status IN ('FAULTY', 'BROKEN', 'REPAIRING', 'MAINTENANCE') "
                         + "ORDER BY CASE WHEN status IN ('FAULTY', 'BROKEN') THEN 1 ELSE 2 END, handover_date DESC";
        try (PreparedStatement ps = connection.prepareStatement(sqlUrgent);
             ResultSet rs = ps.executeQuery()) {
            List<Asset> urgent = new ArrayList<>();
            while (rs.next()) {
                urgent.add(mapAsset(rs));
            }
            stats.setUrgentAssets(urgent);
        } catch (SQLException ex) {
            Logger.getLogger(AssetDAO.class.getName()).log(Level.SEVERE, null, ex);
        }

        return stats;
    }

    private Asset mapAsset(ResultSet rs) throws SQLException {
        String rawStatus = rs.getString("status");
        if ("BROKEN".equalsIgnoreCase(rawStatus)) {
            rawStatus = "FAULTY";
        } else if ("MAINTENANCE".equalsIgnoreCase(rawStatus)) {
            rawStatus = "REPAIRING";
        }
        Asset a = new Asset(
            rs.getString("code"),
            rs.getNString("name"),
            rs.getNString("category"),
            rs.getString("serial_number"),
            rs.getDate("handover_date") != null ? rs.getDate("handover_date").toString() : "",
            rs.getDouble("price"),
            rs.getString("room_id"),
            rawStatus,
            rs.getNString("note")
        );
        if (rs.getTimestamp("created_at") != null) {
            a.setCreatedAt(rs.getTimestamp("created_at").toString());
        }
        return a;
    }
}
