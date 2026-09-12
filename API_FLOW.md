# Notification API Flow

Base path: `/api/v1`

## 1) POST /api/v1/notifications

Purpose: Create a notification request and queue it for delivery.

Flow:
1. Client sends a JSON request body using `NotificationRequest`.
2. `NotificationController.createNotification` receives the request with `@Valid @RequestBody`.
3. The controller calls `NotificationHandler.createNotification(request)`.
4. The handler delegates to `NotificationService.createNotification(request)`.
5. The service validates the request:
   - `sourceSystem`
   - `notificationType`
   - `severity`
   - `priority`
   - `recipients`
   - `createdAt`
   - `title`
   - `message`
   - each channel is validated using `NotificationChannel.fromValue(...)`
   - enum conversion for priority, severity, and type is validated
6. Duplicate protection is performed:
   - `NotificationServiceUtils.ensureNotDuplicate(...)`
   - repository-level duplicate check is also applied when DB persistence is enabled
7. The service normalizes channels and recipients and resolves delivery routing via `NotificationRoutingPolicy`.
8. If no `notificationId` is provided, a UUID is generated.
9. A `StoredNotification` record is created with status set to `QUEUED`.
10. The notification is persisted to the in-memory map or the database repository.
11. The service starts async processing with `processNotificationAsync(id)`.
12. A `201 Created` response is returned with `NotificationResponse`.

Delivery processing:
- The service loads the stored notification.
- Status is updated to `PROCESSING`.
- For each selected channel, the service calls `attemptDeliveryWithRetry(...)`.
- Retry logic is applied only for retryable downstream failures.
- Each delivery attempt is recorded in `deliveryAttempts`.
- Final state is determined as:
  - `SENT` if all channels succeed
  - `PARTIALLY_FAILED` if some succeed and some fail
  - `FAILED` if all fail

## 2) GET /api/v1/getnotifications

Purpose: Retrieve all notifications.

Flow:
1. Controller exposes `GET /api/v1/getnotifications`.
2. `NotificationController.getNotifications()` calls `NotificationHandler.getNotifications()`.
3. Handler delegates to `NotificationService.getNotifications()`.
4. Service loads all stored notifications.
5. Notifications are sorted newest-first by `createdAt`.
6. Each `StoredNotification` is mapped to `NotificationResponse`.
7. Response returns `200 OK` with a list of notifications.

## 3) GET /api/v1/notifications/{id}

Purpose: Fetch a single notification by ID.

Flow:
1. Client calls `GET /api/v1/notifications/{id}`.
2. `NotificationController.getNotificationById` receives `@PathVariable String id`.
3. Handler delegates to `NotificationService.getNotificationById(id)`.
4. Service validates that `id` is not blank.
5. Service loads the notification by ID from repository or in-memory map.
6. If missing, it throws `ResourceNotFoundException`.
7. Response returns `200 OK` with a single `NotificationResponse`.

## 4) GET /api/v1/notifications/{id}/status

Purpose: Return the current status for a single notification.

Flow:
1. Client calls `GET /api/v1/notifications/{id}/status`.
2. `NotificationController.getNotificationStatus` receives the path variable.
3. Handler delegates to `NotificationService.getNotificationStatus(id)`.
4. Service validates non-blank `id`.
5. Service loads the notification.
6. If missing, it throws `ResourceNotFoundException`.
7. Response returns `200 OK` with `NotificationStatusResponse` containing:
   - overall status
   - selected channels
   - recipient-by-recipient status
   - created/scheduled/expiry timestamps
   - last updated time

## NotificationRequest Model

```java
public record NotificationRequest(
        String notificationId,
        String sourceSystem,
        String correlationId,
        String notificationType,
        String severity,
        String priority,
        List<String> recipients,
        List<String> channels,
        LocalDateTime createdAt,
        LocalDateTime scheduledAt,
        LocalDateTime expiresAt,
        String title,
        String message) {
}
```

## NotificationResponse Model

```java
public record NotificationResponse(
        String id,
        String notificationId,
        String sourceSystem,
        String correlationId,
        String notificationType,
        String severity,
        String priority,
        List<String> recipients,
        List<String> channels,
        LocalDateTime createdAt,
        LocalDateTime scheduledAt,
        LocalDateTime expiresAt,
        String title,
        String message,
        String status,
        List<DeliveryAttemptResponse> deliveryAttempts) {
}
```

## NotificationStatusResponse Model

```java
public record NotificationStatusResponse(
        String id,
        String notificationId,
        String overallStatus,
        List<String> selectedChannels,
        List<RecipientDeliveryStatus> recipientDeliveryStatus,
        LocalDateTime createdAt,
        LocalDateTime scheduledAt,
        LocalDateTime expiresAt,
        LocalDateTime lastUpdatedAt) {

    public record RecipientDeliveryStatus(
            String recipient,
            String channel,
            String status) {
    }
}
```

## Request Validation Rules

The validator checks for:
- non-blank source system
- non-blank notification type
- non-blank severity
- non-blank priority
- at least one recipient
- required created timestamp
- non-blank title
- non-blank message
- supported channel values
- valid priority, severity, and notification type values

## Error Handling

Common failure modes:
- invalid request payload -> 400 / validation exception
- notification not found -> 404 / `ResourceNotFoundException`
- internal service issue -> 500

## Summary

The service supports create, list, fetch-by-id, and status-check APIs for notifications, with asynchronous downstream delivery attempts and retry handling for retryable failures.
