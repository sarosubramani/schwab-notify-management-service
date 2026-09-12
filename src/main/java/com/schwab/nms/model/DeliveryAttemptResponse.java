package com.schwab.nms.model;

public record DeliveryAttemptResponse(
        String channel,
        int attemptNumber,
        String status,
        String provider,
        String message) {
}
