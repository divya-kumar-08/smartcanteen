package com.smartcanteen.dto;

import com.smartcanteen.model.OrderStatus;

public class QueueStatusDto {

    private String orderNumber;
    private OrderStatus status;
    private String statusDisplayName;
    private int queuePosition;
    private int ordersAheadCount;
    private int estimatedWaitMinutes;
    private String calculationExplanation;
    private String disclaimer = "Estimated waiting time is an approximation based on active kitchen load and preparation queues. It may vary slightly during peak canteen rush.";
    private boolean active;

    public QueueStatusDto() {
    }

    public QueueStatusDto(String orderNumber, OrderStatus status, int queuePosition, int ordersAheadCount,
                          int estimatedWaitMinutes, String calculationExplanation) {
        this.orderNumber = orderNumber;
        this.status = status;
        this.statusDisplayName = (status != null) ? status.getDisplayName() : null;
        this.queuePosition = queuePosition;
        this.ordersAheadCount = ordersAheadCount;
        this.estimatedWaitMinutes = estimatedWaitMinutes;
        this.calculationExplanation = calculationExplanation;
        this.active = (status != null && status.isActive());
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
        this.statusDisplayName = (status != null) ? status.getDisplayName() : null;
        this.active = (status != null && status.isActive());
    }

    public String getStatusDisplayName() {
        return statusDisplayName;
    }

    public void setStatusDisplayName(String statusDisplayName) {
        this.statusDisplayName = statusDisplayName;
    }

    public int getQueuePosition() {
        return queuePosition;
    }

    public void setQueuePosition(int queuePosition) {
        this.queuePosition = queuePosition;
    }

    public int getOrdersAheadCount() {
        return ordersAheadCount;
    }

    public void setOrdersAheadCount(int ordersAheadCount) {
        this.ordersAheadCount = ordersAheadCount;
    }

    public int getEstimatedWaitMinutes() {
        return estimatedWaitMinutes;
    }

    public void setEstimatedWaitMinutes(int estimatedWaitMinutes) {
        this.estimatedWaitMinutes = estimatedWaitMinutes;
    }

    public String getCalculationExplanation() {
        return calculationExplanation;
    }

    public void setCalculationExplanation(String calculationExplanation) {
        this.calculationExplanation = calculationExplanation;
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
