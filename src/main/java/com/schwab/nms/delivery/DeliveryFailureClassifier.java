package com.schwab.nms.delivery;

import com.schwab.nms.enums.DeliveryFailureType;
import com.schwab.nms.exception.DownstreamDeliveryException;

import java.net.SocketTimeoutException;
import java.util.Locale;

public final class DeliveryFailureClassifier {
    private DeliveryFailureClassifier() {
    }

    public static DeliveryFailureType classify(Throwable throwable) {
        if (throwable == null) {
            return DeliveryFailureType.TRANSIENT_PROVIDER_FAILURE;
        }

        if (throwable instanceof DownstreamDeliveryException d) {
            return d.getFailureType();
        }

        String message = throwable.getMessage() == null ? "" : throwable.getMessage().toLowerCase(Locale.ROOT);

        if (throwable instanceof SocketTimeoutException || message.contains("timeout") || message.contains("timed out")) {
            return DeliveryFailureType.TIMEOUT;
        }
        if (message.contains("rate limit") || message.contains("429") || message.contains("too many requests")) {
            return DeliveryFailureType.RATE_LIMITED;
        }
        if (message.contains("401") || message.contains("403") || message.contains("unauthorized") || message.contains("forbidden") || message.contains("auth")) {
            return DeliveryFailureType.AUTHENTICATION_OR_AUTHORIZATION_FAILURE;
        }
        if (message.contains("invalid recipient") || message.contains("recipient") && message.contains("invalid") || message.contains("bad address") || message.contains("unknown recipient")) {
            return DeliveryFailureType.INVALID_RECIPIENT;
        }
        if (message.contains("rejected") || message.contains("permanent") || message.contains("unsupported provider") || message.contains("not allowed")) {
            return DeliveryFailureType.PERMANENT_PROVIDER_REJECTION;
        }
        if (message.contains("connection") || message.contains("temporary") || message.contains("unavailable") || message.contains("server error") || message.contains("transient")) {
            return DeliveryFailureType.TRANSIENT_PROVIDER_FAILURE;
        }

        return DeliveryFailureType.TRANSIENT_PROVIDER_FAILURE;
    }

    public static boolean isRetryable(Throwable throwable) {
        return classify(throwable).isRetryable();
    }
}
