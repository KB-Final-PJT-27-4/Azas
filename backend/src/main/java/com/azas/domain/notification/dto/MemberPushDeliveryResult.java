package com.azas.domain.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberPushDeliveryResult {

    private final int attemptedCount;
    private final int successCount;
    private final int failureCount;
    private final int invalidatedDeviceCount;
}
