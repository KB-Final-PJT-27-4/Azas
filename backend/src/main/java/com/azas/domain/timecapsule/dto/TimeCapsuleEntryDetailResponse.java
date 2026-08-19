package com.azas.domain.timecapsule.dto;

import com.azas.domain.timecapsule.entity.TimeCapsuleEntry;
import com.azas.domain.timecapsule.entity.TimeCapsuleMedia;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class TimeCapsuleEntryDetailResponse {

    @JsonProperty("time_capsule_entry_id")
    private final Long timeCapsuleEntryId;

    @JsonProperty("time_capsule_id")
    private final Long timeCapsuleId;

    @JsonProperty("author_member_id")
    private final Long authorMemberId;

    @JsonProperty("account_transaction_id")
    private final Long accountTransactionId;

    private final String title;
    private final String message;

    @JsonProperty("contribution_amount")
    private final BigDecimal contributionAmount;

    @JsonProperty("contributed_at")
    private final LocalDateTime contributedAt;

    private final String status;

    @JsonProperty("sealed_at")
    private final LocalDateTime sealedAt;
    private final MediaResponse media;

    @JsonProperty("created_at")
    private final LocalDateTime createdAt;

    public TimeCapsuleEntryDetailResponse(
            TimeCapsuleEntry entry,
            MediaResponse media
    ) {
        this.timeCapsuleEntryId = entry.getTimeCapsuleEntryId();
        this.timeCapsuleId = entry.getTimeCapsuleId();
        this.authorMemberId = entry.getAuthorMemberId();
        this.accountTransactionId = entry.getAccountTransactionId();
        this.title = entry.getTitle();
        this.message = entry.getMessage();
        this.contributionAmount = entry.getContributionAmount();
        this.contributedAt = entry.getContributedAt();
        this.status = entry.getStatus().name();
        this.sealedAt = entry.getSealedAt();
        this.media = media;
        this.createdAt = entry.getCreatedAt();
    }

    @Getter
    public static class MediaResponse {

        @JsonProperty("time_capsule_media_id")
        private final Long timeCapsuleMediaId;

        @JsonProperty("media_type")
        private final String mediaType;

        @JsonProperty("mime_type")
        private final String mimeType;

        @JsonProperty("file_size")
        private final long fileSize;

        @JsonProperty("download_url")
        private final String downloadUrl;

        @JsonProperty("expires_at")
        private final LocalDateTime expiresAt;

        public MediaResponse(
                TimeCapsuleMedia media,
                String downloadUrl,
                LocalDateTime expiresAt
        ) {
            this.timeCapsuleMediaId = media.getTimeCapsuleMediaId();
            this.mediaType = media.getMediaType().name();
            this.mimeType = media.getMimeType();
            this.fileSize = media.getFileSize();
            this.downloadUrl = downloadUrl;
            this.expiresAt = expiresAt;
        }
    }
}
