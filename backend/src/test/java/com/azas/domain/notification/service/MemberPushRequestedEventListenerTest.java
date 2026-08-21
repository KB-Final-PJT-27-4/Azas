package com.azas.domain.notification.service;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MemberPushRequestedEventListenerTest {

    @Test
    void delegatesCommittedEventToMemberPushService() {
        MemberPushService memberPushService =
                mock(MemberPushService.class);
        MemberPushRequestedEventListener listener =
                new MemberPushRequestedEventListener(
                        memberPushService
                );
        PushMessage message = message();

        listener.handle(
                new MemberPushRequestedEvent(7L, message)
        );

        verify(memberPushService).sendToMember(7L, message);
    }

    @Test
    void isolatesDeliveryFailureFromCommittedBusinessFlow() {
        MemberPushService memberPushService =
                mock(MemberPushService.class);
        MemberPushRequestedEventListener listener =
                new MemberPushRequestedEventListener(
                        memberPushService
                );
        PushMessage message = message();

        when(memberPushService.sendToMember(7L, message))
                .thenThrow(new IllegalStateException(
                        "Firebase unavailable"
                ));

        assertDoesNotThrow(() -> listener.handle(
                new MemberPushRequestedEvent(7L, message)
        ));
    }

    private PushMessage message() {
        return new PushMessage(
                "미션 알림",
                "새로운 미션이 도착했어요.",
                "/child/missions",
                Map.of("notification_type", "MISSION_ASSIGNED")
        );
    }
}
