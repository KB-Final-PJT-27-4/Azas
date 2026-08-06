package com.azas.domain.timecapsule.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
public class CreateTimeCapsuleMediaUploadUrlsResponse {

    @JsonProperty("time_capsule_entry_id")
    private final long timeCapsuleEntryId;
    private final List<UploadResponse> uploads;

    // [JMG] CAPSULE-7 발급된 업로드 URL 목록을 엔트리 기준 프런트 응답으로 구성한다.
    public CreateTimeCapsuleMediaUploadUrlsResponse(
            long timeCapsuleEntryId,
            List<UploadResponse> uploads
    ) {
        this.timeCapsuleEntryId = timeCapsuleEntryId;
        this.uploads = List.copyOf(uploads);
    }

    @Getter
    public static class UploadResponse {

        @JsonProperty("time_capsule_media_id")
        private final Long timeCapsuleMediaId;

        @JsonProperty("slot_no")
        private final int slotNo;

        @JsonProperty("upload_url")
        private final String uploadUrl;

        @JsonProperty("expires_at")
        private final LocalDateTime expiresAt;

        @JsonProperty("required_headers")
        private final Map<String, String> requiredHeaders;

        // [JMG] CAPSULE-7 개별 미디어 업로드에 필요한 임시 URL과 헤더를 응답 항목으로 구성한다.
        public UploadResponse(
                Long timeCapsuleMediaId,
                int slotNo,
                String uploadUrl,
                LocalDateTime expiresAt,
                Map<String, String> requiredHeaders
        ) {
            this.timeCapsuleMediaId = timeCapsuleMediaId;
            this.slotNo = slotNo;
            this.uploadUrl = uploadUrl;
            this.expiresAt = expiresAt;
            this.requiredHeaders = Map.copyOf(requiredHeaders);
        }
    }
}
