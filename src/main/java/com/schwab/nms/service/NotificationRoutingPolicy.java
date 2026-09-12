package com.schwab.nms.service;

import com.schwab.nms.enums.NotificationChannel;
import com.schwab.nms.enums.NotificationPriority;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class NotificationRoutingPolicy {

    public List<String> resolveChannels(List<String> requestedChannels, String priority) {
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
