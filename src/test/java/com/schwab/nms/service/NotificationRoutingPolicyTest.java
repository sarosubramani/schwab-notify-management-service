package com.schwab.nms.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NotificationRoutingPolicyTest {

    private final NotificationRoutingPolicy policy = new NotificationRoutingPolicy();

    @Test
    void resolvesRequestedChannelsWhenProvided() {
        List<String> result = policy.resolveChannels(List.of("EMAIL", "SMS", "PUSH"), "HIGH");

        assertEquals(List.of("EMAIL", "SMS", "PUSH"), result);
    }

    @Test
    void fallsBackToDefaultChannelsForPriorityWhenInputIsEmpty() {
        List<String> result = policy.resolveChannels(List.of(), "HIGH");

        assertEquals(List.of("EMAIL", "SMS"), result);
    }

    @Test
    void includesCriticalDeliveryChannelsByDefault() {
        List<String> result = policy.resolveChannels(null, "CRITICAL");

        assertEquals(List.of("EMAIL", "SMS", "PUSH", "TEAMS"), result);
    }

    @Test
    void invalidRequestedChannelIsRejected() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> policy.resolveChannels(List.of("WHATSAPP"), "HIGH"));

        assertEquals("Unsupported notification channel: WHATSAPP", ex.getMessage());
    }

    @Test
    void invalidPriorityIsRejected() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> policy.resolveChannels(List.of("EMAIL"), "URGENT"));

        assertEquals("Unsupported notification priority: URGENT", ex.getMessage());
    }
}
