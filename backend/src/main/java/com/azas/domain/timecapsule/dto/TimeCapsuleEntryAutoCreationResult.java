package com.azas.domain.timecapsule.dto;

import com.azas.domain.timecapsule.entity.TimeCapsuleEntry;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class TimeCapsuleEntryAutoCreationResult {

    @JsonProperty("time_capsule_id")
    private final Long timeCapsuleId;

    @JsonProperty("time_capsule_entry_id")
    private final Long timeCapsuleEntryId;

    private final String status;

    private TimeCapsuleEntryAutoCreationResult(TimeCapsuleEntry entry) {
        this.timeCapsuleId = entry.getTimeCapsuleId();
        this.timeCapsuleEntryId = entry.getTimeCapsuleEntryId();
        this.status = entry.getStatus().name();
    }

    // [JMG] CAPSULE-5 이체 도메인이 사용할 자동 생성 결과를 최소 정보로 변환한다.
    public static TimeCapsuleEntryAutoCreationResult from(TimeCapsuleEntry entry) {
        return new TimeCapsuleEntryAutoCreationResult(entry);
    }
}
