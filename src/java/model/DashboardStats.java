package model;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO chứa các chỉ số KPI thống kê cho Bảng điều khiển (Dashboard).
 */
public class DashboardStats implements Serializable {
    private int totalAssets;
    private int totalRooms;
    private double totalValue;
    private int activeCount;
    private int repairingCount;
    private int faultyCount;
    private List<Asset> recentAssets = new ArrayList<>();
    private List<Asset> urgentAssets = new ArrayList<>();

    public DashboardStats() {
    }

    public int getTotalAssets() {
        return totalAssets;
    }

    public void setTotalAssets(int totalAssets) {
        this.totalAssets = totalAssets;
    }

    public int getTotalRooms() {
        return totalRooms;
    }

    public void setTotalRooms(int totalRooms) {
        this.totalRooms = totalRooms;
    }

    public double getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(double totalValue) {
        this.totalValue = totalValue;
    }

    public int getActiveCount() {
        return activeCount;
    }

    public void setActiveCount(int activeCount) {
        this.activeCount = activeCount;
    }

    public int getRepairingCount() {
        return repairingCount;
    }

    public void setRepairingCount(int repairingCount) {
        this.repairingCount = repairingCount;
    }

    public int getFaultyCount() {
        return faultyCount;
    }

    public void setFaultyCount(int faultyCount) {
        this.faultyCount = faultyCount;
    }

    public List<Asset> getRecentAssets() {
        return recentAssets;
    }

    public void setRecentAssets(List<Asset> recentAssets) {
        this.recentAssets = recentAssets;
    }

    public List<Asset> getUrgentAssets() {
        return urgentAssets;
    }

    public void setUrgentAssets(List<Asset> urgentAssets) {
        this.urgentAssets = urgentAssets;
    }

    public String getFormattedTotalValue() {
        DecimalFormat df = new DecimalFormat("###,###,### VNĐ");
        return df.format(totalValue);
    }

    public double getActiveRate() {
        if (totalAssets == 0) return 0;
        return Math.round((activeCount * 100.0 / totalAssets) * 10.0) / 10.0;
    }

    public double getRepairingRate() {
        if (totalAssets == 0) return 0;
        return Math.round((repairingCount * 100.0 / totalAssets) * 10.0) / 10.0;
    }

    public double getFaultyRate() {
        if (totalAssets == 0) return 0;
        return Math.round((faultyCount * 100.0 / totalAssets) * 10.0) / 10.0;
    }
}

