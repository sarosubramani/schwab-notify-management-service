# Schwab Notification Management Service

A Spring Boot microservice for creating, tracking, and delivering notifications across multiple channels such as email, SMS, push, in-app, Slack, and Teams.

## Features

- Create notifications with validation
- Resolve recipient channels based on routing policy
- Track notification lifecycle and delivery attempts
- Retrieve notifications and status by ID
- Persist notifications in a database-backed repository when configured
- Retry retryable downstream delivery failures using Resilience4j
- Expose REST APIs for client integration

## Tech Stack

- Java 21
- Spring Boot 3.x
- Maven
- Spring JDBC / H2 / MySQL-compatible configuration
- Resilience4j retry support
- JUnit 5 + Mockito
- JaCoCo for coverage enforcement

## Project Structure

```text
src/
  main/
    java/com/schwab/nms/
      constants/
      controller/
      delivery/
      enums/
      exception/
      handler/
      model/
      repository/
      service/
      util/
      validator/
      NmsApplication.java
  resources/
    application.properties
    schema.sql
  test/
    java/com/schwab/nms/
```

## API Endpoints

Base URL:

```text
http://localhost:8080/api/v1
```

### 1. Create notification

- Method: POST
- Path: `/notifications`

Request body example:

```json
{
  "notificationId": "notif-1001",
  "sourceSystem": "billing-system",
  "correlationId": "corr-1001",
  "notificationType": "ALERT",
  "severity": "ERROR",
  "priority": "HIGH",
  "recipients": ["ops@example.com", "+15551234567"],
  "channels": ["EMAIL", "SMS"],
  "createdAt": "2026-09-12T17:40:00",
  "scheduledAt": null,
  "expiresAt": null,
  "title": "Payment due",
  "message": "Invoice 1024 is due today"
}
```

### 2. Get all notifications

- Method: GET
- Path: `/getnotifications`

### 3. Get notification by ID

- Method: GET
- Path: `/notifications/{id}`

### 4. Get notification status by ID

- Method: GET
- Path: `/notifications/{id}/status`

## Delivery Behavior

Notifications are processed asynchronously after creation. Each channel attempt records a delivery attempt and the final notification status can be:

- `QUEUED`
- `PROCESSING`
- `SENT`
- `PARTIALLY_FAILED`
- `FAILED`

Retry handling is applied for retryable downstream failure types, including:

- transient provider failures
- timeouts
- rate-limit responses

Non-retryable failures such as invalid recipients, permanent rejections, and auth failures are not retried.

## Local Setup

### Prerequisites

- Java 21
- Maven wrapper (`mvnw` included)

### Run the app

From the project root:

```bash
./mvnw spring-boot:run
```

or on Windows:

```powershell
./mvnw.cmd spring-boot:run
```

## Database Configuration

The app includes database-backed storage support. By default it is compatible with H2 for local bootability, and can also be configured for MySQL-like databases via `application.properties`.

## Testing

Run the test suite:

```bash
./mvnw test
```

JaCoCo is configured to enforce coverage threshold checks.

## API Documentation

Swagger/OpenAPI is available through the Spring Boot actuator/documentation endpoints once the app is running. The controller is annotated with OpenAPI metadata for request/response documentation.

## Notes

This service is designed to act as a notification orchestration layer, storing notification metadata and tracking async delivery attempts without directly depending on a real external provider implementation.
