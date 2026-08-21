package com.azas.domain.notification.dto;

import com.azas.domain.notification.entity.PushPlatform;
import com.azas.domain.notification.entity.PushProvider;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PushDeviceResponse {

    @JsonProperty("push_device_id")
    private final Long pushDeviceId;

    @JsonProperty("device_key")
    private final String deviceKey;

    private final PushPlatform platform;

    private final PushProvider provider;

    @JsonProperty("device_name")
    private final String deviceName;

    private final boolean active;

    @JsonProperty("last_seen_at")
    private final LocalDateTime lastSeenAt;

    @JsonProperty("created_at")
    private final LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private final LocalDateTime updatedAt;

    public static PushDeviceResponse from(PushDeviceRow row) {
        return new PushDeviceResponse(
                row.getPushDeviceId(),
                row.getDeviceKey(),
                row.getPlatform(),
                row.getProvider(),
                row.getDeviceName(),
                row.isActive(),
                row.getLastSeenAt(),
                row.getCreatedAt(),
                row.getUpdatedAt()
        );
    }
}
