package com.azas.domain.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActivePushDeviceRow {

    private Long pushDeviceId;
    private byte[] tokenCiphertext;
}
