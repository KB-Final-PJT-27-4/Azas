package com.azas.domain.timecapsule.storage;

import java.time.Duration;

public interface TimeCapsuleObjectStorage {

    PresignedUrl createUploadUrl(
            String objectKey,
            String mimeType,
            Duration validFor
    );

    PresignedUrl createDownloadUrl(String objectKey, Duration validFor);

    StoredObjectMetadata getObjectMetadata(String objectKey);

    void deleteObject(String objectKey);

    record PresignedUrl(String url) {
    }

    record StoredObjectMetadata(String mimeType, long fileSize) {
    }
}
