package com.schwab.nms.service;

import com.schwab.nms.model.DeliveryAttemptResponse;
import com.schwab.nms.model.NotificationRequest;
import com.schwab.nms.model.NotificationResponse;
import com.schwab.nms.model.NotificationStatusResponse;
import com.schwab.nms.constants.NmsConstants;
import com.schwab.nms.model.StoredNotification;
import com.schwab.nms.enums.NotificationPriority;
import com.schwab.nms.enums.NotificationSeverity;
import com.schwab.nms.enums.NotificationStatus;
import com.schwab.nms.enums.NotificationType;
import com.schwab.nms.exception.ResourceNotFoundException;
import com.schwab.nms.util.NotificationServiceUtils;
import com.schwab.nms.validator.RequestValidator;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static com.schwab.nms.util.NmsUtils.normalizeList;

@Service
public class NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationService.class);

    private final Map<String, StoredNotification> notifications = new ConcurrentHashMap<>();
    private final NotificationRoutingPolicy routingPolicy;

    private final RequestValidator requestValidator;

    @Autowired
    public NotificationService(NotificationRoutingPolicy routingPolicy) {
            this.routingPolicy = routingPolicy;
            this.requestValidator = new RequestValidator();
    }

    public NotificationResponse createNotification(NotificationRequest request) {
        LOGGER.info("Enter: createNotification");
        try {
            if (request == null) {
                throw new IllegalArgumentException(NmsConstants.Messages.ERR_REQUEST_NULL);
            }

            requestValidator.createNotifyValidateRequest(request);
            NotificationServiceUtils.ensureNotDuplicate(request, notifications, routingPolicy);

            List<String> channels = routingPolicy.resolveChannels(normalizeList(request.channels()), request.priority());
            List<String> recipients = normalizeList(request.recipients());
            NotificationPriority priority = NotificationPriority.fromValue(request.priority());
            NotificationSeverity severity = NotificationSeverity.fromValue(request.severity());
            NotificationType notificationType = NotificationType.fromValue(request.notificationType());
            String id = StringUtils.isBlank(request.notificationId()) ? UUID.randomUUID().toString() : request.notificationId();

            StoredNotification notification = new StoredNotification(
                    id,
                    request.sourceSystem(),
                    request.correlationId(),
                    notificationType.name(),
                    severity.name(),
                    priority.name(),
                    recipients,
                    channels,
                    request.createdAt(),
                    request.scheduledAt(),
                    request.expiresAt(),
                    request.title(),
                    request.message(),
                    NotificationStatus.QUEUED.name(),
                    new ArrayList<>(),
                    LocalDateTime.now());

            notifications.put(id, notification);
            processNotificationAsync(id);
            LOGGER.info("Exit: createNotification");
            return NotificationServiceUtils.toResponse(notification);
        } catch (Exception e) {
            LOGGER.error("Error in createNotification", e);
            throw e;
        }
    }

    public NotificationResponse getNotificationById(String id) {
        LOGGER.info("Enter: getNotificationById");
        try {
            if (StringUtils.isBlank(id)) {
                throw new ResourceNotFoundException("Notification", "id", id);
            }

            StoredNotification notification = notifications.get(id);
            if (null == notification) {
                throw new ResourceNotFoundException("Notification", "id", id);
            }

            LOGGER.info("Exit: getNotificationById");
            return NotificationServiceUtils.toResponse(notification);
        } catch (Exception e) {
            LOGGER.error("Error in getNotificationById", e);
            throw e;
        }
    }

    public NotificationStatusResponse getNotificationStatus(String id) {
        LOGGER.info("Enter: getNotificationStatus");
        try {
            if (StringUtils.isBlank(id)) {
                throw new ResourceNotFoundException("Notification", "id", id);
            }

            StoredNotification notification = notifications.get(id);
            if (null == notification) {
                throw new ResourceNotFoundException("Notification", "id", id);
            }

            LOGGER.info("Exit: getNotificationStatus");
            return NotificationServiceUtils.toStatusResponse(notification);
        } catch (Exception e) {
            LOGGER.error("Error in getNotificationStatus", e);
            throw e;
        }
    }

    public List<NotificationResponse> getNotifications() {
        LOGGER.info("Enter: getNotifications");
        try {
            List<NotificationResponse> result = notifications.values().stream()
                    .sorted(Comparator.comparing(StoredNotification::createdAt).reversed())
                    .map(NotificationServiceUtils::toResponse)
                    .toList();
            LOGGER.info("Exit: getNotifications");
            return result;
        } catch (Exception e) {
            LOGGER.error("Error in getNotifications", e);
            throw e;
        }
    }

    @Async
    public CompletableFuture<Void> processNotificationAsync(String id) {
        LOGGER.info("Enter: processNotificationAsync");
        try {
            StoredNotification notification = notifications.get(id);
            if (notification == null) {
                LOGGER.info("Exit: processNotificationAsync as null");
                return CompletableFuture.completedFuture(null);
            }

            NotificationServiceUtils.updateStatus(notifications, id, NotificationStatus.PROCESSING.name());

            boolean hasSuccessfulDelivery = false;
            boolean hasFailure = false;

            for (String channel : notification.channels()) {
                try {
                    Thread.sleep(150L);
                    String provider = NotificationServiceUtils.resolveProvider(channel);
                    DeliveryAttemptResponse attempt = new DeliveryAttemptResponse(
                            channel,
                            notification.deliveryAttempts().size() + 1,
                            NotificationStatus.SENT.name(),
                            provider,
                            NmsConstants.Messages.ATTEMPT_DELIVERED);

                    NotificationServiceUtils.addAttempt(notifications, id, attempt);
                    hasSuccessfulDelivery = true;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    NotificationServiceUtils.addAttempt(notifications, id, new DeliveryAttemptResponse(channel, notification.deliveryAttempts().size() + 1, NotificationStatus.FAILED.name(), NotificationServiceUtils.resolveProvider(channel), NmsConstants.Messages.ATTEMPT_INTERRUPTED));
                    hasFailure = true;
                } catch (Exception e) {
                    NotificationServiceUtils.addAttempt(notifications, id, new DeliveryAttemptResponse(channel, notification.deliveryAttempts().size() + 1, NotificationStatus.FAILED.name(), NotificationServiceUtils.resolveProvider(channel), e.getMessage()));
                    hasFailure = true;
                }
            }

            if (hasFailure && !hasSuccessfulDelivery) {
                NotificationServiceUtils.updateStatus(notifications, id, NotificationStatus.FAILED.name());
            } else if (hasFailure) {
                NotificationServiceUtils.updateStatus(notifications, id, NotificationStatus.PARTIALLY_FAILED.name());
            } else {
                NotificationServiceUtils.updateStatus(notifications, id, NotificationStatus.SENT.name());
            }

            LOGGER.info("Exit: processNotificationAsync");
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            LOGGER.error("Error in processNotificationAsync", e);
            throw e;
        }
    }

}
