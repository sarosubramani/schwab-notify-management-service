package com.schwab.nms.enums;

import org.apache.commons.lang3.StringUtils;

public enum NotificationPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL;

    public static NotificationPriority fromValue(String rawValue) {
        if (StringUtils.isBlank(rawValue)) {
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
