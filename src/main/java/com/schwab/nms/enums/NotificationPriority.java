package com.schwab.nms.enums;

public enum NotificationPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL;

    public static NotificationPriority fromValue(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            throw new IllegalArgumentException("Notification priority cannot be blank");
        }

        for (NotificationPriority priority : values()) {
            if (priority.name().equalsIgnoreCase(rawValue.trim())) {
                return priority;
            }
        }

        throw new IllegalArgumentException("Unsupported notification priority: " + rawValue);
    }
}
