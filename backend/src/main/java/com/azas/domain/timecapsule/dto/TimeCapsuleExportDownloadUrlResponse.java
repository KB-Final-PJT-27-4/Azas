package com.azas.domain.timecapsule.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class TimeCapsuleExportDownloadUrlResponse {

    @JsonProperty("time_capsule_export_id")
    private final Long timeCapsuleExportId;

    @JsonProperty("download_url")
    private final String downloadUrl;

    @JsonProperty("expires_in_seconds")
    private final long expiresInSeconds;

    // [JMG] CAPSULE-10 완료된 결과물에 대한 짧은 수명의 다운로드 URL 응답을 구성한다.
    public TimeCapsuleExportDownloadUrlResponse(
            Long timeCapsuleExportId,
            String downloadUrl,
            long expiresInSeconds
    ) {
        this.timeCapsuleExportId = timeCapsuleExportId;
        this.downloadUrl = downloadUrl;
        this.expiresInSeconds = expiresInSeconds;
    }
}
