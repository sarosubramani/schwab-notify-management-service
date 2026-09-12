package com.schwab.nms.util;

import com.schwab.nms.constants.NmsConstants;
import com.schwab.nms.model.DeliveryAttemptResponse;
import com.schwab.nms.model.StoredNotification;
import com.schwab.nms.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationServiceUtilsTest {

    @Test
    void saveAndLoadNotification_worksWithInMemoryMap() {
        Map<String, StoredNotification> notifications = new ConcurrentHashMap<>();
        StoredNotification notification = new StoredNotification(
                "n-1",
                "billing-system",
                "corr-1",
                "ALERT",
                "ERROR",
                "HIGH",
                List.of("ops@example.com"),
                List.of("EMAIL"),
                LocalDateTime.now(),
                null,
                null,
                "Title",
                "Message",
                "QUEUED",
                List.of(),
                LocalDateTime.now()
        );

        NotificationServiceUtils.saveNotification(notifications, null, notification);

        assertEquals(notification, NotificationServiceUtils.loadNotification(notifications, null, "n-1"));
        assertEquals(List.of(notification), NotificationServiceUtils.loadAllNotifications(notifications, null));
    }

    @Test
    void updateStoredStatus_andAddStoredAttempt_updateExistingNotification() {
        Map<String, StoredNotification> notifications = new ConcurrentHashMap<>();
        StoredNotification notification = new StoredNotification(
                "n-2",
                "billing-system",
                "corr-2",
                "ALERT",
                "ERROR",
                "HIGH",
                List.of("ops@example.com"),
                List.of("EMAIL"),
                LocalDateTime.now(),
                null,
                null,
                "Title",
                "Message",
                "QUEUED",
                List.of(),
                LocalDateTime.now()
        );
        notifications.put("n-2", notification);

        NotificationServiceUtils.updateStoredStatus(notifications, null, "n-2", "PROCESSING");
        NotificationServiceUtils.addStoredAttempt(notifications, null, "n-2",
                new DeliveryAttemptResponse("EMAIL", 1, "SENT", NmsConstants.Providers.SMTP, "ok"));

        StoredNotification updated = notifications.get("n-2");
        assertEquals("PROCESSING", updated.status());
        assertEquals(1, updated.deliveryAttempts().size());
        assertEquals("EMAIL", updated.deliveryAttempts().getFirst().channel());
    }

    @Test
    void saveNotification_withRepository_usesRepositorySaveAndUpdate() {
        NotificationRepository repository = Mockito.mock(NotificationRepository.class);
        StoredNotification existing = new StoredNotification(
                "n-3",
                "billing-system",
                "corr-3",
                "ALERT",
                "ERROR",
                "HIGH",
                List.of("ops@example.com"),
                List.of("EMAIL"),
                LocalDateTime.now(),
                null,
                null,
                "Title",
                "Message",
                "QUEUED",
                List.of(),
                LocalDateTime.now()
        );
        when(repository.findById("n-3")).thenReturn(Optional.of(existing));

        NotificationServiceUtils.saveNotification(new ConcurrentHashMap<>(), repository, existing);
        verify(repository).update(existing);

        StoredNotification newNotification = new StoredNotification(
                "n-4",
                "billing-system",
                "corr-4",
                "ALERT",
                "ERROR",
                "HIGH",
                List.of("ops@example.com"),
                List.of("EMAIL"),
                LocalDateTime.now(),
                null,
                null,
                "Title 2",
                "Message 2",
                "QUEUED",
                List.of(),
                LocalDateTime.now()
        );
        when(repository.findById("n-4")).thenReturn(Optional.empty());
        NotificationServiceUtils.saveNotification(new ConcurrentHashMap<>(), repository, newNotification);
        verify(repository).save(newNotification);
    }

    @Test
    void loadNotification_returnsNullWhenNotPresent() {
        Map<String, StoredNotification> notifications = new ConcurrentHashMap<>();

        assertNull(NotificationServiceUtils.loadNotification(notifications, null, "missing"));
        assertNotNull(NotificationServiceUtils.resolveProvider("EMAIL"));
    }
}
