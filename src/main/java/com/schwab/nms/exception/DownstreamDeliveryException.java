package com.schwab.nms.exception;

import com.schwab.nms.enums.DeliveryFailureType;

public class DownstreamDeliveryException extends RuntimeException {
    private final DeliveryFailureType failureType;

    public DownstreamDeliveryException(DeliveryFailureType failureType, String message) {
        super(message);
        this.failureType = failureType;
    }

    public DownstreamDeliveryException(DeliveryFailureType failureType, String message, Throwable cause) {
        super(message, cause);
        this.failureType = failureType;
    }

    public DeliveryFailureType getFailureType() {
        return failureType;
    }
}
