package com.azas.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebasePushMessageSenderTest {

    @Mock
    private FirebaseMessaging firebaseMessaging;

    private FirebasePushMessageSender sender;

    @BeforeEach
    void setUp() {
        sender = new FirebasePushMessageSender(
                firebaseMessaging
        );
    }

    @Test
    void sendsMessageThroughFirebaseAdminSdk()
            throws Exception {
        when(firebaseMessaging.send(any(Message.class)))
                .thenReturn("projects/azas/messages/100");

        String messageId = sender.send(
                "fcm-token",
                new PushMessage(
                        "제목",
                        "내용",
                        "/notifications/1",
                        Map.of("notification_id", "1")
                )
        );

        assertEquals(
                "projects/azas/messages/100",
                messageId
        );
        verify(firebaseMessaging).send(any(Message.class));
    }

    @Test
    void classifiesFirebaseMessagingErrorCodes() {
        assertTrue(sender.isInvalidToken(
                MessagingErrorCode.UNREGISTERED
        ));
        assertTrue(sender.isInvalidToken(
                MessagingErrorCode.SENDER_ID_MISMATCH
        ));
        assertFalse(sender.isInvalidToken(
                MessagingErrorCode.UNAVAILABLE
        ));

        assertTrue(sender.isRetryable(
                MessagingErrorCode.INTERNAL
        ));
        assertTrue(sender.isRetryable(
                MessagingErrorCode.UNAVAILABLE
        ));
        assertTrue(sender.isRetryable(
                MessagingErrorCode.QUOTA_EXCEEDED
        ));
        assertFalse(sender.isRetryable(
                MessagingErrorCode.UNREGISTERED
        ));
    }
}
