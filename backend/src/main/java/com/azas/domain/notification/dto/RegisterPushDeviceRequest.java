package com.azas.domain.notification.dto;

import com.azas.domain.notification.entity.PushPlatform;
import com.azas.domain.notification.entity.PushProvider;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterPushDeviceRequest {

    @NotBlank
    @Size(max = 100)
    @JsonProperty("device_key")
    private String deviceKey;

    @NotNull
    private PushPlatform platform;

    @NotNull
    private PushProvider provider;

    @NotBlank
    @Size(max = 4096)
    @JsonProperty("push_token")
    private String pushToken;

    @Size(max = 100)
    @JsonProperty("device_name")
    private String deviceName;
}
