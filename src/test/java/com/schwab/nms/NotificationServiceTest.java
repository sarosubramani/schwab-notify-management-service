package com.schwab.nms;

import com.schwab.nms.exception.ResourceNotFoundException;
import com.schwab.nms.model.NotificationRequest;
import com.schwab.nms.model.NotificationResponse;
import com.schwab.nms.model.NotificationStatusResponse;
import com.schwab.nms.service.NotificationRoutingPolicy;
import com.schwab.nms.service.NotificationService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NotificationServiceTest {

    private final NotificationService notificationService = new NotificationService(new NotificationRoutingPolicy());

    @Test
    void createNotificationReturnsQueuedResponse() {
        NotificationRequest request = validRequest("notif-1001", "billing-system", "corr-1001");

        NotificationResponse response = notificationService.createNotification(request);

        assertNotNull(response.id());
        assertEquals("billing-system", response.sourceSystem());
        assertEquals("QUEUED", response.status());
        assertEquals(List.of("EMAIL", "SMS"), response.channels());
    }

    @Test
    void defaultChannelsAreAppliedWhenChannelsAreBlank() {
        NotificationRequest request = new NotificationRequest(
                "notif-1002",
                "risk-system",
                "corr-1002",
                "SECURITY",
                "CRITICAL",
                "HIGH",
                List.of("risk@example.com"),
                List.of(),
                LocalDateTime.now(),
                null,
                null,
                "Risk alert",
                "Threshold breached"
        );

        NotificationResponse response = notificationService.createNotification(request);

        assertEquals(List.of("EMAIL", "SMS"), response.channels());
    }

    @Test
    void duplicateRequestThrowsIllegalArgumentException() {
        NotificationRequest request = validRequest("notif-1003", "fraud-monitor", "corr-1003");

        notificationService.createNotification(request);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> notificationService.createNotification(request));

        assertEquals("Duplicate notification request detected", exception.getMessage());
    }

    @Test
    void getNotificationByIdReturnsStoredRecord() {
        NotificationRequest request = validRequest("notif-1004", "ops-system", "corr-1004");

        NotificationResponse created = notificationService.createNotification(request);
        NotificationResponse found = notificationService.getNotificationById(created.id());

        assertEquals(created.id(), found.id());
        assertEquals("ops-system", found.sourceSystem());
    }

    @Test
    void getNotificationStatusReturnsOverallStatus() {
        NotificationRequest request = validRequest("notif-1005", "support-system", "corr-1005");

        NotificationResponse created = notificationService.createNotification(request);
        NotificationStatusResponse status = notificationService.getNotificationStatus(created.id());

        assertEquals(created.id(), status.id());
        assertNotNull(status.overallStatus());
    }

    @Test
    void getNotificationsReturnsAllSortedNewestFirst() {
        NotificationRequest older = validRequestAt("notif-1006", "billing-system", "corr-1006", LocalDateTime.now().minusMinutes(2));
        NotificationRequest newer = validRequestAt("notif-1007", "billing-system", "corr-1007", LocalDateTime.now().minusMinutes(1));

        notificationService.createNotification(older);
        notificationService.createNotification(newer);

        List<NotificationResponse> all = notificationService.getNotifications();

        assertEquals(2, all.size());
        assertEquals("notif-1007", all.get(0).notificationId());
    }

    @Test
    void missingNotificationByIdThrowsResourceNotFound() {
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> notificationService.getNotificationById("missing-id"));

        assertEquals("Notification not found with id: missing-id", exception.getMessage());
    }

    @Test
    void nullRequestIsRejected() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> notificationService.createNotification(null));

        assertEquals("Notification request cannot be null", exception.getMessage());
    }

    private NotificationRequest validRequest(String notificationId, String sourceSystem, String correlationId) {
        return validRequestAt(notificationId, sourceSystem, correlationId, LocalDateTime.now());
    }

    private NotificationRequest validRequestAt(String notificationId, String sourceSystem, String correlationId, LocalDateTime createdAt) {
        return new NotificationRequest(
                notificationId,
                sourceSystem,
                correlationId,
                "ALERT",
                "ERROR",
                "HIGH",
                List.of("ops@example.com"),
                List.of("EMAIL", "SMS"),
                createdAt,
                null,
                null,
                "Payment due",
                "Invoice 1024 is due today"
        );
    }
}
