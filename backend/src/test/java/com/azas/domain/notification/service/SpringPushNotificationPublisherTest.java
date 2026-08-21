package com.azas.domain.notification.service;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SpringPushNotificationPublisherTest {

    @Test
    void publishesMemberPushRequestAsApplicationEvent() {
        ApplicationEventPublisher applicationEventPublisher =
                mock(ApplicationEventPublisher.class);
        SpringPushNotificationPublisher publisher =
                new SpringPushNotificationPublisher(
                        applicationEventPublisher
                );
        PushMessage message = new PushMessage(
                "미션 알림",
                "새 미션이 도착했어요.",
                "/child/missions",
                Map.of()
        );

        publisher.publish(7L, message);

        verify(applicationEventPublisher).publishEvent(
                any(MemberPushRequestedEvent.class)
        );
    }
}
