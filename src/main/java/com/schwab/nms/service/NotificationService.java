package com.schwab.nms.service;

import com.schwab.nms.constants.NmsConstants;
import com.schwab.nms.delivery.DeliveryFailureClassifier;
import com.schwab.nms.enums.DeliveryFailureType;
import com.schwab.nms.exception.DownstreamDeliveryException;
import com.schwab.nms.enums.NotificationPriority;
import com.schwab.nms.enums.NotificationSeverity;
import com.schwab.nms.enums.NotificationStatus;
import com.schwab.nms.enums.NotificationType;
import com.schwab.nms.exception.ResourceNotFoundException;
import com.schwab.nms.model.DeliveryAttemptResponse;
import com.schwab.nms.model.NotificationRequest;
import com.schwab.nms.model.NotificationResponse;
import com.schwab.nms.model.NotificationStatusResponse;
import com.schwab.nms.model.StoredNotification;
import com.schwab.nms.repository.NotificationRepository;
import com.schwab.nms.util.NotificationServiceUtils;
import com.schwab.nms.validator.RequestValidator;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
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
    private static final int MAX_DELIVERY_RETRIES = 3;

    private final Map<String, StoredNotification> notifications = new ConcurrentHashMap<>();
    private final NotificationRoutingPolicy routingPolicy;
    private final RequestValidator requestValidator;
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRoutingPolicy routingPolicy) {
        this(routingPolicy, null);
    }

    @Autowired
    public NotificationService(NotificationRoutingPolicy routingPolicy, NotificationRepository notificationRepository) {
        this.routingPolicy = routingPolicy;
        this.requestValidator = new RequestValidator();
        this.notificationRepository = notificationRepository;
    }

    public NotificationResponse createNotification(NotificationRequest request) {
        LOGGER.info("Enter: createNotification");
        try {
            if (request == null) {
                throw new IllegalArgumentException(NmsConstants.Messages.ERR_REQUEST_NULL);
            }

            requestValidator.createNotifyValidateRequest(request);
            NotificationServiceUtils.ensureNotDuplicate(request, notifications, routingPolicy);
            if (notificationRepository != null && notificationRepository.findAll().stream()
                    .anyMatch(existing -> com.schwab.nms.util.NmsUtils.buildFingerprint(existing).equals(com.schwab.nms.util.NmsUtils.buildFingerprint(request, routingPolicy)))) {
                throw new IllegalArgumentException(NmsConstants.Messages.ERR_DUPLICATE);
            }

            List<String> channels = routingPolicy.resolveChannels(normalizeList(request.channels()), request.priority());
            List<String> recipients = normalizeList(request.recipients());
            NotificationPriority priority = NotificationPriority.fromValue(request.priority());
            NotificationSeverity severity = NotificationSeverity.fromValue(request.severity());
            NotificationType notificationType = NotificationType.fromValue(request.notificationType());
            String id = StringUtils.isBlank(request.notificationId()) ? UUID.randomUUID().toString() : request.notificationId();

            StoredNotification notification = new StoredNotification(
                    id,
                    request.sourceSystem(),
                    StringUtils.isBlank(request.correlationId()) ? UUID.randomUUID().toString() : request.correlationId(),
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

            NotificationServiceUtils.saveNotification(notifications, notificationRepository, notification);
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
                throw new ResourceNotFoundException(NmsConstants.Resource.NOTIFICATION, NmsConstants.Field.ID, id);
            }

            StoredNotification notification = NotificationServiceUtils.loadNotification(notifications, notificationRepository, id);
            if (notification == null) {
                throw new ResourceNotFoundException(NmsConstants.Resource.NOTIFICATION, NmsConstants.Field.ID, id);
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
                throw new ResourceNotFoundException(NmsConstants.Resource.NOTIFICATION, NmsConstants.Field.ID, id);
            }

            StoredNotification notification = NotificationServiceUtils.loadNotification(notifications, notificationRepository, id);
            if (notification == null) {
                throw new ResourceNotFoundException(NmsConstants.Resource.NOTIFICATION, NmsConstants.Field.ID, id);
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
            List<NotificationResponse> result = NotificationServiceUtils.loadAllNotifications(notifications, notificationRepository).stream()
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
            StoredNotification notification = NotificationServiceUtils.loadNotification(notifications, notificationRepository, id);
            if (notification == null) {
                LOGGER.info("Exit: processNotificationAsync as null");
                return CompletableFuture.completedFuture(null);
            }

            NotificationServiceUtils.updateStoredStatus(notifications, notificationRepository, id, NotificationStatus.PROCESSING.name());

            boolean hasSuccessfulDelivery = false;
            boolean hasFailure = false;

            for (String channel : notification.channels()) {
                try {
                    DeliveryAttemptResponse attempt = attemptDeliveryWithRetry(id, channel, notification);
                    NotificationServiceUtils.addStoredAttempt(notifications, notificationRepository, id, attempt);
                    if (NotificationStatus.SENT.name().equals(attempt.status())) {
                        hasSuccessfulDelivery = true;
                    } else {
                        hasFailure = true;
                    }
                } catch (Exception e) {
                    NotificationServiceUtils.addStoredAttempt(notifications, notificationRepository, id, new DeliveryAttemptResponse(channel, notification.deliveryAttempts().size() + 1, NotificationStatus.FAILED.name(), NotificationServiceUtils.resolveProvider(channel), e.getMessage()));
                    hasFailure = true;
                }
            }

            if (hasFailure && !hasSuccessfulDelivery) {
                NotificationServiceUtils.updateStoredStatus(notifications, notificationRepository, id, NotificationStatus.FAILED.name());
            } else if (hasFailure) {
                NotificationServiceUtils.updateStoredStatus(notifications, notificationRepository, id, NotificationStatus.PARTIALLY_FAILED.name());
            } else {
                NotificationServiceUtils.updateStoredStatus(notifications, notificationRepository, id, NotificationStatus.SENT.name());
            }

            LOGGER.info("Exit: processNotificationAsync");
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            LOGGER.error("Error in processNotificationAsync", e);
            throw e;
        }
    }

    private DeliveryAttemptResponse attemptDeliveryWithRetry(String notificationId, String channel, StoredNotification notification) {
        String provider = NotificationServiceUtils.resolveProvider(channel);
        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(MAX_DELIVERY_RETRIES)
                .waitDuration(Duration.ofMillis(200L))
                .retryOnException(DeliveryFailureClassifier::isRetryable)
                .failAfterMaxAttempts(true)
                .build();
        Retry retry = Retry.of("notification-delivery-" + notificationId + "-" + channel, retryConfig);

        try {
            String result = Retry.decorateSupplier(retry, () -> executeDownstreamDelivery(channel, provider, notification)).get();
            return new DeliveryAttemptResponse(
                    channel,
                    notification.deliveryAttempts().size() + 1,
                    NotificationStatus.SENT.name(),
                    provider,
                    result);
        } catch (Exception e) {
            DeliveryFailureType failureType = DeliveryFailureClassifier.classify(e);
            String failureMessage = failureType.getDescription() + ": " + (e.getMessage() == null ? "downstream failure" : e.getMessage());
            return new DeliveryAttemptResponse(
                    channel,
                    notification.deliveryAttempts().size() + 1,
                    NotificationStatus.FAILED.name(),
                    provider,
                    failureMessage);
        }
    }

    private String executeDownstreamDelivery(String channel, String provider, StoredNotification notification) {
        String recipient = notification.recipients().isEmpty() ? "unknown" : notification.recipients().getFirst();

        if (notification.message().contains("transient")) {
            throw new DownstreamDeliveryException(DeliveryFailureType.TRANSIENT_PROVIDER_FAILURE, provider + " transient failure");
        }
        if (notification.message().contains("timeout")) {
            throw new DownstreamDeliveryException(DeliveryFailureType.TIMEOUT, provider + " timed out");
        }
        if (notification.message().contains("rate-limit") || notification.message().contains("rate limit")) {
            throw new DownstreamDeliveryException(DeliveryFailureType.RATE_LIMITED, provider + " rate limited");
        }
        if (notification.message().contains("auth")) {
            throw new DownstreamDeliveryException(DeliveryFailureType.AUTHENTICATION_OR_AUTHORIZATION_FAILURE, provider + " authentication/authorization failed");
        }
        if (notification.message().contains("invalid") || "invalid@example.com".equalsIgnoreCase(recipient)) {
            throw new DownstreamDeliveryException(DeliveryFailureType.INVALID_RECIPIENT, "Invalid recipient: " + recipient);
        }
        if (notification.message().contains("rejected") || notification.message().contains("permanent")) {
            throw new DownstreamDeliveryException(DeliveryFailureType.PERMANENT_PROVIDER_REJECTION, provider + " rejected notification");
        }

        try {
            Thread.sleep(150L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DownstreamDeliveryException(DeliveryFailureType.TIMEOUT, provider + " timed out while contacting downstream", e);
        }

        return NmsConstants.Messages.ATTEMPT_DELIVERED;
    }

}
