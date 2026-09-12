package com.schwab.nms.enums;

public enum NotificationType {
    ALERT,
    INFO,
    REMINDER,
    SYSTEM,
    SECURITY;

    public static NotificationType fromValue(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            throw new IllegalArgumentException("Notification type cannot be blank");
        }

        for (NotificationType type : values()) {
            if (type.name().equalsIgnoreCase(rawValue.trim())) {
                return type;
            }
        }

        throw new IllegalArgumentException("Unsupported notification type: " + rawValue);
    }
}
