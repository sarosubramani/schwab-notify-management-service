package com.schwab.nms;

import com.schwab.nms.constants.NmsConstants;
import com.schwab.nms.enums.NotificationChannel;
import com.schwab.nms.enums.NotificationPriority;
import com.schwab.nms.enums.NotificationSeverity;
import com.schwab.nms.enums.NotificationType;
import com.schwab.nms.exception.GlobalExceptionHandler;
import com.schwab.nms.exception.ResourceNotFoundException;
import com.schwab.nms.handler.NotificationHandler;
import com.schwab.nms.model.DeliveryAttemptResponse;
import com.schwab.nms.model.NotificationRequest;
import com.schwab.nms.model.NotificationResponse;
import com.schwab.nms.model.NotificationStatusResponse;
import com.schwab.nms.model.StoredNotification;
import com.schwab.nms.service.NotificationRoutingPolicy;
import com.schwab.nms.service.NotificationService;
import com.schwab.nms.util.NmsUtils;
import com.schwab.nms.util.NotificationServiceUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class CoverageBoostTest {

    @Test
    void notificationHandlerDelegatesAllOperations() {
        NotificationService service = Mockito.mock(NotificationService.class);
        NotificationHandler handler = new NotificationHandler(service);

        NotificationRequest request = validRequest();
        NotificationResponse response = new NotificationResponse(
                "n-1",
                "n-1",
                "billing-system",
                "corr-1",
                "ALERT",
                "ERROR",
                "HIGH",
                List.of("user@example.com"),
                List.of("EMAIL"),
                LocalDateTime.now(),
                null,
                null,
                "Title",
                "Message",
                "QUEUED",
                List.of()
        );
        NotificationStatusResponse statusResponse = new NotificationStatusResponse(
                "n-1",
                "n-1",
                "QUEUED",
                List.of("EMAIL"),
                List.of(new NotificationStatusResponse.RecipientDeliveryStatus("user@example.com", "EMAIL", "QUEUED")),
                LocalDateTime.now(),
                null,
                null,
                LocalDateTime.now()
        );

        when(service.createNotification(request)).thenReturn(response);
        when(service.getNotificationById("n-1")).thenReturn(response);
        when(service.getNotificationStatus("n-1")).thenReturn(statusResponse);
        when(service.getNotifications()).thenReturn(List.of(response));

        assertSame(response, handler.createNotification(request));
        assertSame(response, handler.getNotificationById("n-1"));
        assertSame(statusResponse, handler.getNotificationStatus("n-1"));
        assertEquals(1, handler.getNotifications().size());
    }

    @Test
    void notificationServiceUtilsTransformsAndValidatesState() {
        Map<String, StoredNotification> notifications = new ConcurrentHashMap<>();
        StoredNotification notification = new StoredNotification(
                "n-2",
                "billing-system",
                "corr-2",
                "ALERT",
                "ERROR",
                "HIGH",
                List.of("user@example.com"),
                List.of("EMAIL", "SMS"),
                LocalDateTime.now(),
                null,
                null,
                "Title",
                "Message",
                "QUEUED",
                List.of(),
                LocalDateTime.now()
        );
        notifications.put("n-2", notification);

        NotificationServiceUtils.updateStatus(notifications, "n-2", "PROCESSING");
        NotificationServiceUtils.addAttempt(notifications, "n-2", new DeliveryAttemptResponse("EMAIL", 1, "SENT", NmsConstants.Providers.SMTP, "ok"));

        assertEquals("PROCESSING", notifications.get("n-2").status());
        assertEquals(1, notifications.get("n-2").deliveryAttempts().size());
        assertEquals(NmsConstants.Providers.SMTP, NotificationServiceUtils.resolveProvider("EMAIL"));
        assertEquals(NmsConstants.Providers.TWILIO, NotificationServiceUtils.resolveProvider("SMS"));
        assertEquals(NmsConstants.Providers.NMS_INBOX, NotificationServiceUtils.resolveProvider("IN_APP"));
        assertEquals(NmsConstants.Providers.TEAMS_WEBHOOK, NotificationServiceUtils.resolveProvider("TEAMS"));

        NotificationRequest duplicateRequest = validRequest();
        NotificationRoutingPolicy routingPolicy = new NotificationRoutingPolicy();
        NotificationServiceUtils.ensureNotDuplicate(duplicateRequest, notifications, routingPolicy);

        notifications.put("duplicate", new StoredNotification(
                "dup",
                duplicateRequest.sourceSystem(),
                duplicateRequest.correlationId(),
                duplicateRequest.notificationType(),
                duplicateRequest.severity(),
                duplicateRequest.priority(),
                duplicateRequest.recipients(),
                routingPolicy.resolveChannels(duplicateRequest.channels(), duplicateRequest.priority()),
                duplicateRequest.createdAt(),
                duplicateRequest.scheduledAt(),
                duplicateRequest.expiresAt(),
                duplicateRequest.title(),
                duplicateRequest.message(),
                "QUEUED",
                List.of(),
                LocalDateTime.now()
        ));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> NotificationServiceUtils.ensureNotDuplicate(duplicateRequest, notifications, routingPolicy));
        assertEquals(NmsConstants.Messages.ERR_DUPLICATE, ex.getMessage());

        NotificationResponse response = NotificationServiceUtils.toResponse(notifications.get("n-2"));
        NotificationStatusResponse status = NotificationServiceUtils.toStatusResponse(notifications.get("n-2"));

        assertEquals("n-2", response.id());
        assertEquals("PROCESSING", status.overallStatus());
    }

    @Test
    void nmsUtilsNormalizesAndBuildsFingerprints() {
        NotificationRoutingPolicy route = new NotificationRoutingPolicy();
        NotificationRequest request = validRequest();

        assertEquals(List.of("email", "sms"), NmsUtils.normalizeList(List.of(" email ", "email", "", "sms", "sms")));
        assertNotNull(NmsUtils.buildFingerprint(request, route));

        StoredNotification stored = new StoredNotification(
                "n-3",
                "billing-system",
                "corr-3",
                "INFO",
                "WARNING",
                "MEDIUM",
                List.of("a@example.com", "b@example.com"),
                List.of("EMAIL"),
                LocalDateTime.now(),
                null,
                null,
                "Hello",
                "World",
                "QUEUED",
                List.of(),
                LocalDateTime.now()
        );

        assertEquals(NmsUtils.buildFingerprint(stored), NmsUtils.buildFingerprint(stored));
    }

    @Test
    void notificationHandlerReThrowsServiceExceptions() {
        NotificationService service = Mockito.mock(NotificationService.class);
        NotificationHandler handler = new NotificationHandler(service);
        NotificationRequest request = validRequest();

        when(service.createNotification(request)).thenThrow(new RuntimeException("boom"));
        when(service.getNotificationById("n-1")).thenThrow(new RuntimeException("boom"));
        when(service.getNotificationStatus("n-1")).thenThrow(new RuntimeException("boom"));
        when(service.getNotifications()).thenThrow(new RuntimeException("boom"));

        assertThrows(RuntimeException.class, () -> handler.createNotification(request));
        assertThrows(RuntimeException.class, () -> handler.getNotificationById("n-1"));
        assertThrows(RuntimeException.class, () -> handler.getNotificationStatus("n-1"));
        assertThrows(RuntimeException.class, () -> handler.getNotifications());
    }

    @Test
    void globalExceptionHandlerReturnsExpectedResponses() throws Exception {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/notifications/abc");

        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new TestValidationBean(), "target");
        bindingResult.rejectValue("sourceSystem", "required", "Source system is required");
        Method method = CoverageBoostTest.class.getDeclaredMethod("dummyMethod");
        MethodArgumentNotValidException validationException = new MethodArgumentNotValidException(new MethodParameter(method, -1), bindingResult);

        var notFound = handler.handleResourceNotFound(new ResourceNotFoundException("Notification", "id", "abc"), request);
        var validation = handler.handleValidationException(validationException, request);
        var badRequest = handler.handleIllegalArgumentException(new IllegalArgumentException("bad input"), request);
        var generic = handler.handleGeneralException(new RuntimeException("oops"), request);

        assertEquals(404, notFound.getBody().status());
        assertEquals(400, validation.getBody().status());
        assertEquals(400, badRequest.getBody().status());
        assertEquals(500, generic.getBody().status());
    }

    @Test
    void enumConvertersSupportValidAndInvalidValues() {
        assertEquals(NotificationChannel.EMAIL, NotificationChannel.fromValue("EMAIL"));
        assertEquals(NotificationPriority.HIGH, NotificationPriority.fromValue("HIGH"));
        assertEquals(NotificationSeverity.CRITICAL, NotificationSeverity.fromValue("CRITICAL"));
        assertEquals(NotificationType.SECURITY, NotificationType.fromValue("SECURITY"));

        assertThrows(IllegalArgumentException.class, () -> NotificationChannel.fromValue("WHATSAPP"));
        assertThrows(IllegalArgumentException.class, () -> NotificationPriority.fromValue("URGENT"));
        assertThrows(IllegalArgumentException.class, () -> NotificationSeverity.fromValue("SEVERE"));
        assertThrows(IllegalArgumentException.class, () -> NotificationType.fromValue("UNKNOWN"));
    }

    private static class TestValidationBean {
        private String sourceSystem;

        public String getSourceSystem() {
            return sourceSystem;
        }

        public void setSourceSystem(String sourceSystem) {
            this.sourceSystem = sourceSystem;
        }
    }

    private void dummyMethod() {
    }

    private NotificationRequest validRequest() {
        return new NotificationRequest(
                "n-1",
                "billing-system",
                "corr-1",
                "ALERT",
                "ERROR",
                "HIGH",
                List.of("user@example.com"),
                List.of("EMAIL", "SMS"),
                LocalDateTime.now(),
                null,
                null,
                "Title",
                "Message"
        );
    }
}
