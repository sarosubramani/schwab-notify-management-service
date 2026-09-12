package com.schwab.nms.validator;

import com.schwab.nms.constants.NmsConstants;
import com.schwab.nms.enums.NotificationChannel;
import com.schwab.nms.enums.NotificationPriority;
import com.schwab.nms.enums.NotificationSeverity;
import com.schwab.nms.enums.NotificationType;
import com.schwab.nms.model.NotificationRequest;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.schwab.nms.util.NmsUtils.normalizeList;

public class RequestValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestValidator.class);
    public void createNotifyValidateRequest(NotificationRequest request) {
        LOGGER.debug("Enter: createNotifyValidateRequest");
        try {
            if (StringUtils.isBlank(request.sourceSystem())) {
                throw new IllegalArgumentException(NmsConstants.Messages.ERR_SOURCE_SYSTEM);
            }
            if (StringUtils.isBlank(request.notificationType())) {
                throw new IllegalArgumentException(NmsConstants.Messages.ERR_NOTIFICATION_TYPE);
            }
            if (StringUtils.isBlank(request.severity())) {
                throw new IllegalArgumentException(NmsConstants.Messages.ERR_SEVERITY);
            }
            if (StringUtils.isBlank(request.priority())) {
                throw new IllegalArgumentException(NmsConstants.Messages.ERR_PRIORITY);
            }
            if (normalizeList(request.recipients()).isEmpty()) {
                throw new IllegalArgumentException(NmsConstants.Messages.ERR_RECIPIENTS);
            }
            if (ObjectUtils.isEmpty(request.createdAt())) {
                throw new IllegalArgumentException(NmsConstants.Messages.ERR_CREATED_AT);
            }
            if (StringUtils.isBlank(request.title())) {
                throw new IllegalArgumentException(NmsConstants.Messages.ERR_TITLE);
            }
            if (StringUtils.isBlank(request.message())) {
                throw new IllegalArgumentException(NmsConstants.Messages.ERR_MESSAGE);
            }

            for (String channel : normalizeList(request.channels())) {
                NotificationChannel.fromValue(channel);
            }

            NotificationPriority.fromValue(request.priority());
            NotificationSeverity.fromValue(request.severity());
            NotificationType.fromValue(request.notificationType());
            LOGGER.debug("Exit: createNotifyValidateRequest");
        } catch (Exception e) {
            LOGGER.error("Error in createNotifyValidateRequest", e);
            throw e;
        }
    }
}
