package com.azas.domain.timecapsule.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Getter
@NoArgsConstructor
public class CompleteTimeCapsuleMediaUploadRequest {

    @NotNull
    @Positive
    @JsonProperty("time_capsule_media_id")
    private Long timeCapsuleMediaId;
}
