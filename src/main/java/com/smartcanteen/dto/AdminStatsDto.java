package com.smartcanteen.dto;

import java.math.BigDecimal;

public class AdminStatsDto {

    private BigDecimal totalGrossRevenue;
    private long totalOrdersCount;
    private long activeOrdersCount;
    private long preparingOrdersCount;
    private long readyOrdersCount;
    private long completedOrdersCount;
    private long totalMenuItemsCount;

    public AdminStatsDto() {
    }

    public BigDecimal getTotalGrossRevenue() {
        return totalGrossRevenue;
    }

    public void setTotalGrossRevenue(BigDecimal totalGrossRevenue) {
        this.totalGrossRevenue = totalGrossRevenue;
    }

    public long getTotalOrdersCount() {
        return totalOrdersCount;
    }

    public void setTotalOrdersCount(long totalOrdersCount) {
        this.totalOrdersCount = totalOrdersCount;
    }

    public long getActiveOrdersCount() {
        return activeOrdersCount;
    }

    public void setActiveOrdersCount(long activeOrdersCount) {
        this.activeOrdersCount = activeOrdersCount;
    }

    public long getPreparingOrdersCount() {
        return preparingOrdersCount;
    }

    public void setPreparingOrdersCount(long preparingOrdersCount) {
        this.preparingOrdersCount = preparingOrdersCount;
    }

    public long getReadyOrdersCount() {
        return readyOrdersCount;
    }

    public void setReadyOrdersCount(long readyOrdersCount) {
        this.readyOrdersCount = readyOrdersCount;
    }

    public long getCompletedOrdersCount() {
        return completedOrdersCount;
    }

    public void setCompletedOrdersCount(long completedOrdersCount) {
        this.completedOrdersCount = completedOrdersCount;
    }

    public long getTotalMenuItemsCount() {
        return totalMenuItemsCount;
    }

    public void setTotalMenuItemsCount(long totalMenuItemsCount) {
        this.totalMenuItemsCount = totalMenuItemsCount;
    }
}
