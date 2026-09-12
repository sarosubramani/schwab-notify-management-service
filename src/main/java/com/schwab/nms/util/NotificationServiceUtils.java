package com.schwab.nms.util;

import com.schwab.nms.constants.NmsConstants;
import com.schwab.nms.enums.NotificationChannel;
import com.schwab.nms.model.DeliveryAttemptResponse;
import com.schwab.nms.model.NotificationRequest;
import com.schwab.nms.model.NotificationResponse;
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
        LOGGER.info("Enter: updateStatus");
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
            LOGGER.info("Exit: updateStatus");
        } catch (Exception e) {
            LOGGER.error("Error in updateStatus", e);
            throw e;
        }
    }

    public static void addAttempt(Map<String, StoredNotification> notifications, String id, DeliveryAttemptResponse attempt) {
        LOGGER.info("Enter: addAttempt");
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
            LOGGER.info("Exit: addAttempt");
        } catch (Exception e) {
            LOGGER.error("Error in addAttempt", e);
            throw e;
        }
    }

    public static String resolveProvider(String channel) {
        LOGGER.info("Enter: resolveProvider");
        try {
            String provider = switch (NotificationChannel.fromValue(channel)) {
                case EMAIL -> NmsConstants.Providers.SMTP;
                case SMS -> NmsConstants.Providers.TWILIO;
                case PUSH -> NmsConstants.Providers.FCM;
                case IN_APP -> NmsConstants.Providers.NMS_INBOX;
                case SLACK -> NmsConstants.Providers.SLACK_WEBHOOK;
                case TEAMS -> NmsConstants.Providers.TEAMS_WEBHOOK;
            };
            LOGGER.info("Exit: resolveProvider");
            return provider;
        } catch (Exception e) {
            LOGGER.error("Error in resolveProvider", e);
            throw e;
        }
    }

    public static void ensureNotDuplicate(NotificationRequest request, Map<String, StoredNotification> notifications, NotificationRoutingPolicy routingPolicy) {
        LOGGER.info("Enter: ensureNotDuplicate");
        try {
            String fingerprint = buildFingerprint(request, routingPolicy);
            for (StoredNotification existing : notifications.values()) {
                if (buildFingerprint(existing).equals(fingerprint)) {
                    throw new IllegalArgumentException(NmsConstants.Messages.ERR_DUPLICATE);
                }
            }
            LOGGER.info("Exit: ensureNotDuplicate");
        } catch (Exception e) {
            LOGGER.error("Error in ensureNotDuplicate", e);
            throw e;
        }
    }

    public static NotificationResponse toResponse(StoredNotification notification) {
        LOGGER.info("Enter: toResponse");
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
            LOGGER.info("Exit: toResponse");
            return result;
        } catch (Exception e) {
            LOGGER.error("Error in toResponse", e);
            throw e;
        }
    }
}