package com.schwab.nms.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record NotificationRequest(
        String notificationId,
        @NotBlank(message = "Source system is required") String sourceSystem,
        @NotBlank(message = "Correlation identifier is required") String correlationId,
        @NotBlank(message = "Notification type is required") String notificationType,
        @NotBlank(message = "Severity is required") String severity,
        @NotBlank(message = "Priority is required") String priority,
        @NotEmpty(message = "At least one recipient is required") List<String> recipients,
        List<String> channels,
        @NotNull(message = "Creation timestamp is required") LocalDateTime createdAt,
        LocalDateTime scheduledAt,
        LocalDateTime expiresAt,
        @NotBlank(message = "Title is required") String title,
        @NotBlank(message = "Message is required") String message) {
}
