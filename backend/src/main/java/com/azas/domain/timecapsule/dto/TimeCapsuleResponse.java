package com.azas.domain.timecapsule.dto;

import com.azas.domain.timecapsule.entity.TimeCapsule;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.Getter;

import java.time.LocalDateTime;

@ApiModel(description = "타임캡슐 보관함 응답")
@Getter
public class TimeCapsuleResponse {

    @JsonProperty("time_capsule_id")
    private final Long timeCapsuleId;

    @JsonProperty("child_id")
    private final Long childId;

    @JsonProperty("financial_account_id")
    private final Long financialAccountId;

    private final String title;
    private final String status;

    @JsonProperty("expected_release_at")
    private final LocalDateTime expectedReleaseAt;

    @JsonProperty("release_reason")
    private final String releaseReason;

    @JsonProperty("released_at")
    private final LocalDateTime releasedAt;

    @JsonProperty("entry_count")
    private final int entryCount;

    @JsonProperty("latest_entry_at")
    private final LocalDateTime latestEntryAt;

    @JsonProperty("created_at")
    private final LocalDateTime createdAt;

    private TimeCapsuleResponse(TimeCapsule timeCapsule) {
        this.timeCapsuleId = timeCapsule.getTimeCapsuleId();
        this.childId = timeCapsule.getChildId();
        this.financialAccountId = timeCapsule.getFinancialAccountId();
        this.title = timeCapsule.getTitle();
        this.status = timeCapsule.getStatus().name();
        this.expectedReleaseAt = timeCapsule.getExpectedReleaseAt();
        this.releaseReason = timeCapsule.getReleaseReason();
        this.releasedAt = timeCapsule.getReleasedAt();
        this.entryCount = timeCapsule.getEntryCount();
        this.latestEntryAt = timeCapsule.getLatestEntryAt();
        this.createdAt = timeCapsule.getCreatedAt();
    }

    // [JMG] CAPSULE-1, CAPSULE-3 ERD 보관함 엔티티를 API 응답으로 변환한다.
    public static TimeCapsuleResponse from(TimeCapsule timeCapsule) {
        return new TimeCapsuleResponse(timeCapsule);
    }
}
