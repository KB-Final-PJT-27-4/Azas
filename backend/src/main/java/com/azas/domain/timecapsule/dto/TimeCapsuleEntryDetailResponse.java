package com.azas.domain.timecapsule.dto;

import com.azas.domain.timecapsule.entity.TimeCapsuleEntry;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@ApiModel(value = "TimeCapsuleEntryDetailResponse")
public class TimeCapsuleEntryDetailResponse {

    @JsonProperty("time_capsule_entry_id")
    private final Long timeCapsuleEntryId;

    @JsonProperty("time_capsule_id")
    private final Long timeCapsuleId;

    @JsonProperty("entry_number")
    private final int entryNumber;

    @JsonProperty("total_entry_count")
    private final int totalEntryCount;

    private final String title;
    private final String message;

    @JsonProperty("account_transaction_id")
    private final Long accountTransactionId;

    @JsonProperty("contribution_amount")
    private final BigDecimal contributionAmount;

    @JsonProperty("contributed_at")
    private final LocalDateTime contributedAt;

    @ApiModelProperty(required = true)
    private final ImageResponse image;

    public TimeCapsuleEntryDetailResponse(
            TimeCapsuleEntry entry,
            int entryNumber,
            int totalEntryCount,
            ImageResponse image
    ) {
        this.timeCapsuleEntryId = entry.getTimeCapsuleEntryId();
        this.timeCapsuleId = entry.getTimeCapsuleId();
        this.entryNumber = entryNumber;
        this.totalEntryCount = totalEntryCount;
        this.title = entry.getTitle();
        this.message = entry.getMessage();
        this.accountTransactionId = entry.getAccountTransactionId();
        this.contributionAmount = entry.getContributionAmount();
        this.contributedAt = entry.getContributedAt();
        this.image = image;
    }

    @Getter
    @ApiModel(value = "TimeCapsuleEntryDetailImageResponse")
    public static class ImageResponse {

        @JsonProperty("download_url")
        private final String downloadUrl;

        @JsonProperty("expires_at")
        private final LocalDateTime expiresAt;

        public ImageResponse(
                String downloadUrl,
                LocalDateTime expiresAt
        ) {
            this.downloadUrl = downloadUrl;
            this.expiresAt = expiresAt;
        }
    }
}
