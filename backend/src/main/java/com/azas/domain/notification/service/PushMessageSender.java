package com.azas.domain.notification.service;

public interface PushMessageSender {

    String send(
            String pushToken,
            PushMessage pushMessage
    );
}
