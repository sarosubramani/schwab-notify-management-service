package com.schwab.nms.service;

import com.schwab.nms.model.DeliveryAttemptResponse;
import com.schwab.nms.model.NotificationRequest;
import com.schwab.nms.model.NotificationResponse;
import com.schwab.nms.model.NotificationStatusResponse;
import com.schwab.nms.constants.NmsConstants;
import com.schwab.nms.model.StoredNotification;
import com.schwab.nms.enums.NotificationChannel;
import com.schwab.nms.enums.NotificationPriority;
import com.schwab.nms.enums.NotificationSeverity;
import com.schwab.nms.enums.NotificationStatus;
import com.schwab.nms.enums.NotificationType;
import com.schwab.nms.exception.ResourceNotFoundException;
import com.schwab.nms.validator.RequestValidator;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;

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

    private final Map<String, StoredNotification> notifications = new ConcurrentHashMap<>();
    private final NotificationRoutingPolicy routingPolicy;

    @Autowired
    public NotificationService(NotificationRoutingPolicy routingPolicy) {
        this.routingPolicy = routingPolicy;
    }

    @Autowired
    public RequestValidator requestValidator;;

    public NotificationResponse createNotification(NotificationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException(NmsConstants.Messages.ERR_REQUEST_NULL);
        }

        requestValidator.createNotifyValidateRequest(request);
        ensureNotDuplicate(request);

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
        return toResponse(notification);
    }

    public NotificationResponse getNotificationById(String id) {
        if (StringUtils.isNotBlank(id)) {
            throw new ResourceNotFoundException("Notification", "id", id);
        }

        StoredNotification notification = notifications.get(id);
        if (ObjectUtils.isNotEmpty(notification)) {
            throw new ResourceNotFoundException("Notification", "id", id);
        }

        return toResponse(notification);
    }

    public List<NotificationResponse> getNotifications() {
        return notifications.values().stream()
                .sorted(Comparator.comparing(StoredNotification::createdAt).reversed())
                .map(this::toResponse)
                .toList();
    }

    public NotificationStatusResponse getNotificationStatus(String id) {
        NotificationResponse response = getNotificationById(id);
        StoredNotification notification = notifications.get(id);
        List<NotificationStatusResponse.RecipientDeliveryStatus> recipientStatuses = new ArrayList<>();

        for (String recipient : notification.recipients()) {
            for (String channel : notification.channels()) {
                String status = notification.deliveryAttempts().stream()
                        .filter(attempt -> attempt.channel().equalsIgnoreCase(channel))
                        .map(DeliveryAttemptResponse::status)
                        .reduce((first, second) -> second)
                        .orElse(NmsConstants.Messages.DEFAULT_PENDING);

                recipientStatuses.add(new NotificationStatusResponse.RecipientDeliveryStatus(recipient, channel, status));
            }
        }

        return new NotificationStatusResponse(
                        notification.id(),
                        notification.id(),
                response.status(),
                        notification.channels(),
                recipientStatuses,
                        notification.createdAt(),
                        notification.scheduledAt(),
                        notification.expiresAt(),
                LocalDateTime.now());
    }

    @Async
    public CompletableFuture<Void> processNotificationAsync(String id) {
        StoredNotification notification = notifications.get(id);
        if (notification == null) {
            return CompletableFuture.completedFuture(null);
        }

        updateStatus(id, NotificationStatus.PROCESSING.name());

        boolean hasSuccessfulDelivery = false;
        boolean hasFailure = false;

        for (String channel : notification.channels()) {
            try {
                Thread.sleep(150L);
                String provider = resolveProvider(channel);
                DeliveryAttemptResponse attempt = new DeliveryAttemptResponse(
                        channel,
                        notification.deliveryAttempts().size() + 1,
                        NotificationStatus.SENT.name(),
                        provider,
                        NmsConstants.Messages.ATTEMPT_DELIVERED);

                addAttempt(id, attempt);
                hasSuccessfulDelivery = true;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                addAttempt(id, new DeliveryAttemptResponse(channel, notification.deliveryAttempts().size() + 1, NotificationStatus.FAILED.name(), resolveProvider(channel), NmsConstants.Messages.ATTEMPT_INTERRUPTED));
                hasFailure = true;
            } catch (Exception e) {
                addAttempt(id, new DeliveryAttemptResponse(channel, notification.deliveryAttempts().size() + 1, NotificationStatus.FAILED.name(), resolveProvider(channel), e.getMessage()));
                hasFailure = true;
            }
        }

        if (hasFailure && !hasSuccessfulDelivery) {
            updateStatus(id, NotificationStatus.FAILED.name());
        } else if (hasFailure) {
            updateStatus(id, NotificationStatus.PARTIALLY_FAILED.name());
        } else {
            updateStatus(id, NotificationStatus.SENT.name());
        }

        return CompletableFuture.completedFuture(null);
    }

    private void updateStatus(String id, String status) {
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
    }

    private void addAttempt(String id, DeliveryAttemptResponse attempt) {
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
    }

    private String resolveProvider(String channel) {
        return switch (NotificationChannel.fromValue(channel)) {
            case EMAIL -> NmsConstants.Providers.SMTP;
            case SMS -> NmsConstants.Providers.TWILIO;
            case PUSH -> NmsConstants.Providers.FCM;
            case IN_APP -> NmsConstants.Providers.NMS_INBOX;
            case SLACK -> NmsConstants.Providers.SLACK_WEBHOOK;
            case TEAMS -> NmsConstants.Providers.TEAMS_WEBHOOK;
        };
    }



    private void ensureNotDuplicate(NotificationRequest request) {
        String fingerprint = com.schwab.nms.util.NmsUtils.buildFingerprint(request, routingPolicy);
        for (StoredNotification existing : notifications.values()) {
            if (com.schwab.nms.util.NmsUtils.buildFingerprint(existing).equals(fingerprint)) {
                throw new IllegalArgumentException(NmsConstants.Messages.ERR_DUPLICATE);
            }
        }
    }

    private NotificationResponse toResponse(StoredNotification notification) {
        return new NotificationResponse(
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
    }

}

