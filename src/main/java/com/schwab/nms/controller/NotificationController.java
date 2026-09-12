package com.schwab.nms.controller;

import com.schwab.nms.model.NotificationRequest;
import com.schwab.nms.model.NotificationResponse;
import com.schwab.nms.model.NotificationStatusResponse;
import com.schwab.nms.handler.NotificationHandler;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class NotificationController {

    private final NotificationHandler notificationHandler;

    public NotificationController(NotificationHandler notificationHandler) {
        this.notificationHandler = notificationHandler;
    }

    @PostMapping("/notifications")
    public ResponseEntity<NotificationResponse> createNotification(@Valid @RequestBody NotificationRequest request) {
        NotificationResponse response = notificationHandler.createNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<NotificationResponse>> getNotifications() {
        return ResponseEntity.ok(notificationHandler.getNotifications());
    }

    @GetMapping("/notifications/{id}")
    public ResponseEntity<NotificationResponse> getNotificationById(@PathVariable String id) {
        return ResponseEntity.ok(notificationHandler.getNotificationById(id));
    }

    @GetMapping("/notifications/{id}/status")
    public ResponseEntity<NotificationStatusResponse> getNotificationStatus(@PathVariable String id) {
        return ResponseEntity.ok(notificationHandler.getNotificationStatus(id));
    }
}
