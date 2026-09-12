package com.schwab.nms.validator;

import com.schwab.nms.constants.NmsConstants;
import com.schwab.nms.model.NotificationRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RequestValidatorTest {

    @Test
    void validRequestDoesNotThrow() {
        RequestValidator validator = new RequestValidator();

        assertDoesNotThrow(() -> validator.createNotifyValidateRequest(validRequest()));
    }

    @Test
    void missingSourceSystemIsRejected() {
        NotificationRequest request = requestWithSourceSystem(" ");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new RequestValidator().createNotifyValidateRequest(request));

        assertEquals(NmsConstants.Messages.ERR_SOURCE_SYSTEM, ex.getMessage());
    }

    @Test
    void unsupportedChannelIsRejected() {
        NotificationRequest request = requestWithChannels(List.of("WHATSAPP"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new RequestValidator().createNotifyValidateRequest(request));

        assertEquals("Unsupported notification channel: WHATSAPP", ex.getMessage());
    }

    @Test
    void invalidPriorityIsRejected() {
        NotificationRequest request = requestWithPriority("URGENT");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new RequestValidator().createNotifyValidateRequest(request));

        assertEquals("Unsupported notification priority: URGENT", ex.getMessage());
    }

    @Test
    void blankRequiredFieldsAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new RequestValidator().createNotifyValidateRequest(requestWithSourceSystem(" ")));
        assertThrows(IllegalArgumentException.class,
                () -> new RequestValidator().createNotifyValidateRequest(requestWithNotificationType(" ")));
        assertThrows(IllegalArgumentException.class,
                () -> new RequestValidator().createNotifyValidateRequest(requestWithSeverity(" ")));
        assertThrows(IllegalArgumentException.class,
                () -> new RequestValidator().createNotifyValidateRequest(requestWithPriority(" ")));
        assertThrows(IllegalArgumentException.class,
                () -> new RequestValidator().createNotifyValidateRequest(requestWithRecipients(List.of())));
        assertThrows(IllegalArgumentException.class,
                () -> new RequestValidator().createNotifyValidateRequest(requestWithCreatedAt(null)));
        assertThrows(IllegalArgumentException.class,
                () -> new RequestValidator().createNotifyValidateRequest(requestWithTitle(" ")));
        assertThrows(IllegalArgumentException.class,
                () -> new RequestValidator().createNotifyValidateRequest(requestWithMessage(" ")));
    }

    private NotificationRequest validRequest() {
        return new NotificationRequest(
                "notif-1001",
                "billing-system",
                "corr-1001",
                "ALERT",
                "ERROR",
                "HIGH",
                List.of("ops@example.com"),
                List.of("EMAIL", "SMS"),
                LocalDateTime.now(),
                null,
                null,
                "Payment due",
                "Invoice 1024 is due today"
        );
    }

    private NotificationRequest requestWithSourceSystem(String sourceSystem) {
        return new NotificationRequest(
                validRequest().notificationId(),
                sourceSystem,
                validRequest().correlationId(),
                validRequest().notificationType(),
                validRequest().severity(),
                validRequest().priority(),
                validRequest().recipients(),
                validRequest().channels(),
                validRequest().createdAt(),
                validRequest().scheduledAt(),
                validRequest().expiresAt(),
                validRequest().title(),
                validRequest().message()
        );
    }

    private NotificationRequest requestWithChannels(List<String> channels) {
        return new NotificationRequest(
                validRequest().notificationId(),
                validRequest().sourceSystem(),
                validRequest().correlationId(),
                validRequest().notificationType(),
                validRequest().severity(),
                validRequest().priority(),
                validRequest().recipients(),
                channels,
                validRequest().createdAt(),
                validRequest().scheduledAt(),
                validRequest().expiresAt(),
                validRequest().title(),
                validRequest().message()
        );
    }

    private NotificationRequest requestWithPriority(String priority) {
        return new NotificationRequest(
                validRequest().notificationId(),
                validRequest().sourceSystem(),
                validRequest().correlationId(),
                validRequest().notificationType(),
                validRequest().severity(),
                priority,
                validRequest().recipients(),
                validRequest().channels(),
                validRequest().createdAt(),
                validRequest().scheduledAt(),
                validRequest().expiresAt(),
                validRequest().title(),
                validRequest().message()
        );
    }

    private NotificationRequest requestWithCorrelationId(String correlationId) {
        return new NotificationRequest(
                validRequest().notificationId(),
                validRequest().sourceSystem(),
                correlationId,
                validRequest().notificationType(),
                validRequest().severity(),
                validRequest().priority(),
                validRequest().recipients(),
                validRequest().channels(),
                validRequest().createdAt(),
                validRequest().scheduledAt(),
                validRequest().expiresAt(),
                validRequest().title(),
                validRequest().message()
        );
    }

    private NotificationRequest requestWithNotificationType(String notificationType) {
        return new NotificationRequest(
                validRequest().notificationId(),
                validRequest().sourceSystem(),
                validRequest().correlationId(),
                notificationType,
                validRequest().severity(),
                validRequest().priority(),
                validRequest().recipients(),
                validRequest().channels(),
                validRequest().createdAt(),
                validRequest().scheduledAt(),
                validRequest().expiresAt(),
                validRequest().title(),
                validRequest().message()
        );
    }

    private NotificationRequest requestWithSeverity(String severity) {
        return new NotificationRequest(
                validRequest().notificationId(),
                validRequest().sourceSystem(),
                validRequest().correlationId(),
                validRequest().notificationType(),
                severity,
                validRequest().priority(),
                validRequest().recipients(),
                validRequest().channels(),
                validRequest().createdAt(),
                validRequest().scheduledAt(),
                validRequest().expiresAt(),
                validRequest().title(),
                validRequest().message()
        );
    }

    private NotificationRequest requestWithRecipients(List<String> recipients) {
        return new NotificationRequest(
                validRequest().notificationId(),
                validRequest().sourceSystem(),
                validRequest().correlationId(),
                validRequest().notificationType(),
                validRequest().severity(),
                validRequest().priority(),
                recipients,
                validRequest().channels(),
                validRequest().createdAt(),
                validRequest().scheduledAt(),
                validRequest().expiresAt(),
                validRequest().title(),
                validRequest().message()
        );
    }

    private NotificationRequest requestWithCreatedAt(LocalDateTime createdAt) {
        return new NotificationRequest(
                validRequest().notificationId(),
                validRequest().sourceSystem(),
                validRequest().correlationId(),
                validRequest().notificationType(),
                validRequest().severity(),
                validRequest().priority(),
                validRequest().recipients(),
                validRequest().channels(),
                createdAt,
                validRequest().scheduledAt(),
                validRequest().expiresAt(),
                validRequest().title(),
                validRequest().message()
        );
    }

    private NotificationRequest requestWithTitle(String title) {
        return new NotificationRequest(
                validRequest().notificationId(),
                validRequest().sourceSystem(),
                validRequest().correlationId(),
                validRequest().notificationType(),
                validRequest().severity(),
                validRequest().priority(),
                validRequest().recipients(),
                validRequest().channels(),
                validRequest().createdAt(),
                validRequest().scheduledAt(),
                validRequest().expiresAt(),
                title,
                validRequest().message()
        );
    }

    private NotificationRequest requestWithMessage(String message) {
        return new NotificationRequest(
                validRequest().notificationId(),
                validRequest().sourceSystem(),
                validRequest().correlationId(),
                validRequest().notificationType(),
                validRequest().severity(),
                validRequest().priority(),
                validRequest().recipients(),
                validRequest().channels(),
                validRequest().createdAt(),
                validRequest().scheduledAt(),
                validRequest().expiresAt(),
                validRequest().title(),
                message
        );
    }
}
