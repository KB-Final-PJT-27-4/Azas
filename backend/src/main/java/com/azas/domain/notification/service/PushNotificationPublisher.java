package com.azas.domain.notification.service;

public interface PushNotificationPublisher {

    void publish(
            Long memberId,
            PushMessage pushMessage
    );
}
