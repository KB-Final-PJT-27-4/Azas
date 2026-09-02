package com.azas.domain.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PushDeviceRegistrationResult {

    private final PushDeviceResponse response;
    private final boolean created;
}
