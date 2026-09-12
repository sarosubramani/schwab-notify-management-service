package com.schwab.nms.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.schwab.nms.model.DeliveryAttemptResponse;
import com.schwab.nms.model.StoredNotification;
    import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class NotificationRepository {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NotificationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(StoredNotification notification) {
        jdbcTemplate.update(
                "INSERT INTO notifications (id, source_system, correlation_id, notification_type, severity, priority, recipients, channels, created_at, scheduled_at, expires_at, title, message, status, delivery_attempts, received_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                notification.id(),
                notification.sourceSystem(),
                notification.correlationId(),
                notification.notificationType(),
                notification.severity(),
                notification.priority(),
                toJson(notification.recipients()),
                toJson(notification.channels()),
                notification.createdAt(),
                notification.scheduledAt(),
                notification.expiresAt(),
                notification.title(),
                notification.message(),
                notification.status(),
                toJson(notification.deliveryAttempts()),
                notification.receivedAt()
        );
    }

    public void update(StoredNotification notification) {
        jdbcTemplate.update(
                "UPDATE notifications SET source_system = ?, correlation_id = ?, notification_type = ?, severity = ?, priority = ?, recipients = ?, channels = ?, created_at = ?, scheduled_at = ?, expires_at = ?, title = ?, message = ?, status = ?, delivery_attempts = ?, received_at = ? WHERE id = ?",
                notification.sourceSystem(),
                notification.correlationId(),
                notification.notificationType(),
                notification.severity(),
                notification.priority(),
                toJson(notification.recipients()),
                toJson(notification.channels()),
                notification.createdAt(),
                notification.scheduledAt(),
                notification.expiresAt(),
                notification.title(),
                notification.message(),
                notification.status(),
                toJson(notification.deliveryAttempts()),
                notification.receivedAt(),
                notification.id()
        );
    }

    public Optional<StoredNotification> findById(String id) {
        String sql = "SELECT * FROM notifications WHERE id = ?";
        List<StoredNotification> results = jdbcTemplate.query(sql, notificationRowMapper(), id);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
    }

    public List<StoredNotification> findAll() {
        String sql = "SELECT * FROM notifications ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, notificationRowMapper());
    }

    private RowMapper<StoredNotification> notificationRowMapper() {
        return (rs, rowNum) -> new StoredNotification(
                rs.getString("id"),
                rs.getString("source_system"),
                rs.getString("correlation_id"),
                rs.getString("notification_type"),
                rs.getString("severity"),
                rs.getString("priority"),
                readStringList(rs.getString("recipients")),
                readStringList(rs.getString("channels")),
                rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null,
                rs.getTimestamp("scheduled_at") != null ? rs.getTimestamp("scheduled_at").toLocalDateTime() : null,
                rs.getTimestamp("expires_at") != null ? rs.getTimestamp("expires_at").toLocalDateTime() : null,
                rs.getString("title"),
                rs.getString("message"),
                rs.getString("status"),
                readAttemptList(rs.getString("delivery_attempts")),
                rs.getTimestamp("received_at") != null ? rs.getTimestamp("received_at").toLocalDateTime() : null
        );
    }

    private String toJson(Object value) {
        try {
            return value == null ? "[]" : objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to serialize notification data", e);
        }
    }

    private List<String> readStringList(String json) {
        if (json == null || json.isBlank() || "null".equalsIgnoreCase(json)) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            throw new IllegalStateException("Unable to deserialize recipients/channels", e);
        }
    }

    private List<DeliveryAttemptResponse> readAttemptList(String json) {
        if (json == null || json.isBlank() || "null".equalsIgnoreCase(json)) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            throw new IllegalStateException("Unable to deserialize delivery attempts", e);
        }
    }
}
