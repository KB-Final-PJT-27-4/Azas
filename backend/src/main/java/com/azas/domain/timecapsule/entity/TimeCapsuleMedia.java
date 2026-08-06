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

    // [JMG] CAPSULE-7 서버가 생성한 객체 키를 사용해 업로드 대기 미디어를 생성한다.
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

    // [JMG] CAPSULE-8 저장소 객체의 MIME 타입과 파일 크기가 사전 요청과 일치하는지 검증한다.
    public boolean matchesUploadedObject(
            String uploadedMimeType,
            long uploadedFileSize
    ) {
        return mimeType.equalsIgnoreCase(uploadedMimeType)
                && fileSize == uploadedFileSize;
    }

    // [JMG] CAPSULE-8 저장소 검증을 통과한 업로드 대기 미디어를 활성 상태로 전환한다.
    public void activate() {
        status = TimeCapsuleMediaStatus.ACTIVE;
    }
}
