package model;

import java.io.Serializable;

/**
 * Thiết bị cốt lõi trong từng phòng học (projector, acLeft, acRight, tv).
 */
public class RoomAsset implements Serializable {
    private int assetId;
    private String roomId;
    private String assetKey;
    private String name;
    private String status; // ACTIVE, REPAIRING, FAULTY
    private String note;

    public RoomAsset() {
    }

    public RoomAsset(String roomId, String assetKey, String name, String status, String note) {
        this.roomId = roomId;
        this.assetKey = assetKey;
        this.name = name;
        this.status = status;
        this.note = note != null ? note : "";
    }

    public int getAssetId() {
        return assetId;
    }

    public void setAssetId(int assetId) {
        this.assetId = assetId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getAssetKey() {
        return assetKey;
    }

    public void setAssetKey(String assetKey) {
        this.assetKey = assetKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}

