package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Thông tin phòng học, định mức bàn ghế, mục đích sử dụng và danh sách tài sản trang bị.
 */
public class Room implements Serializable {
    private String id;
    private String building;
    private int floor;
    private int std;
    private int cur;
    private String purpose = "Lý thuyết tiêu chuẩn";
    private Map<String, RoomAsset> assets = new LinkedHashMap<>();
    private List<Asset> assetList = new ArrayList<>();

    public Room() {
    }

    public Room(String id, String building, int floor, int std, int cur) {
        this.id = id;
        this.building = building;
        this.floor = floor;
        this.std = std;
        this.cur = cur;
    }

    public Room(String id, String building, int floor, int std, int cur, String purpose) {
        this.id = id;
        this.building = building;
        this.floor = floor;
        this.std = std;
        this.cur = cur;
        this.purpose = (purpose != null && !purpose.isBlank()) ? purpose : "Lý thuyết tiêu chuẩn";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public int getStd() {
        return std;
    }

    public void setStd(int std) {
        this.std = std;
    }

    public int getCur() {
        return cur;
    }

    public void setCur(int cur) {
        this.cur = cur;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public Map<String, RoomAsset> getAssets() {
        return assets;
    }

    public void setAssets(Map<String, RoomAsset> assets) {
        this.assets = assets;
    }

    public List<Asset> getAssetList() {
        return assetList;
    }

    public void setAssetList(List<Asset> assetList) {
        this.assetList = assetList;
    }

    public int getAssetCount() {
        if (assetList != null && !assetList.isEmpty()) {
            return assetList.size();
        }
        return assets.size();
    }

    public RoomAsset getProjector() {
        return assets.getOrDefault("projector", new RoomAsset(id, "projector", "Máy chiếu", "ACTIVE", ""));
    }

    public RoomAsset getAcLeft() {
        return assets.getOrDefault("acLeft", new RoomAsset(id, "acLeft", "Điều hòa 1 (Trái)", "ACTIVE", ""));
    }

    public RoomAsset getAcRight() {
        return assets.getOrDefault("acRight", new RoomAsset(id, "acRight", "Điều hòa 2 (Phải)", "ACTIVE", ""));
    }

    public RoomAsset getTv() {
        return assets.getOrDefault("tv", new RoomAsset(id, "tv", "Smart TV", "ACTIVE", ""));
    }

    public boolean isHasAssetIssue() {
        if (assetList != null && !assetList.isEmpty()) {
            for (Asset a : assetList) {
                if (!"ACTIVE".equalsIgnoreCase(a.getStatus())) {
                    return true;
                }
            }
        }
        for (RoomAsset a : assets.values()) {
            if (!"ACTIVE".equalsIgnoreCase(a.getStatus())) {
                return true;
            }
        }
        return false;
    }

    public boolean isHasProblem() {
        return cur != std || isHasAssetIssue();
    }

    public int getMaxChairs() {
        return Math.max(cur, std);
    }

    public int getMinChairs() {
        return Math.min(cur, std);
    }
}
