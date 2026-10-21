package com.shopsphere.model;

/** Lifecycle of an order. */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    public static OrderStatus fromString(String value) {
        if (value == null) {
            return PENDING;
        }
        for (OrderStatus status : values()) {
            if (status.name().equalsIgnoreCase(value.trim())) {
                return status;
            }
        }
        return PENDING;
    }

    public String getLabel() {
        return switch (this) {
            case PENDING -> "Pending";
            case CONFIRMED -> "Confirmed";
            case PROCESSING -> "Processing";
            case SHIPPED -> "Shipped";
            case DELIVERED -> "Delivered";
            case CANCELLED -> "Cancelled";
        };
    }

    public String getBadgeClass() {
        return switch (this) {
            case PENDING -> "badge-pending";
            case CONFIRMED -> "badge-confirmed";
            case PROCESSING -> "badge-processing";
            case SHIPPED -> "badge-shipped";
            case DELIVERED -> "badge-delivered";
            case CANCELLED -> "badge-cancelled";
        };
    }

    public boolean isCancellable() {
        return this == PENDING || this == CONFIRMED || this == PROCESSING;
    }
}
