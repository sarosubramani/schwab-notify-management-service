package com.schwab.nms.service;

import com.schwab.nms.enums.NotificationChannel;
import com.schwab.nms.enums.NotificationPriority;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Component
public class NotificationRoutingPolicy {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationRoutingPolicy.class);

    public List<String> resolveChannels(List<String> requestedChannels, String priority) {
        LOGGER.info("Enter: resolveChannels");
        try {
            NotificationPriority parsedPriority = NotificationPriority.fromValue(priority);

            if (requestedChannels != null && !requestedChannels.isEmpty()) {
                List<String> resolved = new ArrayList<>();
                for (String channel : requestedChannels) {
                    if (channel == null || channel.isBlank()) {
                        continue;
                    }

                    resolved.add(NotificationChannel.fromValue(channel).name());
                }
                if (!resolved.isEmpty()) {
                    return resolved;
                }
            }
            return defaultChannelsForPriority(parsedPriority);
        } catch (Exception e) {
            LOGGER.error("Error in resolveChannels", e);
            throw e;
        }
    }

    private List<String> defaultChannelsForPriority(NotificationPriority priority) {
        return switch (priority) {
            case LOW -> List.of(NotificationChannel.IN_APP.name());
            case MEDIUM -> List.of(NotificationChannel.EMAIL.name());
            case HIGH -> List.of(NotificationChannel.EMAIL.name(), NotificationChannel.SMS.name());
            case CRITICAL -> List.of(NotificationChannel.EMAIL.name(), NotificationChannel.SMS.name(), NotificationChannel.PUSH.name(), NotificationChannel.TEAMS.name());
        };
    }
}
