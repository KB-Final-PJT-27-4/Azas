package com.azas.domain.timecapsule.dto;

import com.azas.domain.timecapsule.entity.TimeCapsuleEntry;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class CreateTimeCapsuleEntryResponse {

    @JsonProperty("time_capsule_entry_id")
    private final Long timeCapsuleEntryId;

    @JsonProperty("time_capsule_id")
    private final Long timeCapsuleId;

    @JsonProperty("account_transaction_id")
    private final Long accountTransactionId;

    @JsonProperty("contribution_amount")
    private final BigDecimal contributionAmount;

    @JsonProperty("contributed_at")
    private final LocalDateTime contributedAt;

    private final String status;

    @JsonProperty("created_at")
    private final LocalDateTime createdAt;

    private CreateTimeCapsuleEntryResponse(TimeCapsuleEntry entry) {
        this.timeCapsuleEntryId = entry.getTimeCapsuleEntryId();
        this.timeCapsuleId = entry.getTimeCapsuleId();
        this.accountTransactionId = entry.getAccountTransactionId();
        this.contributionAmount = entry.getContributionAmount();
        this.contributedAt = entry.getContributedAt();
        this.status = entry.getStatus().name();
        this.createdAt = entry.getCreatedAt();
    }

    public static CreateTimeCapsuleEntryResponse from(
            TimeCapsuleEntry entry
    ) {
        return new CreateTimeCapsuleEntryResponse(entry);
    }
}
