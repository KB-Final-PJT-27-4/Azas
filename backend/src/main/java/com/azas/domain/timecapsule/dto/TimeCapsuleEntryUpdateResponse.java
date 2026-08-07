package com.azas.domain.timecapsule.dto;

import com.azas.domain.timecapsule.entity.TimeCapsuleEntry;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class TimeCapsuleEntryUpdateResponse {

    @JsonProperty("time_capsule_entry_id")
    private final Long timeCapsuleEntryId;

    private final String title;
    private final String message;
    private final String status;

    @JsonProperty("edit_count")
    private final int editCount;

    @JsonProperty("updated_at")
    private final LocalDateTime updatedAt;

    private TimeCapsuleEntryUpdateResponse(TimeCapsuleEntry entry) {
        this.timeCapsuleEntryId = entry.getTimeCapsuleEntryId();
        this.title = entry.getTitle();
        this.message = entry.getMessage();
        this.status = entry.getStatus().name();
        this.editCount = entry.getEditCount();
        this.updatedAt = entry.getUpdatedAt();
    }

    // [JMG] CAPSULE-12 수정된 엔트리 엔티티를 프런트엔드 수정 결과 응답으로 변환한다.
    public static TimeCapsuleEntryUpdateResponse from(TimeCapsuleEntry entry) {
        return new TimeCapsuleEntryUpdateResponse(entry);
    }
}
