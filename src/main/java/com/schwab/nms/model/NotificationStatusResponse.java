package com.schwab.nms.model;

import java.time.LocalDateTime;
import java.util.List;

public record NotificationStatusResponse(
        String id,
        String notificationId,
        String overallStatus,
        List<String> selectedChannels,
        List<RecipientDeliveryStatus> recipientDeliveryStatus,
        LocalDateTime createdAt,
        LocalDateTime scheduledAt,
        LocalDateTime expiresAt,
        LocalDateTime lastUpdatedAt) {

    public record RecipientDeliveryStatus(
            String recipient,
            String channel,
            String status) {
    }
}
