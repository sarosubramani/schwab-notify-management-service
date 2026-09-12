package com.schwab.nms.controller;

import com.schwab.nms.handler.NotificationHandler;
import com.schwab.nms.model.NotificationRequest;
import com.schwab.nms.model.NotificationResponse;
import com.schwab.nms.model.NotificationStatusResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class NotificationControllerUnitTest {

    @Test
    void createNotificationReturnsCreatedResponse() {
        NotificationHandler handler = Mockito.mock(NotificationHandler.class);
        NotificationController controller = new NotificationController(handler);
        NotificationRequest request = validRequest();
        NotificationResponse response = validResponse();

        when(handler.createNotification(request)).thenReturn(response);

        ResponseEntity<NotificationResponse> result = controller.createNotification(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals("n-1", result.getBody().id());
    }

    @Test
    void getNotificationsReturnsOkResponse() {
        NotificationHandler handler = Mockito.mock(NotificationHandler.class);
        NotificationController controller = new NotificationController(handler);
        NotificationResponse response = validResponse();

        when(handler.getNotifications()).thenReturn(List.of(response));

        ResponseEntity<List<NotificationResponse>> result = controller.getNotifications();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
    }

    @Test
    void getNotificationByIdReturnsOkResponse() {
        NotificationHandler handler = Mockito.mock(NotificationHandler.class);
        NotificationController controller = new NotificationController(handler);
        NotificationResponse response = validResponse();

        when(handler.getNotificationById("n-1")).thenReturn(response);

        ResponseEntity<NotificationResponse> result = controller.getNotificationById("n-1");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("n-1", result.getBody().id());
    }

    @Test
    void getNotificationStatusReturnsOkResponse() {
        NotificationHandler handler = Mockito.mock(NotificationHandler.class);
        NotificationController controller = new NotificationController(handler);
        NotificationStatusResponse status = new NotificationStatusResponse(
                "n-1",
                "n-1",
                "QUEUED",
                List.of("EMAIL"),
                List.of(new NotificationStatusResponse.RecipientDeliveryStatus("user@example.com", "EMAIL", "QUEUED")),
                LocalDateTime.now(),
                null,
                null,
                LocalDateTime.now()
        );

        when(handler.getNotificationStatus("n-1")).thenReturn(status);

        ResponseEntity<NotificationStatusResponse> result = controller.getNotificationStatus("n-1");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("QUEUED", result.getBody().overallStatus());
    }

    @Test
    void controllerReThrowsExceptionsFromHandler() {
        NotificationHandler handler = Mockito.mock(NotificationHandler.class);
        NotificationController controller = new NotificationController(handler);
        when(handler.getNotificationById("missing")).thenThrow(new RuntimeException("boom"));

        assertThrows(RuntimeException.class, () -> controller.getNotificationById("missing"));
    }

    private NotificationRequest validRequest() {
        return new NotificationRequest(
                "n-1",
                "billing-system",
                "corr-1",
                "ALERT",
                "ERROR",
                "HIGH",
                List.of("user@example.com"),
                List.of("EMAIL", "SMS"),
                LocalDateTime.now(),
                null,
                null,
                "Title",
                "Message"
        );
    }

    private NotificationResponse validResponse() {
        return new NotificationResponse(
                "n-1",
                "n-1",
                "billing-system",
                "corr-1",
                "ALERT",
                "ERROR",
                "HIGH",
                List.of("user@example.com"),
                List.of("EMAIL", "SMS"),
                LocalDateTime.now(),
                null,
                null,
                "Title",
                "Message",
                "QUEUED",
                List.of()
        );
    }
}
