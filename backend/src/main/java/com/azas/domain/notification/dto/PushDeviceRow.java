package com.azas.domain.notification.dto;

import com.azas.domain.notification.entity.PushPlatform;
import com.azas.domain.notification.entity.PushProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PushDeviceRow {

    private Long pushDeviceId;
    private Long memberId;
    private String deviceKey;
    private PushPlatform platform;
    private PushProvider provider;
    private String deviceName;
    private boolean active;
    private LocalDateTime lastSeenAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
