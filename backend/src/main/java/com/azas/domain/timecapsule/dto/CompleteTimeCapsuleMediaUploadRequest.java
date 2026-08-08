package com.azas.domain.timecapsule.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;
import java.util.List;

@Getter
@NoArgsConstructor
public class CompleteTimeCapsuleMediaUploadRequest {

    @NotEmpty
    @JsonProperty("media_ids")
    private List<@Positive Long> mediaIds;
}
