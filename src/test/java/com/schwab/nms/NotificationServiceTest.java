package com.schwab.nms;

import com.schwab.nms.model.NotificationRequest;
import com.schwab.nms.model.NotificationResponse;
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
    void greenfieldCreateNotificationShouldReturnQueuedResponse() {
        NotificationRequest request = new NotificationRequest(
                "notif-1001",
                "billing-system",
                "evt-9001",
                "ALERT",
                "ERROR",
                "HIGH",
                List.of("customer@example.com"),
                List.of("EMAIL", "SMS"),
                LocalDateTime.now(),
                null,
                null,
                "Payment due",
                "Invoice 1024 is due today");

        NotificationResponse response = notificationService.createNotification(request);

        assertNotNull(response.id());
        assertEquals("billing-system", response.sourceSystem());
        assertEquals("EMAIL", response.channels().get(0));
        assertEquals("QUEUED", response.status());
    }

    @Test
    void brownfieldShouldSupportTeamsChannel() {
        NotificationRequest request = new NotificationRequest(
                "notif-1002",
                "operations-system",
                "evt-9002",
                "INFO",
                "WARNING",
                "MEDIUM",
                List.of("ops-team@company.com"),
                List.of("TEAMS"),
                LocalDateTime.now(),
                null,
                null,
                "Incident update",
                "Database failover complete");

        NotificationResponse response = notificationService.createNotification(request);

        assertEquals("TEAMS", response.channels().get(0));
        assertEquals("MEDIUM", response.priority());
    }

    @Test
    void ambiguousRequirementShouldDefaultChannelsForPriority() {
        NotificationRequest request = new NotificationRequest(
                "notif-1003",
                "risk-system",
                "evt-9003",
                "SECURITY",
                "CRITICAL",
                "HIGH",
                List.of("risk@example.com"),
                List.of(),
                LocalDateTime.now(),
                null,
                null,
                "Risk alert",
                "Account performance threshold breached");

        NotificationResponse response = notificationService.createNotification(request);

        assertEquals(List.of("EMAIL", "SMS"), response.channels());
    }

    @Test
    void duplicateRequestShouldBeRejected() {
        NotificationRequest request = new NotificationRequest(
                "notif-1004",
                "fraud-monitor",
                "evt-9004",
                "SECURITY",
                "CRITICAL",
                "CRITICAL",
                List.of("ops@example.com"),
                List.of("EMAIL", "PUSH"),
                LocalDateTime.now(),
                null,
                null,
                "Suspicious activity",
                "A high-risk transaction was flagged");

        notificationService.createNotification(request);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> notificationService.createNotification(request));

        assertEquals("Duplicate notification request detected", exception.getMessage());
    }

    @Test
    void getNotificationShouldReturnStoredNotificationById() {
        NotificationRequest request = new NotificationRequest(
                "notif-1005",
                "fraud-monitor",
                "evt-9005",
                "SECURITY",
                "CRITICAL",
                "CRITICAL",
                List.of("ops@example.com"),
                List.of("EMAIL", "PUSH"),
                LocalDateTime.now(),
                null,
                null,
                "Suspicious activity",
                "A high-risk transaction was flagged");

        NotificationResponse created = notificationService.createNotification(request);
        NotificationResponse found = notificationService.getNotificationById(created.id());

        assertEquals(created.id(), found.id());
        assertEquals("fraud-monitor", found.sourceSystem());
    }
}
