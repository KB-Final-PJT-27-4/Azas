package com.azas.domain.timecapsule.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TimeCapsuleMedia {

    private Long timeCapsuleMediaId;
    private Long timeCapsuleEntryId;
    private TimeCapsuleMediaType mediaType;
    private String objectKey;
    private String mimeType;
    private long fileSize;
    private int slotNo;
    private TimeCapsuleMediaStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TimeCapsuleMedia createPendingUpload(
            long timeCapsuleEntryId,
            TimeCapsuleMediaType mediaType,
            String objectKey,
            String mimeType,
            long fileSize,
            int slotNo
    ) {
        TimeCapsuleMedia media = new TimeCapsuleMedia();
        media.timeCapsuleEntryId = timeCapsuleEntryId;
        media.mediaType = mediaType;
        media.objectKey = objectKey;
        media.mimeType = mimeType;
        media.fileSize = fileSize;
        media.slotNo = slotNo;
        media.status = TimeCapsuleMediaStatus.PENDING_UPLOAD;
        return media;
    }

    public boolean matchesUploadedObject(
            String uploadedMimeType,
            long uploadedFileSize
    ) {
        return mimeType.equalsIgnoreCase(uploadedMimeType)
                && fileSize == uploadedFileSize;
    }

    public void activate() {
        status = TimeCapsuleMediaStatus.ACTIVE;
    }
}
