package com.azas.domain.notification.service;

import com.azas.domain.notification.dto.ActivePushDeviceRow;
import com.azas.domain.notification.dto.MemberPushDeliveryResult;
import com.azas.domain.notification.mapper.PushDeviceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberPushService {

    private final PushDeviceMapper pushDeviceMapper;
    private final PushTokenProtector pushTokenProtector;
    private final PushMessageSender pushMessageSender;

    public MemberPushDeliveryResult sendToMember(
            Long memberId,
            PushMessage pushMessage
    ) {
        List<ActivePushDeviceRow> devices =
                pushDeviceMapper.findActiveByMemberId(
                        memberId
                );

        int successCount = 0;
        int failureCount = 0;
        int invalidatedDeviceCount = 0;

        for (ActivePushDeviceRow device : devices) {
            final String pushToken;

            try {
                pushToken = pushTokenProtector.decrypt(
                        device.getTokenCiphertext()
                );
            } catch (IllegalArgumentException exception) {
                failureCount++;
                invalidatedDeviceCount +=
                        pushDeviceMapper.deactivateById(
                                device.getPushDeviceId()
                        );
                continue;
            }

            try {
                pushMessageSender.send(pushToken, pushMessage);
                successCount++;
            } catch (PushDeliveryException exception) {
                failureCount++;

                if (exception.isInvalidToken()) {
                    invalidatedDeviceCount +=
                            pushDeviceMapper.deactivateById(
                                    device.getPushDeviceId()
                            );
                }
            } catch (RuntimeException exception) {
                // 메시지 구성 등 토큰과 무관한 오류는 기기를
                // 비활성화하지 않고 해당 발송만 실패 처리한다.
                failureCount++;
            }
        }

        return new MemberPushDeliveryResult(
                devices.size(),
                successCount,
                failureCount,
                invalidatedDeviceCount
        );
    }
}
