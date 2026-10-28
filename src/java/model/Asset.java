package model;

import java.io.Serializable;
import java.text.DecimalFormat;

/**
 * Thực thể Tài sản (Asset) phục vụ Quản lý Tài sản (Asset Management - CRUD).
 * Đầy đủ thông số: Mã, Tên, Danh mục, Số serial, Ngày bàn giao, Đơn giá, Phòng bố trí, Trạng thái.
 */
public class Asset implements Serializable {
    private String code;
    private String name;
    private String category;
    private String serialNumber;
    private String handoverDate;
    private double price;
    private String roomId;
    private String status; // ACTIVE, REPAIRING, FAULTY
    private String note;
    private String createdAt;

    public Asset() {
    }

    public Asset(String code, String name, String category, String serialNumber,
                 String handoverDate, double price, String roomId, String status, String note) {
        this.code = code;
        this.name = name;
        this.category = category;
        this.serialNumber = serialNumber;
        this.handoverDate = handoverDate;
        this.price = price;
        this.roomId = roomId;
        this.status = status;
        this.note = note != null ? note : "";
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getHandoverDate() {
        return handoverDate;
    }

    public void setHandoverDate(String handoverDate) {
        this.handoverDate = handoverDate;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getFormattedPrice() {
        DecimalFormat df = new DecimalFormat("###,###,### VNĐ");
        return df.format(price);
    }

    public String getStatusDisplayName() {
        if ("FAULTY".equalsIgnoreCase(status) || "BROKEN".equalsIgnoreCase(status)) {
            return "Hỏng hóc";
        }
        if ("REPAIRING".equalsIgnoreCase(status) || "MAINTENANCE".equalsIgnoreCase(status)) {
            return "Đang bảo trì";
        }
        return "Hoạt động tốt";
    }
}
