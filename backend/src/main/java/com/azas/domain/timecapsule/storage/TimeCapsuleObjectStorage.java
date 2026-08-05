package com.azas.domain.timecapsule.storage;

import java.time.Duration;

public interface TimeCapsuleObjectStorage {

    // [JMG] CAPSULE-7 비공개 객체에 대한 짧은 수명의 PUT Presigned URL을 발급한다.
    PresignedUrl createUploadUrl(
            String objectKey,
            String mimeType,
            Duration validFor
    );

    // [JMG] CAPSULE-4 비공개 썸네일·미디어 객체에 대한 짧은 수명의 GET Presigned URL을 발급한다.
    PresignedUrl createDownloadUrl(String objectKey, Duration validFor);

    // [JMG] CAPSULE-8 업로드 완료 객체의 실제 MIME 타입과 파일 크기를 조회한다.
    StoredObjectMetadata getObjectMetadata(String objectKey);

    record PresignedUrl(String url) {
    }

    record StoredObjectMetadata(String mimeType, long fileSize) {
    }
}
