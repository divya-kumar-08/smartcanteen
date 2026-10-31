package com.smartcanteen.model;

public enum PaymentMethod {
    CASH("Cash at Counter"),
    DEMO_ONLINE("Simulated Demo Payment (UPI/Card)");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
