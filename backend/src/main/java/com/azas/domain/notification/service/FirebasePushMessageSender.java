package com.azas.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.WebpushConfig;
import com.google.firebase.messaging.WebpushFcmOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("firebase")
@RequiredArgsConstructor
public class FirebasePushMessageSender
        implements PushMessageSender {

    private final FirebaseMessaging firebaseMessaging;

    @Override
    @SuppressWarnings("deprecation")
    public String send(
            String pushToken,
            PushMessage pushMessage
    ) {
        Message.Builder messageBuilder = Message.builder()
                .setToken(pushToken)
                .setNotification(
                        Notification.builder()
                                .setTitle(pushMessage.getTitle())
                                .setBody(pushMessage.getBody())
                                .build()
                )
                .putAllData(pushMessage.getData());

        if (pushMessage.getActionUrl() != null) {
            messageBuilder.putData(
                    "action_url",
                    pushMessage.getActionUrl()
            );
        }

        if (isHttpsLink(pushMessage.getActionUrl())) {
            messageBuilder.setWebpushConfig(
                    WebpushConfig.builder()
                            .setFcmOptions(
                                    WebpushFcmOptions.withLink(
                                            pushMessage.getActionUrl()
                                    )
                            )
                            .build()
            );
        }

        try {
            return firebaseMessaging.send(
                    messageBuilder.build()
            );
        } catch (FirebaseMessagingException exception) {
            MessagingErrorCode errorCode =
                    exception.getMessagingErrorCode();

            throw new PushDeliveryException(
                    "FCM 푸시 메시지를 발송하지 못했습니다.",
                    isInvalidToken(errorCode),
                    isRetryable(errorCode),
                    exception
            );
        }
    }

    boolean isInvalidToken(
            MessagingErrorCode errorCode
    ) {
        return errorCode == MessagingErrorCode.UNREGISTERED
                || errorCode
                == MessagingErrorCode.SENDER_ID_MISMATCH;
    }

    boolean isRetryable(
            MessagingErrorCode errorCode
    ) {
        return errorCode == MessagingErrorCode.INTERNAL
                || errorCode == MessagingErrorCode.UNAVAILABLE
                || errorCode
                == MessagingErrorCode.QUOTA_EXCEEDED;
    }

    private boolean isHttpsLink(String actionUrl) {
        return actionUrl != null
                && actionUrl.startsWith("https://");
    }
}
