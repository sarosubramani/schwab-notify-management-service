package com.schwab.nms.util;

import com.schwab.nms.constants.NmsConstants;
import com.schwab.nms.enums.NotificationChannel;
import com.schwab.nms.model.DeliveryAttemptResponse;
import com.schwab.nms.model.NotificationRequest;
import com.schwab.nms.model.NotificationResponse;
import com.schwab.nms.model.NotificationStatusResponse;
import com.schwab.nms.model.StoredNotification;
import com.schwab.nms.service.NotificationRoutingPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.schwab.nms.util.NmsUtils.buildFingerprint;

public final class NotificationServiceUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationServiceUtils.class);

    private NotificationServiceUtils() {}

    public static void updateStatus(Map<String, StoredNotification> notifications, String id, String status) {
        LOGGER.debug("Enter: updateStatus");
        try {
            StoredNotification notification = notifications.get(id);
            if (notification != null) {
                notifications.put(id, new StoredNotification(
                        notification.id(),
                        notification.sourceSystem(),
                        notification.correlationId(),
                        notification.notificationType(),
                        notification.severity(),
                        notification.priority(),
                        notification.recipients(),
                        notification.channels(),
                        notification.createdAt(),
                        notification.scheduledAt(),
                        notification.expiresAt(),
                        notification.title(),
                        notification.message(),
                        status,
                        notification.deliveryAttempts(),
                        notification.receivedAt()));
            }
            LOGGER.debug("Exit: updateStatus");
        } catch (Exception e) {
            LOGGER.error("Error in updateStatus", e);
            throw e;
        }
    }

    public static void addAttempt(Map<String, StoredNotification> notifications, String id, DeliveryAttemptResponse attempt) {
        LOGGER.debug("Enter: addAttempt");
        try {
            StoredNotification notification = notifications.get(id);
            if (notification != null) {
                List<DeliveryAttemptResponse> attempts = new ArrayList<>(notification.deliveryAttempts());
                attempts.add(attempt);
                notifications.put(id, new StoredNotification(
                        notification.id(),
                        notification.sourceSystem(),
                        notification.correlationId(),
                        notification.notificationType(),
                        notification.severity(),
                        notification.priority(),
                        notification.recipients(),
                        notification.channels(),
                        notification.createdAt(),
                        notification.scheduledAt(),
                        notification.expiresAt(),
                        notification.title(),
                        notification.message(),
                        notification.status(),
                        attempts,
                        notification.receivedAt()));
            }
            LOGGER.debug("Exit: addAttempt");
        } catch (Exception e) {
            LOGGER.error("Error in addAttempt", e);
            throw e;
        }
    }

    public static String resolveProvider(String channel) {
        LOGGER.debug("Enter: resolveProvider");
        try {
            String provider = switch (NotificationChannel.fromValue(channel)) {
                case EMAIL -> NmsConstants.Providers.SMTP;
                case SMS -> NmsConstants.Providers.TWILIO;
                case PUSH -> NmsConstants.Providers.FCM;
                case IN_APP -> NmsConstants.Providers.NMS_INBOX;
                case SLACK -> NmsConstants.Providers.SLACK_WEBHOOK;
                case TEAMS -> NmsConstants.Providers.TEAMS_WEBHOOK;
            };
            LOGGER.debug("Exit: resolveProvider");
            return provider;
        } catch (Exception e) {
            LOGGER.error("Error in resolveProvider", e);
            throw e;
        }
    }

    public static void ensureNotDuplicate(NotificationRequest request, Map<String, StoredNotification> notifications, NotificationRoutingPolicy routingPolicy) {
        LOGGER.debug("Enter: ensureNotDuplicate");
        try {
            String fingerprint = buildFingerprint(request, routingPolicy);
            for (StoredNotification existing : notifications.values()) {
                if (buildFingerprint(existing).equals(fingerprint)) {
                    throw new IllegalArgumentException(NmsConstants.Messages.ERR_DUPLICATE);
                }
            }
            LOGGER.debug("Exit: ensureNotDuplicate");
        } catch (Exception e) {
            LOGGER.error("Error in ensureNotDuplicate", e);
            throw e;
        }
    }

    public static NotificationResponse toResponse(StoredNotification notification) {
        LOGGER.debug("Enter: toResponse");
        try {
            NotificationResponse result = new NotificationResponse(
                    notification.id(),
                    notification.id(),
                    notification.sourceSystem(),
                    notification.correlationId(),
                    notification.notificationType(),
                    notification.severity(),
                    notification.priority(),
                    notification.recipients(),
                    notification.channels(),
                    notification.createdAt(),
                    notification.scheduledAt(),
                    notification.expiresAt(),
                    notification.title(),
                    notification.message(),
                    notification.status(),
                    notification.deliveryAttempts());
            LOGGER.debug("Exit: toResponse");
            return result;
        } catch (Exception e) {
            LOGGER.error("Error in toResponse", e);
            throw e;
        }
    }

    public static NotificationStatusResponse toStatusResponse(StoredNotification notification) {
        LOGGER.debug("Enter: toStatusResponse");
        try {
            List<NotificationStatusResponse.RecipientDeliveryStatus> recipientStatus = notification.recipients().stream()
                    .map(recipient -> new NotificationStatusResponse.RecipientDeliveryStatus(
                            recipient,
                            notification.channels().isEmpty() ? "N/A" : notification.channels().getFirst(),
                            notification.status()))
                    .toList();

            NotificationStatusResponse result = new NotificationStatusResponse(
                    notification.id(),
                    notification.id(),
                    notification.status(),
                    notification.channels(),
                    recipientStatus,
                    notification.createdAt(),
                    notification.scheduledAt(),
                    notification.expiresAt(),
                    notification.receivedAt());
            LOGGER.debug("Exit: toStatusResponse");
            return result;
        } catch (Exception e) {
            LOGGER.error("Error in toStatusResponse", e);
            throw e;
        }
    }
}