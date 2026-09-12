package com.schwab.nms.constants;

public final class NmsConstants {

    private NmsConstants() {}

    public static final class Providers {
        public static final String SMTP = "SMTP_PROVIDER";
        public static final String TWILIO = "TWILIO_PROVIDER";
        public static final String FCM = "FCM_PROVIDER";
        public static final String NMS_INBOX = "NMS_INBOX_PROVIDER";
        public static final String SLACK_WEBHOOK = "SLACK_WEBHOOK_PROVIDER";
        public static final String TEAMS_WEBHOOK = "MICROSOFT_TEAMS_WEBHOOK_PROVIDER";

        private Providers() {}
    }

    public static final class Messages {
        public static final String ERR_DUPLICATE = "Duplicate notification request detected";
        public static final String ERR_REQUEST_NULL = "Notification request cannot be null";
        public static final String ATTEMPT_DELIVERED = "Delivered successfully";
        public static final String ATTEMPT_INTERRUPTED = "Delivery interrupted";
        public static final String DEFAULT_PENDING = "PENDING";

        // Validation messages
        public static final String ERR_SOURCE_SYSTEM = "Source system is required";
        public static final String ERR_CORRELATION_ID = "Correlation identifier is required";
        public static final String ERR_NOTIFICATION_TYPE = "Notification type is required";
        public static final String ERR_SEVERITY = "Severity is required";
        public static final String ERR_PRIORITY = "Priority is required";
        public static final String ERR_RECIPIENTS = "At least one recipient is required";
        public static final String ERR_CREATED_AT = "Creation timestamp is required";
        public static final String ERR_TITLE = "Title is required";
        public static final String ERR_MESSAGE = "Message is required";

        private Messages() {}
    }
}
