package com.azas.domain.timecapsule.dto;

import com.azas.domain.timecapsule.entity.TimeCapsuleEntry;
import com.azas.domain.timecapsule.entity.TimeCapsuleMedia;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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

    @JsonProperty("media_mode")
    private final String mediaMode;
    private final String status;

    @JsonProperty("edit_count")
    private final int editCount;

    @JsonProperty("sealed_at")
    private final LocalDateTime sealedAt;
    private final List<MediaResponse> media;

    @JsonProperty("created_at")
    private final LocalDateTime createdAt;

    // [JMG] CAPSULE-14 권한 검증된 엔트리와 활성 미디어의 임시 URL을 상세 응답으로 구성한다.
    public TimeCapsuleEntryDetailResponse(
            TimeCapsuleEntry entry,
            List<MediaResponse> media
    ) {
        this.timeCapsuleEntryId = entry.getTimeCapsuleEntryId();
        this.timeCapsuleId = entry.getTimeCapsuleId();
        this.authorMemberId = entry.getAuthorMemberId();
        this.accountTransactionId = entry.getAccountTransactionId();
        this.title = entry.getTitle();
        this.message = entry.getMessage();
        this.contributionAmount = entry.getContributionAmount();
        this.contributedAt = entry.getContributedAt();
        this.mediaMode = entry.getMediaMode().name();
        this.status = entry.getStatus().name();
        this.editCount = entry.getEditCount();
        this.sealedAt = entry.getSealedAt();
        this.media = List.copyOf(media);
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

        @JsonProperty("slot_no")
        private final int slotNo;

        @JsonProperty("download_url")
        private final String downloadUrl;

        @JsonProperty("expires_at")
        private final LocalDateTime expiresAt;

        // [JMG] CAPSULE-14 저장소 객체 키 대신 만료되는 다운로드 URL만 미디어 응답에 담는다.
        public MediaResponse(
                TimeCapsuleMedia media,
                String downloadUrl,
                LocalDateTime expiresAt
        ) {
            this.timeCapsuleMediaId = media.getTimeCapsuleMediaId();
            this.mediaType = media.getMediaType().name();
            this.mimeType = media.getMimeType();
            this.fileSize = media.getFileSize();
            this.slotNo = media.getSlotNo();
            this.downloadUrl = downloadUrl;
            this.expiresAt = expiresAt;
        }
    }
}
