package com.azas.domain.timecapsule.dto;

import com.azas.domain.timecapsule.entity.TimeCapsuleEntry;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@ApiModel(description = "타임캡슐 기록 생성 응답")
@Getter
public class TimeCapsuleEntryResponse {

    @JsonProperty("time_capsule_entry_id")
    private final Long timeCapsuleEntryId;

    @JsonProperty("time_capsule_id")
    private final Long timeCapsuleId;

    @JsonProperty("author_member_id")
    private final Long authorMemberId;

    private final String status;
    private final String title;

    @JsonProperty("contribution_amount")
    private final BigDecimal contributionAmount;

    @JsonProperty("contributed_at")
    private final LocalDateTime contributedAt;

    @JsonProperty("media_mode")
    private final String mediaMode;

    private TimeCapsuleEntryResponse(TimeCapsuleEntry entry) {
        this.timeCapsuleEntryId = entry.getTimeCapsuleEntryId();
        this.timeCapsuleId = entry.getTimeCapsuleId();
        this.authorMemberId = entry.getAuthorMemberId();
        this.status = entry.getStatus().name();
        this.title = entry.getTitle();
        this.contributionAmount = entry.getContributionAmount();
        this.contributedAt = entry.getContributedAt();
        this.mediaMode = entry.getMediaMode().name();
    }

    public static TimeCapsuleEntryResponse from(TimeCapsuleEntry entry) {
        return new TimeCapsuleEntryResponse(entry);
    }
}
