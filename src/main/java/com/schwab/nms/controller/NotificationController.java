package com.schwab.nms.controller;

import com.schwab.nms.model.NotificationRequest;
import com.schwab.nms.model.NotificationResponse;
import com.schwab.nms.model.NotificationStatusResponse;
import com.schwab.nms.handler.NotificationHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
            LOGGER.debug("Exit: NotificationController constructor");
        } catch (Exception e) {
            LOGGER.error("Error in NotificationController constructor", e);
            throw e;
        }
    }

    @Operation(
            summary = "Create a notification",
            description = "Creates a new notification and returns the stored notification payload.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Notification created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = NotificationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
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

    @Operation(
            summary = "Get all notifications",
            description = "Retrieves all notifications currently tracked by the service.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notifications retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = NotificationResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/getnotifications")
    public ResponseEntity<List<NotificationResponse>> getNotifications() {
        LOGGER.debug("Enter: getNotifications (controller)");
        try {
            List<NotificationResponse> result = notificationHandler.getNotifications();
            LOGGER.debug("Exit: getNotifications (controller)");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            LOGGER.error("Error in getNotifications (controller)", e);
            throw e;
        }
    }

    @Operation(
            summary = "Get notification by ID",
            description = "Fetches an individual notification using its unique notification ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notification found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = NotificationResponse.class))),
            @ApiResponse(responseCode = "404", description = "Notification not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/notifications/{id}")
    public ResponseEntity<NotificationResponse> getNotificationById(@PathVariable String id) {
        LOGGER.debug("Enter: getNotificationById (controller)");
        try {
            NotificationResponse result = notificationHandler.getNotificationById(id);
            LOGGER.debug("Exit: getNotificationById (controller)");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            LOGGER.error("Error in getNotificationById (controller)", e);
            throw e;
        }
    }

    @Operation(
            summary = "Get notification status by ID",
            description = "Returns the current delivery status for an individual notification.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notification status retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = NotificationStatusResponse.class))),
            @ApiResponse(responseCode = "404", description = "Notification not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/notifications/{id}/status")
    public ResponseEntity<NotificationStatusResponse> getNotificationStatus(@PathVariable String id) {
        LOGGER.debug("Enter: getNotificationStatus (controller)");
        try {
            NotificationStatusResponse result = notificationHandler.getNotificationStatus(id);
            LOGGER.debug("Exit: getNotificationStatus (controller)");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            LOGGER.error("Error in getNotificationStatus (controller)", e);
            throw e;
        }
    }
}
