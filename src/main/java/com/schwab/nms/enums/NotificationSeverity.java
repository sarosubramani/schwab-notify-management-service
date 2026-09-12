package com.schwab.nms.enums;

public enum NotificationSeverity {
    INFO,
    WARNING,
    ERROR,
    CRITICAL;

    public static NotificationSeverity fromValue(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            throw new IllegalArgumentException("Notification severity cannot be blank");
        }

        for (NotificationSeverity severity : values()) {
            if (severity.name().equalsIgnoreCase(rawValue.trim())) {
                return severity;
            }
        }

        throw new IllegalArgumentException("Unsupported notification severity: " + rawValue);
    }
}
