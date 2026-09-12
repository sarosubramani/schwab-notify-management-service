package com.schwab.nms.model;

import java.time.LocalDateTime;
import java.util.List;

public record NotificationResponse(
        String id,
        String notificationId,
        String sourceSystem,
        String correlationId,
        String notificationType,
        String severity,
        String priority,
        List<String> recipients,
        List<String> channels,
        LocalDateTime createdAt,
        LocalDateTime scheduledAt,
        LocalDateTime expiresAt,
        String title,
        String message,
        String status,
        List<DeliveryAttemptResponse> deliveryAttempts) {
}
