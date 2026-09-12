package com.schwab.nms.handler;

import com.schwab.nms.model.NotificationRequest;
import com.schwab.nms.model.NotificationResponse;
import com.schwab.nms.model.NotificationStatusResponse;
import com.schwab.nms.service.NotificationService;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Component
public class NotificationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationHandler.class);

    private final NotificationService notificationService;

    public NotificationHandler(NotificationService notificationService) {
        LOGGER.debug("Enter: NotificationHandler constructor");
        try {
            this.notificationService = notificationService;
            LOGGER.debug("Exit: NotificationHandler constructor");
        } catch (Exception e) {
            LOGGER.error("Error in NotificationHandler constructor", e);
            throw e;
        }
    }

    public NotificationResponse createNotification(NotificationRequest request) {
        LOGGER.info("Enter: createNotification (handler)");
        try {
            NotificationResponse result = notificationService.createNotification(request);
            LOGGER.info("Exit: createNotification (handler)");
            return result;
        } catch (Exception e) {
            LOGGER.error("Error in createNotification (handler)", e);
            throw e;
        }
    }

    public NotificationResponse getNotificationById(String id) {
        LOGGER.debug("Enter: getNotificationById (handler)");
        try {
            NotificationResponse result = notificationService.getNotificationById(id);
            LOGGER.debug("Exit: getNotificationById (handler)");
            return result;
        } catch (Exception e) {
            LOGGER.error("Error in getNotificationById (handler)", e);
            throw e;
        }
    }

    public NotificationStatusResponse getNotificationStatus(String id) {
        LOGGER.debug("Enter: getNotificationStatus (handler)");
        try {
            NotificationStatusResponse result = notificationService.getNotificationStatus(id);
            LOGGER.debug("Exit: getNotificationStatus (handler)");
            return result;
        } catch (Exception e) {
            LOGGER.error("Error in getNotificationStatus (handler)", e);
            throw e;
        }
    }

    public List<NotificationResponse> getNotifications() {
        LOGGER.debug("Enter: getNotifications (handler)");
        try {
            List<NotificationResponse> result = notificationService.getNotifications();
            LOGGER.debug("Exit: getNotifications (handler)");
            return result;
        } catch (Exception e) {
            LOGGER.error("Error in getNotifications (handler)", e);
            throw e;
        }
    }
}
