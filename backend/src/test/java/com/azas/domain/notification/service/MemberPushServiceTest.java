package com.azas.domain.notification.service;

import com.azas.domain.notification.dto.ActivePushDeviceRow;
import com.azas.domain.notification.dto.MemberPushDeliveryResult;
import com.azas.domain.notification.mapper.PushDeviceMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberPushServiceTest {

    private static final Long MEMBER_ID = 7L;

    @Mock
    private PushDeviceMapper pushDeviceMapper;

    @Mock
    private PushTokenProtector pushTokenProtector;

    @Mock
    private PushMessageSender pushMessageSender;

    private MemberPushService memberPushService;

    @BeforeEach
    void setUp() {
        memberPushService = new MemberPushService(
                pushDeviceMapper,
                pushTokenProtector,
                pushMessageSender
        );
    }

    @Test
    void sendsPushMessageToEveryActiveDevice() {
        byte[] firstCiphertext = new byte[]{1};
        byte[] secondCiphertext = new byte[]{2};
        PushMessage pushMessage = pushMessage();

        when(pushDeviceMapper.findActiveByMemberId(MEMBER_ID))
                .thenReturn(List.of(
                        new ActivePushDeviceRow(
                                31L,
                                firstCiphertext
                        ),
                        new ActivePushDeviceRow(
                                32L,
                                secondCiphertext
                        )
                ));
        when(pushTokenProtector.decrypt(firstCiphertext))
                .thenReturn("first-token");
        when(pushTokenProtector.decrypt(secondCiphertext))
                .thenReturn("second-token");
        when(pushMessageSender.send(
                "first-token",
                pushMessage
        )).thenReturn("message-1");
        when(pushMessageSender.send(
                "second-token",
                pushMessage
        )).thenReturn("message-2");

        MemberPushDeliveryResult result =
                memberPushService.sendToMember(
                        MEMBER_ID,
                        pushMessage
                );

        assertEquals(2, result.getAttemptedCount());
        assertEquals(2, result.getSuccessCount());
        assertEquals(0, result.getFailureCount());
        assertEquals(0, result.getInvalidatedDeviceCount());
    }

    @Test
    void deactivatesDeviceWhenFcmReportsInvalidToken() {
        byte[] ciphertext = new byte[]{3};
        PushMessage pushMessage = pushMessage();

        when(pushDeviceMapper.findActiveByMemberId(MEMBER_ID))
                .thenReturn(List.of(
                        new ActivePushDeviceRow(
                                33L,
                                ciphertext
                        )
                ));
        when(pushTokenProtector.decrypt(ciphertext))
                .thenReturn("expired-token");
        when(pushMessageSender.send(
                "expired-token",
                pushMessage
        )).thenThrow(new PushDeliveryException(
                "invalid token",
                true,
                false,
                null
        ));
        when(pushDeviceMapper.deactivateById(33L))
                .thenReturn(1);

        MemberPushDeliveryResult result =
                memberPushService.sendToMember(
                        MEMBER_ID,
                        pushMessage
                );

        assertEquals(1, result.getAttemptedCount());
        assertEquals(0, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
        assertEquals(1, result.getInvalidatedDeviceCount());
        verify(pushDeviceMapper).deactivateById(33L);
    }

    @Test
    void deactivatesDeviceWhenStoredCiphertextIsInvalid() {
        byte[] ciphertext = new byte[]{4};

        when(pushDeviceMapper.findActiveByMemberId(MEMBER_ID))
                .thenReturn(List.of(
                        new ActivePushDeviceRow(
                                34L,
                                ciphertext
                        )
                ));
        when(pushTokenProtector.decrypt(ciphertext))
                .thenThrow(new IllegalArgumentException(
                        "invalid ciphertext"
                ));
        when(pushDeviceMapper.deactivateById(34L))
                .thenReturn(1);

        MemberPushDeliveryResult result =
                memberPushService.sendToMember(
                        MEMBER_ID,
                        pushMessage()
                );

        assertEquals(1, result.getFailureCount());
        assertEquals(1, result.getInvalidatedDeviceCount());
        verify(pushDeviceMapper).deactivateById(34L);
    }

    @Test
    void doesNotDeactivateDeviceForMessageConstructionFailure() {
        byte[] ciphertext = new byte[]{5};
        PushMessage pushMessage = pushMessage();

        when(pushDeviceMapper.findActiveByMemberId(MEMBER_ID))
                .thenReturn(List.of(
                        new ActivePushDeviceRow(
                                35L,
                                ciphertext
                        )
                ));
        when(pushTokenProtector.decrypt(ciphertext))
                .thenReturn("valid-token");
        when(pushMessageSender.send(
                "valid-token",
                pushMessage
        )).thenThrow(new IllegalArgumentException(
                "invalid message"
        ));

        MemberPushDeliveryResult result =
                memberPushService.sendToMember(
                        MEMBER_ID,
                        pushMessage
                );

        assertEquals(1, result.getFailureCount());
        assertEquals(0, result.getInvalidatedDeviceCount());
        verify(
                pushDeviceMapper,
                never()
        ).deactivateById(35L);
    }

    private PushMessage pushMessage() {
        return new PushMessage(
                "용돈 요청이 도착했어요",
                "깨비가 용돈 5,000원을 요청했어요.",
                "/allowance/requests/10",
                Map.of("notification_id", "100")
        );
    }
}
