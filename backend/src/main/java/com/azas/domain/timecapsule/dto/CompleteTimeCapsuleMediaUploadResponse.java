package com.azas.domain.timecapsule.dto;

import com.azas.domain.timecapsule.entity.TimeCapsuleMedia;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class CompleteTimeCapsuleMediaUploadResponse {

    @JsonProperty("time_capsule_entry_id")
    private final Long timeCapsuleEntryId;

    @JsonProperty("time_capsule_media_id")
    private final Long timeCapsuleMediaId;

    @JsonProperty("media_status")
    private final String mediaStatus;

    // [JMG] CAPSULE-8 단일 이미지의 활성화 결과만 최소 응답으로 구성한다.
    public CompleteTimeCapsuleMediaUploadResponse(TimeCapsuleMedia media) {
        this.timeCapsuleEntryId = media.getTimeCapsuleEntryId();
        this.timeCapsuleMediaId = media.getTimeCapsuleMediaId();
        this.mediaStatus = media.getStatus().name();
    }
}
