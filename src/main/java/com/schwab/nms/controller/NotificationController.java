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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class NotificationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationController.class);

    private final NotificationHandler notificationHandler;

    public NotificationController(NotificationHandler notificationHandler) {
        LOGGER.info("Enter: NotificationController constructor");
        try {
            this.notificationHandler = notificationHandler;
            LOGGER.info("Exit: NotificationController constructor");
        } catch (Exception e) {
            LOGGER.error("Error in NotificationController constructor", e);
            throw e;
        }
    }

    @PostMapping("/notifications")
    public ResponseEntity<NotificationResponse> createNotification(@Valid @RequestBody NotificationRequest request) {
        LOGGER.info("Enter: createNotification (controller)");
        try {
            NotificationResponse response = notificationHandler.createNotification(request);
            LOGGER.info("Exit: createNotification (controller)");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            LOGGER.error("Error in createNotification (controller)", e);
            throw e;
        }
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<NotificationResponse>> getNotifications() {
        LOGGER.info("Enter: getNotifications (controller)");
        try {
            List<NotificationResponse> result = notificationHandler.getNotifications();
            LOGGER.info("Exit: getNotifications (controller)");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            LOGGER.error("Error in getNotifications (controller)", e);
            throw e;
        }
    }

    @GetMapping("/notifications/{id}")
    public ResponseEntity<NotificationResponse> getNotificationById(@PathVariable String id) {
        LOGGER.info("Enter: getNotificationById (controller)");
        try {
            NotificationResponse result = notificationHandler.getNotificationById(id);
            LOGGER.info("Exit: getNotificationById (controller)");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            LOGGER.error("Error in getNotificationById (controller)", e);
            throw e;
        }
    }

    @GetMapping("/notifications/{id}/status")
    public ResponseEntity<NotificationStatusResponse> getNotificationStatus(@PathVariable String id) {
        LOGGER.info("Enter: getNotificationStatus (controller)");
        try {
            NotificationStatusResponse result = notificationHandler.getNotificationStatus(id);
            LOGGER.info("Exit: getNotificationStatus (controller)");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            LOGGER.error("Error in getNotificationStatus (controller)", e);
            throw e;
        }
    }
}
