package model;

import java.io.Serializable;

/**
 * Thực thể lưu trữ Lịch sử thay đổi hệ thống & Báo cáo hỏng hóc thiết bị (ChangeHistory).
 * Chỉ dành cho Quản trị viên (ADMIN) và Cán bộ Quản lý (STAFF) theo dõi.
 */
public class ChangeHistory implements Serializable {
    private int id;
    private String actionType;
    private String targetType; // ASSET hoặc ROOM
    private String targetCode;
    private String targetName;
    private String roomId;
    private String oldValue;
    private String newValue;
    private String description;
    private String performedBy;
    private String performerName;
    private String performerRole;
    private String createdAt;

    public ChangeHistory() {
    }

    public ChangeHistory(int id, String actionType, String targetType, String targetCode,
                         String targetName, String roomId, String oldValue, String newValue,
                         String description, String performedBy, String performerName,
                         String performerRole, String createdAt) {
        this.id = id;
        this.actionType = actionType;
        this.targetType = targetType;
        this.targetCode = targetCode;
        this.targetName = targetName;
        this.roomId = roomId;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.description = description;
        this.performedBy = performedBy;
        this.performerName = performerName;
        this.performerRole = performerRole;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getTargetCode() {
        return targetCode;
    }

    public void setTargetCode(String targetCode) {
        this.targetCode = targetCode;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }

    public String getPerformerName() {
        return performerName;
    }

    public void setPerformerName(String performerName) {
        this.performerName = performerName;
    }

    public String getPerformerRole() {
        return performerRole;
    }

    public void setPerformerRole(String performerRole) {
        this.performerRole = performerRole;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getActionDisplayName() {
        if (actionType == null) {
            return "Thay đổi";
        }
        switch (actionType.toUpperCase()) {
            case "CREATE_ASSET":
                return "Thêm tài sản";
            case "UPDATE_ASSET":
                return "Cập nhật tài sản";
            case "TRANSFER_ASSET":
                return "Chuyển phòng";
            case "DELETE_ASSET":
                return "Xóa tài sản";
            case "REPORT_DAMAGE":
                return "Báo cáo đồ hỏng";
            case "CREATE_ROOM":
                return "Thêm phòng học";
            case "UPDATE_ROOM":
                return "Cập nhật phòng học";
            case "DELETE_ROOM":
                return "Xóa phòng học";
            default:
                return actionType;
        }
    }
}
