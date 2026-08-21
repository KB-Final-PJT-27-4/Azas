package com.azas.domain.notification.service;

import lombok.Getter;

@Getter
public class PushDeliveryException extends RuntimeException {

    private final boolean invalidToken;
    private final boolean retryable;

    public PushDeliveryException(
            String message,
            boolean invalidToken,
            boolean retryable,
            Throwable cause
    ) {
        super(message, cause);
        this.invalidToken = invalidToken;
        this.retryable = retryable;
    }
}
