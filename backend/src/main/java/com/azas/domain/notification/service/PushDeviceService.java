package com.azas.domain.notification.service;

import com.azas.domain.auth.service.TokenHashEncoder;
import com.azas.domain.notification.dto.PushDeviceCommand;
import com.azas.domain.notification.dto.PushDeviceRegistrationResult;
import com.azas.domain.notification.dto.PushDeviceResponse;
import com.azas.domain.notification.dto.PushDeviceRow;
import com.azas.domain.notification.dto.RegisterPushDeviceRequest;
import com.azas.domain.notification.mapper.PushDeviceMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PushDeviceService {

    private final PushDeviceMapper pushDeviceMapper;
    private final PushTokenProtector pushTokenProtector;
    private final TokenHashEncoder tokenHashEncoder;

    @Transactional
    public PushDeviceRegistrationResult register(
            Long memberId,
            RegisterPushDeviceRequest request
    ) {
        PushDeviceRow existing =
                pushDeviceMapper.findByMemberAndDeviceKey(
                        memberId,
                        request.getDeviceKey()
                );

        String tokenHash =
                tokenHashEncoder.encode(request.getPushToken());

        pushDeviceMapper.deactivateByTokenHashExceptDevice(
                tokenHash,
                memberId,
                request.getDeviceKey()
        );

        PushDeviceCommand command = new PushDeviceCommand(
                memberId,
                request.getDeviceKey(),
                request.getPlatform(),
                request.getProvider(),
                normalizeDeviceName(request.getDeviceName()),
                pushTokenProtector.encrypt(request.getPushToken()),
                tokenHash
        );

        int affectedRows = pushDeviceMapper.upsert(command);
        if (affectedRows < 1) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR
            );
        }

        PushDeviceRow saved =
                pushDeviceMapper.findByMemberAndDeviceKey(
                        memberId,
                        request.getDeviceKey()
                );

        if (saved == null) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR
            );
        }

        return new PushDeviceRegistrationResult(
                PushDeviceResponse.from(saved),
                existing == null
        );
    }

    @Transactional
    public void unregister(
            Long memberId,
            Long pushDeviceId
    ) {
        if (pushDeviceId == null || pushDeviceId <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_PUSH_DEVICE_REQUEST
            );
        }

        int affectedRows = pushDeviceMapper.deactivate(
                pushDeviceId,
                memberId
        );

        if (affectedRows < 1) {
            throw new BusinessException(
                    ErrorCode.PUSH_DEVICE_NOT_FOUND
            );
        }
    }

    private String normalizeDeviceName(String deviceName) {
        if (deviceName == null || deviceName.isBlank()) {
            return null;
        }
        return deviceName.trim();
    }
}
