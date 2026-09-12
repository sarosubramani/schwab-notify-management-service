package com.schwab.nms.enums;

import org.apache.commons.lang3.StringUtils;

public enum NotificationSeverity {
    INFO,
    WARNING,
    ERROR,
    CRITICAL;

    public static NotificationSeverity fromValue(String rawValue) {
        if (StringUtils.isEmpty(rawValue)) {
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
