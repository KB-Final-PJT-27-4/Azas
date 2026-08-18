package com.azas.domain.timecapsule.dto;

import com.azas.domain.timecapsule.entity.TimeCapsuleEntry;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class TimeCapsuleEntrySealResponse {

    @JsonProperty("time_capsule_entry_id")
    private final Long timeCapsuleEntryId;

    private final String status;

    @JsonProperty("sealed_at")
    private final LocalDateTime sealedAt;

    private TimeCapsuleEntrySealResponse(TimeCapsuleEntry entry) {
        this.timeCapsuleEntryId = entry.getTimeCapsuleEntryId();
        this.status = entry.getStatus().name();
        this.sealedAt = entry.getSealedAt();
    }

    public static TimeCapsuleEntrySealResponse from(TimeCapsuleEntry entry) {
        return new TimeCapsuleEntrySealResponse(entry);
    }
}
