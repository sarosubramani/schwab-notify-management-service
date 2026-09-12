package com.schwab.nms.repository;

import com.schwab.nms.model.DeliveryAttemptResponse;
import com.schwab.nms.model.NotificationRequest;
import com.schwab.nms.model.StoredNotification;
import com.schwab.nms.service.NotificationRoutingPolicy;
import com.schwab.nms.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificationRepositoryTest {

    private NotificationRepository repository;

    @BeforeEach
    void setUp() {
        DataSource dataSource = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .setName("notification-repo-" + UUID.randomUUID())
                .addScript("schema.sql")
                .build();
        repository = new NotificationRepository(new JdbcTemplate(dataSource));
    }

    @Test
    void repositoryPersistsAndLoadsNotifications() {
        StoredNotification notification = new StoredNotification(
                "db-1",
                "billing-system",
                "corr-db-1",
                "ALERT",
                "ERROR",
                "HIGH",
                List.of("ops@example.com"),
                List.of("EMAIL", "SMS"),
                LocalDateTime.now(),
                null,
                null,
                "DB title",
                "DB message",
                "QUEUED",
                List.of(new DeliveryAttemptResponse("EMAIL", 1, "SENT", "SMTP", "ok")),
                LocalDateTime.now()
        );

        repository.save(notification);

        Optional<StoredNotification> found = repository.findById("db-1");
        assertTrue(found.isPresent());
        assertEquals("billing-system", found.get().sourceSystem());
        assertEquals("EMAIL", found.get().channels().getFirst());
        assertEquals(1, repository.findAll().size());

        StoredNotification updated = new StoredNotification(
                "db-1",
                "billing-system",
                "corr-db-1",
                "ALERT",
                "ERROR",
                "HIGH",
                List.of("ops@example.com"),
                List.of("EMAIL", "SMS"),
                LocalDateTime.now(),
                null,
                null,
                "DB title",
                "DB message",
                "SENT",
                List.of(new DeliveryAttemptResponse("EMAIL", 1, "SENT", "SMTP", "ok"), new DeliveryAttemptResponse("SMS", 2, "SENT", "TWILIO", "ok")),
                LocalDateTime.now()
        );
        repository.update(updated);

        assertEquals("SENT", repository.findById("db-1").orElseThrow().status());
        assertEquals(2, repository.findById("db-1").orElseThrow().deliveryAttempts().size());
    }

    @Test
    void serviceUsesRepositoryWhenPresent() {
        NotificationService service = new NotificationService(new NotificationRoutingPolicy(), repository);
        NotificationRequest request = new NotificationRequest(
                "repo-notif-1",
                "billing-system",
                "corr-123",
                "ALERT",
                "ERROR",
                "HIGH",
                List.of("ops@example.com"),
                List.of("EMAIL", "SMS"),
                LocalDateTime.now(),
                null,
                null,
                "Repository title",
                "Repository message"
        );

        var response = service.createNotification(request);
        assertNotNull(response.id());
        assertEquals("QUEUED", response.status());
        assertEquals(1, service.getNotifications().size());
        assertNotNull(service.getNotificationStatus(response.id()).overallStatus());
    }
}
