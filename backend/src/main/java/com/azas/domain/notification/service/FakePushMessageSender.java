package com.azas.domain.notification.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
@Profile("!firebase")
public class FakePushMessageSender implements PushMessageSender {

    private final AtomicLong sequence = new AtomicLong();

    @Override
    public String send(
            String pushToken,
            PushMessage pushMessage
    ) {
        return "fake-fcm-message-"
                + sequence.incrementAndGet();
    }
}
