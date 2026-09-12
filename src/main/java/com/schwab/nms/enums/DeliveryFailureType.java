package com.schwab.nms.enums;

public enum DeliveryFailureType {
    TRANSIENT_PROVIDER_FAILURE(true, "Transient provider failure"),
    PERMANENT_PROVIDER_REJECTION(false, "Permanent provider rejection"),
    INVALID_RECIPIENT(false, "Invalid recipient"),
    RATE_LIMITED(true, "Rate-limit response"),
    TIMEOUT(true, "Timeout"),
    AUTHENTICATION_OR_AUTHORIZATION_FAILURE(false, "Authentication or authorization failure");

    private final boolean retryable;
    private final String description;

    DeliveryFailureType(boolean retryable, String description) {
        this.retryable = retryable;
        this.description = description;
    }

    public boolean isRetryable() {
        return retryable;
    }

    public String getDescription() {
        return description;
    }
}
