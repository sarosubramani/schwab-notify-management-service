package com.schwab.nms.util;

import com.schwab.nms.model.NotificationRequest;
import com.schwab.nms.model.StoredNotification;
import com.schwab.nms.service.NotificationRoutingPolicy;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class NmsUtilsTest {

    @Test
    void normalizeList_removesBlanksAndDuplicates() {
        List<String> values = List.of(" email ", "", "email", " SMS ", "sms", " ", "PUSH");

        assertEquals(List.of("email", "SMS", "PUSH"), NmsUtils.normalizeList(values));
        assertEquals(List.of(), NmsUtils.normalizeList(null));
    }

    @Test
    void buildFingerprint_forRequest_isStableAcrossEquivalentInputs() {
        NotificationRoutingPolicy policy = new NotificationRoutingPolicy();
        NotificationRequest request = new NotificationRequest(
                "notif-001",
                "billing-system",
                "corr-001",
                "ALERT",
                "ERROR",
                "HIGH",
                List.of("b@example.com", "a@example.com", "a@example.com"),
                List.of("EMAIL", "SMS", " email "),
                LocalDateTime.of(2026, 9, 12, 9, 0, 0),
                null,
                null,
                "Payment due",
                "Invoice 1024 is due today"
        );

        String fingerprint = NmsUtils.buildFingerprint(request, policy);

        assertNotNull(fingerprint);
        assertEquals(fingerprint, NmsUtils.buildFingerprint(request, policy));
    }

    @Test
    void buildFingerprint_forStoredNotification_isStable() {
        StoredNotification notification = new StoredNotification(
                "notif-002",
                "billing-system",
                "corr-002",
                "ALERT",
                "ERROR",
                "HIGH",
                List.of("b@example.com", "a@example.com"),
                List.of("EMAIL", "SMS"),
                LocalDateTime.of(2026, 9, 12, 9, 0, 0),
                null,
                null,
                "Payment due",
                "Invoice 1024 is due today",
                "QUEUED",
                List.of(),
                LocalDateTime.now()
        );

        assertNotNull(NmsUtils.buildFingerprint(notification));
        assertEquals(NmsUtils.buildFingerprint(notification), NmsUtils.buildFingerprint(notification));
    }

    @Test
    void normalizeList_keepsInsertionOrderForUniqueValues() {
        List<String> values = List.of("PUSH", "EMAIL", "PUSH", "SMS", "EMAIL");

        assertEquals(List.of("PUSH", "EMAIL", "SMS"), NmsUtils.normalizeList(values));
    }
}
