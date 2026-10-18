package dal;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Asset;
import model.Room;
import model.RoomAsset;

/**
 * DAO quản lý nghiệp vụ CRUD Phòng học (Room Management - CRUD):
 * Danh mục phòng học, Thêm / Sửa / Xóa phòng, mục đích sử dụng, số lượng chỗ ngồi,
 * và nạp toàn bộ danh sách tài sản trang bị bên trong phòng (Room Detail).
 */
public class RoomDAO extends DBContext {

    public List<Room> getAllRooms() {
        Map<String, Room> roomMap = new LinkedHashMap<>();
        if (connection == null) {
            return new ArrayList<>();
        }
        String sqlRooms = "SELECT id, building, floor, std_chairs, cur_chairs, purpose FROM dbo.rooms ORDER BY building, floor, id";
        String sqlAssets = "SELECT asset_id, room_id, asset_key, asset_name, status, note FROM dbo.room_assets ORDER BY asset_id";
        try (PreparedStatement psR = connection.prepareStatement(sqlRooms);
             ResultSet rsR = psR.executeQuery()) {
            while (rsR.next()) {
                Room r = new Room(
                        rsR.getString("id"),
                        rsR.getNString("building"),
                        rsR.getInt("floor"),
                        rsR.getInt("std_chairs"),
                        rsR.getInt("cur_chairs"),
                        rsR.getNString("purpose")
                );
                roomMap.put(r.getId(), r);
            }
        } catch (SQLException ex) {
            Logger.getLogger(RoomDAO.class.getName()).log(Level.SEVERE, null, ex);
        }

        try (PreparedStatement psA = connection.prepareStatement(sqlAssets);
             ResultSet rsA = psA.executeQuery()) {
            while (rsA.next()) {
                String roomId = rsA.getString("room_id");
                Room r = roomMap.get(roomId);
                if (r != null) {
                    RoomAsset asset = new RoomAsset(
                            roomId,
                            rsA.getString("asset_key"),
                            rsA.getNString("asset_name"),
                            rsA.getString("status"),
                            rsA.getNString("note")
                    );
                    asset.setAssetId(rsA.getInt("asset_id"));
                    r.getAssets().put(asset.getAssetKey(), asset);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(RoomDAO.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Tải danh sách đầy đủ tài sản từ bảng assets cho từng phòng (Tiêu chí 4: Room Detail)
        AssetDAO assetDAO = new AssetDAO();
        for (Room r : roomMap.values()) {
            List<Asset> aList = assetDAO.getAssetsByRoom(r.getId());
            r.setAssetList(aList);
        }

        return new ArrayList<>(roomMap.values());
    }

    public List<Room> filterRooms(String building, String statusFilter, String search) {
        List<Room> all = getAllRooms();
        List<Room> filtered = new ArrayList<>();
        String bVal = (building == null || building.isEmpty()) ? "ALL" : building.trim();
        String sVal = (statusFilter == null || statusFilter.isEmpty()) ? "ALL" : statusFilter.trim();
        String qVal = (search == null) ? "" : search.trim().toUpperCase();

        for (Room r : all) {
            if (!"ALL".equalsIgnoreCase(bVal) && !r.getBuilding().equalsIgnoreCase(bVal)) {
                continue;
            }
            if (!qVal.isEmpty() && !r.getId().toUpperCase().contains(qVal) && !r.getPurpose().toUpperCase().contains(qVal)) {
                continue;
            }
            boolean isProblem = r.isHasProblem();
            if ("ISSUES".equalsIgnoreCase(sVal) && !isProblem) {
                continue;
            }
            if ("OK".equalsIgnoreCase(sVal) && isProblem) {
                continue;
            }
            filtered.add(r);
        }
        return filtered;
    }

    public Room getRoomById(String roomId) {
        if (roomId == null) {
            return null;
        }
        for (Room r : getAllRooms()) {
            if (r.getId().equalsIgnoreCase(roomId.trim())) {
                return r;
            }
        }
        return null;
    }

    /**
     * CREATE: Thêm phòng học mới (Tiêu chí 4: Quản lý Phòng học - CRUD)
     */
    public boolean createRoom(String id, String building, int floor, int stdChairs, int curChairs,
                              String purpose, String projName, String acLeftName, String acRightName, String tvName) {
        if (connection == null) {
            return false;
        }
        String insertRoom = "INSERT INTO dbo.rooms (id, building, floor, std_chairs, cur_chairs, purpose) VALUES (?, ?, ?, ?, ?, ?)";
        String insertAsset = "INSERT INTO dbo.room_assets (room_id, asset_key, asset_name, status, note) VALUES (?, ?, ?, 'ACTIVE', N'')";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(insertRoom)) {
                ps.setString(1, id.trim().toUpperCase());
                ps.setNString(2, building);
                ps.setInt(3, floor);
                ps.setInt(4, stdChairs);
                ps.setInt(5, curChairs);
                ps.setNString(6, (purpose != null && !purpose.isBlank()) ? purpose.trim() : "Lý thuyết tiêu chuẩn");
                ps.executeUpdate();
            }
            String[][] defaultAssets = {
                {"projector", (projName == null || projName.isBlank()) ? "Máy chiếu Sony" : projName.trim()},
                {"acLeft", (acLeftName == null || acLeftName.isBlank()) ? "Điều hòa Daikin (Trái)" : acLeftName.trim()},
                {"acRight", (acRightName == null || acRightName.isBlank()) ? "Điều hòa Daikin (Phải)" : acRightName.trim()},
                {"tv", (tvName == null || tvName.isBlank()) ? "Smart TV Samsung 65 inch" : tvName.trim()}
            };
            try (PreparedStatement psA = connection.prepareStatement(insertAsset)) {
                for (String[] a : defaultAssets) {
                    psA.setString(1, id.trim().toUpperCase());
                    psA.setString(2, a[0]);
                    psA.setNString(3, a[1]);
                    psA.addBatch();
                }
                psA.executeBatch();
            }

            // Đồng thời tạo các bản ghi tương ứng vào bảng assets
            String insertFullAsset = "INSERT INTO dbo.assets (code, name, category, serial_number, handover_date, price, room_id, status, note) "
                                   + "VALUES (?, ?, ?, ?, CAST(GETDATE() AS DATE), ?, ?, 'ACTIVE', N'Khởi tạo cùng phòng mới')";
            try (PreparedStatement psFA = connection.prepareStatement(insertFullAsset)) {
                String cleanId = id.trim().toUpperCase().replace("-", "");
                psFA.setString(1, "AST-" + cleanId + "-PJ");
                psFA.setNString(2, defaultAssets[0][1]);
                psFA.setNString(3, "Máy chiếu");
                psFA.setString(4, "SN-" + cleanId + "-01");
                psFA.setDouble(5, 20000000);
                psFA.setString(6, id.trim().toUpperCase());
                psFA.addBatch();

                psFA.setString(1, "AST-" + cleanId + "-AC1");
                psFA.setNString(2, defaultAssets[1][1]);
                psFA.setNString(3, "Điều hòa");
                psFA.setString(4, "SN-" + cleanId + "-02");
                psFA.setDouble(5, 14000000);
                psFA.setString(6, id.trim().toUpperCase());
                psFA.addBatch();

                psFA.setString(1, "AST-" + cleanId + "-AC2");
                psFA.setNString(2, defaultAssets[2][1]);
                psFA.setNString(3, "Điều hòa");
                psFA.setString(4, "SN-" + cleanId + "-03");
                psFA.setDouble(5, 14000000);
                psFA.setString(6, id.trim().toUpperCase());
                psFA.addBatch();

                psFA.setString(1, "AST-" + cleanId + "-TV");
                psFA.setNString(2, defaultAssets[3][1]);
                psFA.setNString(3, "Smart TV");
                psFA.setString(4, "SN-" + cleanId + "-04");
                psFA.setDouble(5, 16000000);
                psFA.setString(6, id.trim().toUpperCase());
                psFA.addBatch();

                psFA.executeBatch();
            }

            connection.commit();
            connection.setAutoCommit(true);
            return true;
        } catch (SQLException ex) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException ignore) {
            }
            Logger.getLogger(RoomDAO.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    /**
     * UPDATE: Cập nhật thông tin phòng học, số lượng chỗ ngồi, mục đích sử dụng (Room Management - CRUD)
     */
    public boolean updateRoom(String id, String building, int floor, int stdChairs, int curChairs, String purpose) {
        if (connection == null) {
            return false;
        }
        String sql = "UPDATE dbo.rooms SET building = ?, floor = ?, std_chairs = ?, cur_chairs = ?, purpose = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setNString(1, building);
            ps.setInt(2, floor);
            ps.setInt(3, stdChairs);
            ps.setInt(4, curChairs);
            ps.setNString(5, (purpose != null && !purpose.isBlank()) ? purpose.trim() : "Lý thuyết tiêu chuẩn");
            ps.setString(6, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            Logger.getLogger(RoomDAO.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    /**
     * DELETE: Xóa phòng học (chỉ dành cho quyền Admin)
     */
    public boolean deleteRoom(String id) {
        if (connection == null) {
            return false;
        }
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement psAs = connection.prepareStatement("DELETE FROM dbo.assets WHERE room_id = ?")) {
                psAs.setString(1, id);
                psAs.executeUpdate();
            }
            try (PreparedStatement psRA = connection.prepareStatement("DELETE FROM dbo.room_assets WHERE room_id = ?")) {
                psRA.setString(1, id);
                psRA.executeUpdate();
            }
            try (PreparedStatement psR = connection.prepareStatement("DELETE FROM dbo.rooms WHERE id = ?")) {
                psR.setString(1, id);
                int affected = psR.executeUpdate();
                connection.commit();
                connection.setAutoCommit(true);
                return affected > 0;
            }
        } catch (SQLException ex) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException ignore) {
            }
            Logger.getLogger(RoomDAO.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    /**
     * Cập nhật trạng thái và ghi chú thiết bị trong phòng học.
     */
    public boolean updateAssetStatus(String roomId, String assetKey, String newStatus, String note) {
        if (connection == null) {
            return false;
        }
        String finalNote = "ACTIVE".equalsIgnoreCase(newStatus) ? "" : (note != null ? note : "");
        String sql = "UPDATE dbo.room_assets SET status = ?, note = CASE WHEN ? = 'ACTIVE' THEN N'' WHEN ? <> N'' THEN ? ELSE note END WHERE room_id = ? AND asset_key = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setString(2, newStatus);
            ps.setNString(3, finalNote);
            ps.setNString(4, finalNote);
            ps.setString(5, roomId);
            ps.setString(6, assetKey);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            Logger.getLogger(RoomDAO.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public boolean resetSampleData() {
        if (connection == null) {
            return false;
        }
        try (CallableStatement cs = connection.prepareCall("{call dbo.sp_ResetFcamsData}")) {
            cs.execute();
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(RoomDAO.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
}
