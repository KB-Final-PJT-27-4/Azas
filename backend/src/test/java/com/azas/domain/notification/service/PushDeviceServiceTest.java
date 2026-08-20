package com.azas.domain.notification.service;

import com.azas.domain.auth.service.TokenHashEncoder;
import com.azas.domain.notification.dto.PushDeviceCommand;
import com.azas.domain.notification.dto.PushDeviceRegistrationResult;
import com.azas.domain.notification.dto.PushDeviceRow;
import com.azas.domain.notification.dto.RegisterPushDeviceRequest;
import com.azas.domain.notification.entity.PushPlatform;
import com.azas.domain.notification.entity.PushProvider;
import com.azas.domain.notification.mapper.PushDeviceMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PushDeviceServiceTest {

    private static final Long MEMBER_ID = 7L;
    private static final String DEVICE_KEY = "web-installation-uuid";
    private static final String PUSH_TOKEN = "fcm-registration-token";

    @Mock
    private PushDeviceMapper pushDeviceMapper;

    @Mock
    private PushTokenProtector pushTokenProtector;

    private TokenHashEncoder tokenHashEncoder;
    private PushDeviceService pushDeviceService;

    @BeforeEach
    void setUp() {
        tokenHashEncoder = new TokenHashEncoder();
        pushDeviceService = new PushDeviceService(
                pushDeviceMapper,
                pushTokenProtector,
                tokenHashEncoder
        );
    }

    @Test
    void registersNewPushDevice() {
        RegisterPushDeviceRequest request = request();
        byte[] ciphertext = new byte[]{1, 2, 3};
        String tokenHash = tokenHashEncoder.encode(PUSH_TOKEN);

        when(pushDeviceMapper.findByMemberAndDeviceKey(
                MEMBER_ID,
                DEVICE_KEY
        )).thenReturn(null, savedRow());
        when(pushTokenProtector.encrypt(PUSH_TOKEN))
                .thenReturn(ciphertext);
        when(pushDeviceMapper.upsert(
                org.mockito.ArgumentMatchers.any()
        )).thenReturn(1);

        PushDeviceRegistrationResult result =
                pushDeviceService.register(MEMBER_ID, request);

        assertTrue(result.isCreated());
        assertEquals(31L, result.getResponse().getPushDeviceId());
        assertTrue(result.getResponse().isActive());

        verify(pushDeviceMapper)
                .deactivateByTokenHashExceptDevice(
                        tokenHash,
                        MEMBER_ID,
                        DEVICE_KEY
                );

        ArgumentCaptor<PushDeviceCommand> commandCaptor =
                ArgumentCaptor.forClass(PushDeviceCommand.class);
        verify(pushDeviceMapper).upsert(commandCaptor.capture());

        PushDeviceCommand command = commandCaptor.getValue();
        assertEquals(tokenHash, command.getTokenHash());
        assertArrayEquals(ciphertext, command.getTokenCiphertext());
        assertEquals("MacBook Air", command.getDeviceName());
    }

    @Test
    void updatesExistingDeviceInsteadOfCreatingDuplicate() {
        PushDeviceRow existing = savedRow();

        when(pushDeviceMapper.findByMemberAndDeviceKey(
                MEMBER_ID,
                DEVICE_KEY
        )).thenReturn(existing, existing);
        when(pushTokenProtector.encrypt(PUSH_TOKEN))
                .thenReturn(new byte[]{1});
        when(pushDeviceMapper.upsert(
                org.mockito.ArgumentMatchers.any()
        )).thenReturn(2);

        PushDeviceRegistrationResult result =
                pushDeviceService.register(MEMBER_ID, request());

        assertFalse(result.isCreated());
        assertEquals(31L, result.getResponse().getPushDeviceId());
    }

    @Test
    void rejectsUnregisterWhenDeviceDoesNotBelongToMember() {
        when(pushDeviceMapper.deactivate(31L, MEMBER_ID))
                .thenReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> pushDeviceService.unregister(MEMBER_ID, 31L)
        );

        assertEquals(
                ErrorCode.PUSH_DEVICE_NOT_FOUND,
                exception.getErrorCode()
        );
    }

    private RegisterPushDeviceRequest request() {
        return new RegisterPushDeviceRequest(
                DEVICE_KEY,
                PushPlatform.WEB,
                PushProvider.FCM,
                PUSH_TOKEN,
                " MacBook Air "
        );
    }

    private PushDeviceRow savedRow() {
        LocalDateTime now = LocalDateTime.of(
                2026, 8, 20, 20, 0
        );
        return new PushDeviceRow(
                31L,
                MEMBER_ID,
                DEVICE_KEY,
                PushPlatform.WEB,
                PushProvider.FCM,
                "MacBook Air",
                true,
                now,
                now,
                now
        );
    }
}
