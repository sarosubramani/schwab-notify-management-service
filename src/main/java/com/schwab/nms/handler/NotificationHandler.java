package com.schwab.nms.handler;

import com.schwab.nms.model.NotificationRequest;
import com.schwab.nms.model.NotificationResponse;
import com.schwab.nms.model.NotificationStatusResponse;
import com.schwab.nms.service.NotificationService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotificationHandler {

    private final NotificationService notificationService;

    public NotificationHandler(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public NotificationResponse createNotification(NotificationRequest request) {
        return notificationService.createNotification(request);
    }

    public NotificationResponse getNotification(String id) {
        return notificationService.getNotification(id);
    }

    public List<NotificationResponse> getNotifications() {
        return notificationService.getNotifications();
    }

    public NotificationStatusResponse getNotificationStatus(String id) {
        return notificationService.getNotificationStatus(id);
    }
}
