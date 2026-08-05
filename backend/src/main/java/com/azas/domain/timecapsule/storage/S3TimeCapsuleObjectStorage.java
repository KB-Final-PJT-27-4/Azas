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
    // [JMG] CAPSULE-7 프런트에 AWS 자격 증명을 주지 않고 MIME 타입이 고정된 PUT URL만 발급한다.
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
    // [JMG] CAPSULE-4 S3 객체 키를 외부에 노출하지 않고 임시 GET URL로만 썸네일 접근을 허용한다.
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
    // [JMG] CAPSULE-8 HeadObject 결과로 실제 업로드 객체의 메타데이터를 신뢰 가능한 서버 측에서 검증한다.
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

    // [JMG] CAPSULE-7 환경변수가 없는 로컬 실행에서는 미디어 요청에만 명확한 저장소 오류를 반환한다.
    private String getBucketName() {
        String bucketName = environment.getProperty(BUCKET_ENVIRONMENT_KEY);
        if (bucketName == null || bucketName.isBlank()) {
            throw new BusinessException(ErrorCode.TIME_CAPSULE_STORAGE_UNAVAILABLE);
        }

        return bucketName;
    }

    // [JMG] CAPSULE-7 애플리케이션당 하나의 S3 클라이언트를 지연 생성해 로컬 부팅을 환경변수에 의존시키지 않는다.
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

    // [JMG] CAPSULE-7 애플리케이션당 하나의 Presigner를 지연 생성해 임시 URL 발급 비용을 줄인다.
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

    // [JMG] CAPSULE-7 S3 리전을 명시 환경변수에서 읽어 다른 리전에 객체가 생성되는 사고를 막는다.
    private Region getRegion() {
        String regionName = environment.getProperty(REGION_ENVIRONMENT_KEY);
        if (regionName == null || regionName.isBlank()) {
            throw new BusinessException(ErrorCode.TIME_CAPSULE_STORAGE_UNAVAILABLE);
        }

        return Region.of(regionName);
    }

    // [JMG] CAPSULE-7 애플리케이션 종료 시 S3 클라이언트가 잡고 있는 네트워크 리소스를 해제한다.
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
