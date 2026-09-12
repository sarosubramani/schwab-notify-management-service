package com.schwab.nms.enums;

import org.apache.commons.lang3.StringUtils;

public enum NotificationType {
    ALERT,
    INFO,
    REMINDER,
    SYSTEM,
    SECURITY;

    public static NotificationType fromValue(String rawValue) {
        if (StringUtils.isEmpty(rawValue)) {
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
