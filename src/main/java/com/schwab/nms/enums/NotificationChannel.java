package com.schwab.nms.enums;

public enum NotificationChannel {
    EMAIL,
    SMS,
    PUSH,
    IN_APP,
    SLACK,
    TEAMS;
    public static NotificationChannel fromValue(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            throw new IllegalArgumentException("Notification channel cannot be blank");
        }

        for (NotificationChannel channel : values()) {
            if (channel.name().equalsIgnoreCase(rawValue.trim())) {
                return channel;
            }
        }

        throw new IllegalArgumentException("Unsupported notification channel: " + rawValue);
    }
}
