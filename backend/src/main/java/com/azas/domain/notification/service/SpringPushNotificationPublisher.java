package com.azas.domain.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringPushNotificationPublisher
        implements PushNotificationPublisher {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void publish(
            Long memberId,
            PushMessage pushMessage
    ) {
        if (memberId == null || pushMessage == null) {
            return;
        }

        eventPublisher.publishEvent(
                new MemberPushRequestedEvent(
                        memberId,
                        pushMessage
                )
        );
    }
}
