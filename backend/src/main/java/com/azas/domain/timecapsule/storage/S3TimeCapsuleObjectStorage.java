package com.azas.domain.timecapsule.storage;

import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.Duration;

@Component
public class S3TimeCapsuleObjectStorage
        implements TimeCapsuleObjectStorage, DisposableBean {

    private static final String BUCKET_ENVIRONMENT_KEY = "S3_BUCKET";
    private static final String REGION_ENVIRONMENT_KEY = "AWS_REGION";

    private final Environment environment;
    private S3Client s3Client;
    private S3Presigner s3Presigner;

    public S3TimeCapsuleObjectStorage(Environment environment) {
        this.environment = environment;
    }

    @Override
    public PresignedUrl createUploadUrl(
            String objectKey,
            String mimeType,
            Duration validFor
    ) {
        try {
            PresignedPutObjectRequest request = getS3Presigner()
                    .presignPutObject(PutObjectPresignRequest.builder()
                            .signatureDuration(validFor)
                            .putObjectRequest(PutObjectRequest.builder()
                                    .bucket(getBucketName())
                                    .key(objectKey)
                                    .contentType(mimeType)
                                    .build())
                            .build());
            return new PresignedUrl(request.url().toExternalForm());
        } catch (SdkException exception) {
            throw new BusinessException(
                    ErrorCode.TIME_CAPSULE_STORAGE_UNAVAILABLE,
                    exception
            );
        }
    }

    @Override
    public PresignedUrl createDownloadUrl(
            String objectKey,
            Duration validFor
    ) {
        try {
            PresignedGetObjectRequest request = getS3Presigner()
                    .presignGetObject(GetObjectPresignRequest.builder()
                            .signatureDuration(validFor)
                            .getObjectRequest(GetObjectRequest.builder()
                                    .bucket(getBucketName())
                                    .key(objectKey)
                                    .build())
                            .build());
            return new PresignedUrl(request.url().toExternalForm());
        } catch (SdkException exception) {
            throw new BusinessException(
                    ErrorCode.TIME_CAPSULE_STORAGE_UNAVAILABLE,
                    exception
            );
        }
    }

    @Override
    public StoredObjectMetadata getObjectMetadata(String objectKey) {
        try {
            HeadObjectResponse object = getS3Client().headObject(
                    HeadObjectRequest.builder()
                            .bucket(getBucketName())
                            .key(objectKey)
                            .build()
            );
            return new StoredObjectMetadata(
                    object.contentType(),
                    object.contentLength()
            );
        } catch (S3Exception exception) {
            if (exception.statusCode() == 404) {
                throw new BusinessException(
                        ErrorCode.TIME_CAPSULE_MEDIA_OBJECT_INVALID,
                        exception
                );
            }
            throw new BusinessException(
                    ErrorCode.TIME_CAPSULE_STORAGE_UNAVAILABLE,
                    exception
            );
        } catch (SdkException exception) {
            throw new BusinessException(
                    ErrorCode.TIME_CAPSULE_STORAGE_UNAVAILABLE,
                    exception
            );
        }
    }

    @Override
    public void deleteObject(String objectKey) {
        try {
            getS3Client().deleteObject(DeleteObjectRequest.builder()
                    .bucket(getBucketName())
                    .key(objectKey)
                    .build());
        } catch (SdkException exception) {
            throw new BusinessException(
                    ErrorCode.TIME_CAPSULE_STORAGE_UNAVAILABLE,
                    exception
            );
        }
    }

    private String getBucketName() {
        String bucketName = environment.getProperty(BUCKET_ENVIRONMENT_KEY);
        if (bucketName == null || bucketName.isBlank()) {
            throw new BusinessException(ErrorCode.TIME_CAPSULE_STORAGE_UNAVAILABLE);
        }

        return bucketName;
    }

    private synchronized S3Client getS3Client() {
        if (s3Client == null) {
            s3Client = S3Client.builder()
                    .region(getRegion())
                    .credentialsProvider(
                            DefaultCredentialsProvider.builder().build()
                    )
                    .build();
        }
        return s3Client;
    }

    private synchronized S3Presigner getS3Presigner() {
        if (s3Presigner == null) {
            s3Presigner = S3Presigner.builder()
                    .region(getRegion())
                    .credentialsProvider(
                            DefaultCredentialsProvider.builder().build()
                    )
                    .build();
        }
        return s3Presigner;
    }

    private Region getRegion() {
        String regionName = environment.getProperty(REGION_ENVIRONMENT_KEY);
        if (regionName == null || regionName.isBlank()) {
            throw new BusinessException(ErrorCode.TIME_CAPSULE_STORAGE_UNAVAILABLE);
        }

        return Region.of(regionName);
    }

    @Override
    public synchronized void destroy() {
        if (s3Client != null) {
            s3Client.close();
        }
        if (s3Presigner != null) {
            s3Presigner.close();
        }
    }
}
