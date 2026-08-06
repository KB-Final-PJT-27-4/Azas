package com.azas.domain.timecapsule.dto;

import com.azas.domain.timecapsule.entity.TimeCapsuleEntry;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@ApiModel(description = "타임캡슐 기록 목록 항목")
@Getter
public class TimeCapsuleEntrySummaryResponse {

    @JsonProperty("time_capsule_entry_id")
    private final Long timeCapsuleEntryId;

    private final String title;

    @JsonProperty("contribution_amount")
    private final BigDecimal contributionAmount;

    @JsonProperty("contributed_at")
    private final LocalDateTime contributedAt;

    @JsonProperty("media_mode")
    private final String mediaMode;

    @JsonProperty("thumbnail_url")
    private final String thumbnailUrl;

    @JsonProperty("thumbnail_expires_at")
    private final LocalDateTime thumbnailExpiresAt;

    private final String status;

    @JsonProperty("media_count")
    private final int mediaCount;

    private TimeCapsuleEntrySummaryResponse(
            TimeCapsuleEntry entry,
            String thumbnailUrl,
            LocalDateTime thumbnailExpiresAt
    ) {
        this.timeCapsuleEntryId = entry.getTimeCapsuleEntryId();
        this.title = entry.getTitle();
        this.contributionAmount = entry.getContributionAmount();
        this.contributedAt = entry.getContributedAt();
        this.mediaMode = entry.getMediaMode().name();
        this.thumbnailUrl = thumbnailUrl;
        this.thumbnailExpiresAt = thumbnailExpiresAt;
        this.status = entry.getStatus().name();
        this.mediaCount = entry.getMediaCount();
    }

    // [JMG] CAPSULE-4 기록 조회 결과를 목록 응답 항목으로 변환한다.
    public static TimeCapsuleEntrySummaryResponse from(
            TimeCapsuleEntry entry
    ) {
        return new TimeCapsuleEntrySummaryResponse(entry, null, null);
    }

    // [JMG] CAPSULE-4 서버가 발급한 안전한 썸네일 URL을 엔트리 목록 응답에 포함한다.
    public static TimeCapsuleEntrySummaryResponse from(
            TimeCapsuleEntry entry,
            String thumbnailUrl,
            LocalDateTime thumbnailExpiresAt
    ) {
        return new TimeCapsuleEntrySummaryResponse(
                entry,
                thumbnailUrl,
                thumbnailExpiresAt
        );
    }
}
