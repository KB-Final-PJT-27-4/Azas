package com.azas.domain.timecapsule.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@ApiModel(description = "타임캡슐 대표 이미지 업로드 URL 발급 응답")
@Getter
public class CreateTimeCapsuleMediaUploadUrlResponse {

    @JsonProperty("time_capsule_entry_id")
    private final long timeCapsuleEntryId;

    @JsonProperty("time_capsule_media_id")
    private final long timeCapsuleMediaId;

    @JsonProperty("upload_url")
    private final String uploadUrl;

    @JsonProperty("expires_at")
    private final LocalDateTime expiresAt;

    @JsonProperty("required_headers")
    private final Map<String, String> requiredHeaders;

    public CreateTimeCapsuleMediaUploadUrlResponse(
            long timeCapsuleEntryId,
            long timeCapsuleMediaId,
            String uploadUrl,
            LocalDateTime expiresAt,
            Map<String, String> requiredHeaders
    ) {
        this.timeCapsuleEntryId = timeCapsuleEntryId;
        this.timeCapsuleMediaId = timeCapsuleMediaId;
        this.uploadUrl = uploadUrl;
        this.expiresAt = expiresAt;
        this.requiredHeaders = Map.copyOf(requiredHeaders);
    }
}
