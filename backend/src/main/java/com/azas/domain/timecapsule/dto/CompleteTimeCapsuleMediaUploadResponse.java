package com.azas.domain.timecapsule.dto;

import com.azas.domain.timecapsule.entity.TimeCapsuleEntry;
import com.azas.domain.timecapsule.entity.TimeCapsuleMedia;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class CompleteTimeCapsuleMediaUploadResponse {

    @JsonProperty("time_capsule_entry_id")
    private final Long timeCapsuleEntryId;

    @JsonProperty("media_mode")
    private final String mediaMode;

    @JsonProperty("media_count")
    private final int mediaCount;

    @JsonProperty("thumbnail_ready")
    private final boolean thumbnailReady;
    private final List<MediaResponse> media;

    // [JMG] CAPSULE-8 활성화된 미디어와 엔트리 상태를 업로드 완료 응답으로 구성한다.
    public CompleteTimeCapsuleMediaUploadResponse(
            TimeCapsuleEntry entry,
            int mediaCount,
            boolean thumbnailReady,
            List<TimeCapsuleMedia> media
    ) {
        this.timeCapsuleEntryId = entry.getTimeCapsuleEntryId();
        this.mediaMode = entry.getMediaMode().name();
        this.mediaCount = mediaCount;
        this.thumbnailReady = thumbnailReady;
        this.media = media.stream().map(MediaResponse::new).toList();
    }

    @Getter
    public static class MediaResponse {

        @JsonProperty("time_capsule_media_id")
        private final Long timeCapsuleMediaId;

        @JsonProperty("media_type")
        private final String mediaType;

        @JsonProperty("slot_no")
        private final int slotNo;
        private final String status;

        // [JMG] CAPSULE-8 활성 미디어 엔티티를 프런트 상태 표시용 응답 항목으로 구성한다.
        private MediaResponse(TimeCapsuleMedia media) {
            this.timeCapsuleMediaId = media.getTimeCapsuleMediaId();
            this.mediaType = media.getMediaType().name();
            this.slotNo = media.getSlotNo();
            this.status = media.getStatus().name();
        }
    }
}
