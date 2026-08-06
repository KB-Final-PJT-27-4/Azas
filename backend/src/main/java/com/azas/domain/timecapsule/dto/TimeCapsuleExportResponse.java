package com.azas.domain.timecapsule.dto;

import com.azas.domain.timecapsule.entity.TimeCapsuleExport;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class TimeCapsuleExportResponse {

    @JsonProperty("time_capsule_export_id")
    private final Long timeCapsuleExportId;

    @JsonProperty("time_capsule_id")
    private final Long timeCapsuleId;

    @JsonProperty("export_type")
    private final String exportType;
    private final String status;

    @JsonProperty("output_mime_type")
    private final String outputMimeType;

    @JsonProperty("output_file_size")
    private final Long outputFileSize;

    @JsonProperty("expires_at")
    private final LocalDateTime expiresAt;

    @JsonProperty("started_at")
    private final LocalDateTime startedAt;

    @JsonProperty("completed_at")
    private final LocalDateTime completedAt;

    @JsonProperty("created_at")
    private final LocalDateTime createdAt;

    // [JMG] CAPSULE-9·11 비동기 결과물 작업 엔티티를 프런트 상태 표시용 응답으로 변환한다.
    private TimeCapsuleExportResponse(TimeCapsuleExport export) {
        this.timeCapsuleExportId = export.getTimeCapsuleExportId();
        this.timeCapsuleId = export.getTimeCapsuleId();
        this.exportType = export.getExportType().name();
        this.status = export.getStatus().name();
        this.outputMimeType = export.getOutputMimeType();
        this.outputFileSize = export.getOutputFileSize();
        this.expiresAt = export.getExpiresAt();
        this.startedAt = export.getStartedAt();
        this.completedAt = export.getCompletedAt();
        this.createdAt = export.getCreatedAt();
    }

    // [JMG] CAPSULE-9·11 조회·생성 결과를 동일한 결과물 응답 형식으로 구성한다.
    public static TimeCapsuleExportResponse from(TimeCapsuleExport export) {
        return new TimeCapsuleExportResponse(export);
    }
}
