package com.schwab.nms.delivery;

import com.schwab.nms.enums.DeliveryFailureType;
import com.schwab.nms.exception.DownstreamDeliveryException;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeliveryFailureClassifierTest {

    @Test
    void retryableFailuresAreMarkedForRetry() {
        assertTrue(DeliveryFailureClassifier.isRetryable(new DownstreamDeliveryException(DeliveryFailureType.TRANSIENT_PROVIDER_FAILURE, "transient failure")));
        assertTrue(DeliveryFailureClassifier.isRetryable(new DownstreamDeliveryException(DeliveryFailureType.RATE_LIMITED, "rate limited")));
        assertTrue(DeliveryFailureClassifier.isRetryable(new DownstreamDeliveryException(DeliveryFailureType.TIMEOUT, "timeout")));
        assertFalse(DeliveryFailureClassifier.isRetryable(new DownstreamDeliveryException(DeliveryFailureType.INVALID_RECIPIENT, "invalid recipient")));
        assertFalse(DeliveryFailureClassifier.isRetryable(new DownstreamDeliveryException(DeliveryFailureType.PERMANENT_PROVIDER_REJECTION, "rejected")));
        assertFalse(DeliveryFailureClassifier.isRetryable(new DownstreamDeliveryException(DeliveryFailureType.AUTHENTICATION_OR_AUTHORIZATION_FAILURE, "forbidden")));
    }

    @Test
    void retryOnlyRetriesRetryableFailures() {
        AtomicInteger attempts = new AtomicInteger();
        Retry retry = Retry.of("delivery-retry-test", RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(1))
                .retryOnException(DeliveryFailureClassifier::isRetryable)
                .build());

        assertThrows(DownstreamDeliveryException.class, () -> Retry.decorateRunnable(retry, () -> {
            int current = attempts.incrementAndGet();
            if (current < 3) {
                throw new DownstreamDeliveryException(DeliveryFailureType.TRANSIENT_PROVIDER_FAILURE, "transient");
            }
            throw new DownstreamDeliveryException(DeliveryFailureType.INVALID_RECIPIENT, "invalid");
        }).run());

        assertTrue(attempts.get() >= 3);
    }
}
