package com.schwab.nms.util;

import com.schwab.nms.model.NotificationRequest;
import com.schwab.nms.model.StoredNotification;
import com.schwab.nms.service.NotificationRoutingPolicy;
import org.apache.commons.lang3.StringUtils;
    import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class NmsUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(NmsUtils.class);

    private NmsUtils() {}

    public static List<String> normalizeList(List<String> items) {
        try {
            if (items == null || items.isEmpty()) {
                return List.of();
            }
            List<String> normalized = new ArrayList<>();
            for (String it : items) {
                if (StringUtils.isNotBlank(it)) {
                    String trimmed = it.trim();
                    if (normalized.stream().noneMatch(existing -> existing.equalsIgnoreCase(trimmed))) {
                        normalized.add(trimmed);
                    }
                }
            }
            return normalized;
        } catch (Exception e) {
            LOGGER.error("Error in normalizeList", e);
            throw e;
        }
    }

    public static String buildFingerprint(NotificationRequest request, NotificationRoutingPolicy routingPolicy) {
        try {
            List<String> channels = routingPolicy.resolveChannels(normalizeList(request.channels()), request.priority());
            String channelPart = channels.stream().sorted().collect(Collectors.joining(","));
            String recipientsPart = String.join(",", normalizeList(request.recipients()).stream().sorted().collect(Collectors.joining(",")));
        return String.join("|",
                    request.sourceSystem().trim(),
                    request.correlationId().trim(),
                    request.notificationType().trim(),
                    request.severity().trim(),
                    request.priority().trim(),
                    recipientsPart,
                    channelPart,
                    request.title().trim(),
                    request.message().trim());
        } catch (Exception e) {
            LOGGER.error("Error in buildFingerprint (request, routingPolicy)", e);
            throw e;
        }
    }

    public static String buildFingerprint(StoredNotification notification) {
        try {
            String channelPart = notification.channels().stream().sorted().collect(Collectors.joining(","));
            String recipientsPart = String.join(",", notification.recipients().stream().sorted().collect(Collectors.joining(",")));
            return String.join("|",
                    notification.sourceSystem().trim(),
                    notification.correlationId().trim(),
                    notification.notificationType().trim(),
                    notification.severity().trim(),
                    notification.priority().trim(),
                    recipientsPart,
                    channelPart,
                    notification.title().trim(),
                    notification.message().trim());
        } catch (Exception e) {
            LOGGER.error("Error in buildFingerprint (notification)", e);
            throw e;
        }
    }
}
