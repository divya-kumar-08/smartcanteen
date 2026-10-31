package com.smartcanteen.model;

public enum OrderStatus {
    PLACED("Placed"),
    PREPARING("Preparing"),
    READY("Ready for Pickup"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isActive() {
        return this == PLACED || this == PREPARING;
    }
}
