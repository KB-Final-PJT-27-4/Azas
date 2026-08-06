package com.azas.domain.timecapsule.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TimeCapsuleExport {

    private Long timeCapsuleExportId;
    private Long timeCapsuleId;
    private Long requestedByMemberId;
    private TimeCapsuleExportType exportType;
    private String optionsJson;
    private TimeCapsuleExportStatus status;
    private String outputObjectKey;
    private String outputMimeType;
    private Long outputFileSize;
    private String errorCode;
    private String errorMessage;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // [JMG] CAPSULE-11 비동기 결과물 생성 요청을 PENDING 상태로 생성한다.
    public static TimeCapsuleExport createPending(
            long timeCapsuleId,
            long requestedByMemberId,
            TimeCapsuleExportType exportType,
            String optionsJson
    ) {
        TimeCapsuleExport export = new TimeCapsuleExport();
        export.timeCapsuleId = timeCapsuleId;
        export.requestedByMemberId = requestedByMemberId;
        export.exportType = exportType;
        export.optionsJson = optionsJson;
        export.status = TimeCapsuleExportStatus.PENDING;
        return export;
    }

    // [JMG] CAPSULE-10 다운로드 가능한 최종 결과물 상태인지 확인한다.
    public boolean isSucceeded() {
        return status == TimeCapsuleExportStatus.SUCCEEDED;
    }

    // [JMG] CAPSULE-10 결과물 보관 만료 시각이 현재보다 이전인지 확인한다.
    public boolean isExpiredAt(LocalDateTime now) {
        return expiresAt == null || !expiresAt.isAfter(now);
    }
}
