package com.azas.domain.notification.dto;

import com.azas.domain.notification.entity.PushPlatform;
import com.azas.domain.notification.entity.PushProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PushDeviceCommand {

    private final Long memberId;
    private final String deviceKey;
    private final PushPlatform platform;
    private final PushProvider provider;
    private final String deviceName;
    private final byte[] tokenCiphertext;
    private final String tokenHash;
}
