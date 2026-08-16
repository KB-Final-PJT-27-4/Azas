package com.azas.domain.timecapsule.dto;

import com.azas.domain.timecapsule.entity.TimeCapsule;
import com.azas.domain.timecapsule.entity.TimeCapsuleAccount;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@ApiModel(description = "타임캡슐 보관함 생성 응답")
@Getter
public class CreateTimeCapsuleResponse {

    @JsonProperty("time_capsule_id")
    private final Long timeCapsuleId;

    @JsonProperty("child_id")
    private final Long childId;

    private final String title;
    private final TimeCapsuleAccountResponse account;
    private final String status;

    @JsonProperty("release_date")
    private final LocalDate releaseDate;

    @JsonProperty("released_at")
    private final LocalDateTime releasedAt;

    @JsonProperty("entry_count")
    private final int entryCount;

    @JsonProperty("total_saved_amount")
    private final BigDecimal totalSavedAmount;

    @JsonProperty("latest_entry_at")
    private final LocalDateTime latestEntryAt;

    @JsonProperty("created_at")
    private final LocalDateTime createdAt;

    private CreateTimeCapsuleResponse(
            TimeCapsule timeCapsule,
            TimeCapsuleAccount account
    ) {
        this.timeCapsuleId = timeCapsule.getTimeCapsuleId();
        this.childId = timeCapsule.getChildId();
        this.title = timeCapsule.getTitle();
        this.account = TimeCapsuleAccountResponse.from(account);
        this.status = timeCapsule.getStatus().name();
        this.releaseDate = timeCapsule.getExpectedReleaseAt() == null
                ? null
                : timeCapsule.getExpectedReleaseAt().toLocalDate();
        this.releasedAt = timeCapsule.getReleasedAt();
        this.entryCount = timeCapsule.getEntryCount();
        this.totalSavedAmount =
                timeCapsule.getTotalContributionAmount() == null
                        ? BigDecimal.ZERO
                        : timeCapsule.getTotalContributionAmount();
        this.latestEntryAt = timeCapsule.getLatestEntryAt();
        this.createdAt = timeCapsule.getCreatedAt();
    }

    public static CreateTimeCapsuleResponse from(
            TimeCapsule timeCapsule,
            TimeCapsuleAccount account
    ) {
        return new CreateTimeCapsuleResponse(timeCapsule, account);
    }
}
