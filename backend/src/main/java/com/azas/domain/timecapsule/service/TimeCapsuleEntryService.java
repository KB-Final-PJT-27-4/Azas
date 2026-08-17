package com.azas.domain.timecapsule.service;

import com.azas.domain.timecapsule.dto.CreateTimeCapsuleEntryRequest;
import com.azas.domain.timecapsule.dto.CreateTimeCapsuleEntryResponse;
import com.azas.domain.timecapsule.dto.TimeCapsuleEntryDetailResponse;
import com.azas.domain.timecapsule.dto.TimeCapsuleEntryListResponse;
import com.azas.domain.timecapsule.dto.TimeCapsuleEntrySealResponse;
import com.azas.domain.timecapsule.dto.TimeCapsuleEntrySummaryResponse;
import com.azas.domain.timecapsule.dto.TimeCapsuleSummaryResponse;
import com.azas.domain.timecapsule.dto.CompleteTimeCapsuleMediaUploadRequest;
import com.azas.domain.timecapsule.dto.CompleteTimeCapsuleMediaUploadResponse;
import com.azas.domain.timecapsule.dto.CreateTimeCapsuleMediaUploadUrlRequest;
import com.azas.domain.timecapsule.dto.CreateTimeCapsuleMediaUploadUrlResponse;
import com.azas.domain.timecapsule.entity.TimeCapsule;
import com.azas.domain.timecapsule.entity.TimeCapsuleEntry;
import com.azas.domain.timecapsule.entity.TimeCapsuleEntryMediaMode;
import com.azas.domain.timecapsule.entity.TimeCapsuleEntryTransaction;
import com.azas.domain.timecapsule.entity.TimeCapsuleMediaType;
import com.azas.domain.timecapsule.entity.TimeCapsuleMedia;
import com.azas.domain.timecapsule.entity.TimeCapsuleMediaStatus;
import com.azas.domain.timecapsule.entity.TimeCapsuleStatus;
import com.azas.domain.timecapsule.mapper.TimeCapsuleEntryMapper;
import com.azas.domain.timecapsule.mapper.TimeCapsuleMediaMapper;
import com.azas.domain.timecapsule.mapper.TimeCapsuleMapper;
import com.azas.domain.timecapsule.storage.TimeCapsuleObjectStorage;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimeCapsuleEntryService {

    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");
    private static final Duration UPLOAD_URL_VALIDITY = Duration.ofMinutes(15);
    private static final Duration DOWNLOAD_URL_VALIDITY = Duration.ofMinutes(10);
    private static final long MAX_IMAGE_FILE_SIZE = 10L * 1024 * 1024;
    private static final int REPRESENTATIVE_IMAGE_SLOT_NO = 1;

    private final TimeCapsuleMapper timeCapsuleMapper;
    private final TimeCapsuleEntryMapper timeCapsuleEntryMapper;
    private final TimeCapsuleMediaMapper timeCapsuleMediaMapper;
    private final TimeCapsuleObjectStorage timeCapsuleObjectStorage;

    @Transactional(readOnly = true)
    // [JMG] CAPSULE-4 부모 권한을 확인한 뒤 보관함 요약과 봉인된 엔트리 목록을 조회한다.
    public TimeCapsuleEntryListResponse getTimeCapsuleEntries(
            long requesterMemberId,
            long timeCapsuleId
    ) {
        if (timeCapsuleId < 1) {
            throw new BusinessException(ErrorCode.BADREQUEST);
        }

        TimeCapsule timeCapsule = getAccessibleTimeCapsuleOrThrow(
                requesterMemberId,
                timeCapsuleId
        );

        List<TimeCapsuleEntrySummaryResponse> entries =
                timeCapsuleEntryMapper
                        .findSealedEntriesByTimeCapsuleId(timeCapsuleId)
                        .stream()
                        .map(this::toEntrySummaryResponse)
                        .collect(Collectors.toList());

        return new TimeCapsuleEntryListResponse(
                TimeCapsuleSummaryResponse.from(
                        timeCapsule,
                        LocalDate.now(SERVICE_ZONE)
                ),
                entries
        );
    }

    @Transactional(readOnly = true)
    // [JMG] CAPSULE-14 부모·보호자 권한을 확인한 뒤 엔트리와 활성 미디어의 임시 조회 URL을 반환한다.
    public TimeCapsuleEntryDetailResponse getTimeCapsuleEntry(
            long requesterMemberId,
            long timeCapsuleEntryId
    ) {
        TimeCapsuleEntry entry = getAccessibleTimeCapsuleEntryOrThrow(
                requesterMemberId,
                timeCapsuleEntryId
        );
        LocalDateTime expiresAt = LocalDateTime.now()
                .plus(DOWNLOAD_URL_VALIDITY);
        List<TimeCapsuleEntryDetailResponse.MediaResponse> media =
                timeCapsuleMediaMapper
                        .findActiveByEntryId(timeCapsuleEntryId)
                        .stream()
                        .map(currentMedia ->
                                toEntryDetailMediaResponse(
                                        currentMedia,
                                        expiresAt
                                )
                        )
                        .collect(Collectors.toList());

        return new TimeCapsuleEntryDetailResponse(entry, media);
    }

    @Transactional
    // [JMG] CAPSULE-13 작성자 본인의 DRAFT 엔트리와 연결 미디어를 S3·DB에서 삭제 상태로 처리한다.
    public void deleteTimeCapsuleEntry(
            long requesterMemberId,
            long timeCapsuleEntryId
    ) {
        TimeCapsuleEntry entry = getOwnedTimeCapsuleEntryForUpdateOrThrow(
                requesterMemberId,
                timeCapsuleEntryId
        );
        assertDraftEntry(entry);

        List<TimeCapsuleMedia> media =
                timeCapsuleMediaMapper.findNotDeletedByEntryIdForUpdate(
                        timeCapsuleEntryId
                );
        for (TimeCapsuleMedia currentMedia : media) {
            timeCapsuleObjectStorage.deleteObject(
                    currentMedia.getObjectKey()
            );
        }

        if (timeCapsuleMediaMapper.markNotDeletedMediaAsDeleted(
                timeCapsuleEntryId
        ) != media.size()
                || timeCapsuleEntryMapper.markDraftEntryAsDeleted(
                timeCapsuleEntryId
        ) != 1) {
            throw new BusinessException(
                    ErrorCode.TIME_CAPSULE_ENTRY_MODIFICATION_NOT_ALLOWED
            );
        }
    }

    @Transactional
    // [JMG] CAPSULE-5 부모가 선택한 타임캡슐 계좌의 입금 거래와 작성 내용으로 DRAFT 기록을 생성한다.
    public CreateTimeCapsuleEntryResponse createTimeCapsuleEntry(
            long requesterMemberId,
            long timeCapsuleId,
            CreateTimeCapsuleEntryRequest request
    ) {
        if (timeCapsuleId < 1) {
            throw new BusinessException(ErrorCode.BADREQUEST);
        }

        TimeCapsule timeCapsule =
                timeCapsuleMapper.findAccessibleByIdForUpdate(
                        timeCapsuleId,
                        requesterMemberId
                );
        if (timeCapsule == null) {
            throw new BusinessException(ErrorCode.TIME_CAPSULE_NOT_FOUND);
        }
        assertCollectingTimeCapsule(timeCapsule);

        TimeCapsuleEntry existingEntry =
                timeCapsuleEntryMapper.findByTimeCapsuleAndTransactionId(
                        timeCapsuleId,
                        request.getAccountTransactionId()
                );
        if (existingEntry != null) {
            throw new BusinessException(
                    ErrorCode.DUPLICATE_TIME_CAPSULE_ENTRY
            );
        }

        TimeCapsuleEntryTransaction transaction =
                getContributionTransactionOrThrow(
                        timeCapsule.getFinancialAccountId(),
                        request.getAccountTransactionId()
                );
        assertEligibleContributionTransaction(transaction);

        TimeCapsuleEntry entry = TimeCapsuleEntry.createDraft(
                timeCapsuleId,
                requesterMemberId,
                transaction,
                request.getTrimmedTitle(),
                request.getTrimmedMessage()
        );
        insertEntryOrThrow(entry);

        return CreateTimeCapsuleEntryResponse.from(
                getOwnedTimeCapsuleEntryOrThrow(requesterMemberId,
                        entry.getTimeCapsuleEntryId())
        );
    }

    @Transactional
    // [JMG] CAPSULE-15 미디어 조건을 충족한 DRAFT 엔트리를 작성자 본인이 봉인한다.
    public TimeCapsuleEntrySealResponse sealTimeCapsuleEntry(
            long requesterMemberId,
            long timeCapsuleEntryId
    ) {
        TimeCapsuleEntry entry = getOwnedTimeCapsuleEntryForUpdateOrThrow(
                requesterMemberId,
                timeCapsuleEntryId
        );
        assertDraftEntry(entry);
        assertMediaRequirementsForSeal(entry);

        if (timeCapsuleEntryMapper.sealDraftEntry(timeCapsuleEntryId) != 1
                || timeCapsuleEntryMapper.increaseEntryAggregates(entry) != 1) {
            throw new BusinessException(
                    ErrorCode.TIME_CAPSULE_ENTRY_MODIFICATION_NOT_ALLOWED
            );
        }

        return TimeCapsuleEntrySealResponse.from(
                getOwnedTimeCapsuleEntryOrThrow(
                        requesterMemberId,
                        timeCapsuleEntryId
                )
        );
    }

    @Transactional
    // [JMG] CAPSULE-7 DRAFT 엔트리에 대표 이미지 한 장의 서버 객체 키와 S3 Presigned PUT URL을 발급한다.
    public CreateTimeCapsuleMediaUploadUrlResponse
    createMediaUploadUrl(
            long requesterMemberId,
            long timeCapsuleEntryId,
            CreateTimeCapsuleMediaUploadUrlRequest request
    ) {
        TimeCapsuleEntry entry = getOwnedTimeCapsuleEntryForUpdateOrThrow(
                requesterMemberId,
                timeCapsuleEntryId
        );
        assertDraftEntry(entry);
        String mimeType = request.normalizedMimeType();
        assertValidRepresentativeImageRequest(request, mimeType);
        assertEntryCanReceiveRepresentativeImage(entry);

        TimeCapsuleMedia media = TimeCapsuleMedia.createPendingUpload(
                timeCapsuleEntryId,
                TimeCapsuleMediaType.IMAGE,
                createObjectKey(
                        entry,
                        mimeType,
                        REPRESENTATIVE_IMAGE_SLOT_NO
                ),
                mimeType,
                request.getFileSize(),
                REPRESENTATIVE_IMAGE_SLOT_NO
        );

        try {
            if (timeCapsuleMediaMapper.insert(media) != 1) {
                throw new BusinessException(
                        ErrorCode.TIME_CAPSULE_MEDIA_UPLOAD_NOT_ALLOWED
                );
            }
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(
                    ErrorCode.TIME_CAPSULE_MEDIA_UPLOAD_NOT_ALLOWED,
                    exception
            );
        }

        TimeCapsuleObjectStorage.PresignedUrl presignedUrl =
                timeCapsuleObjectStorage.createUploadUrl(
                        media.getObjectKey(),
                        media.getMimeType(),
                        UPLOAD_URL_VALIDITY
                );

        return new CreateTimeCapsuleMediaUploadUrlResponse(
                timeCapsuleEntryId,
                media.getTimeCapsuleMediaId(),
                presignedUrl.url(),
                LocalDateTime.now().plus(UPLOAD_URL_VALIDITY),
                Map.of("Content-Type", media.getMimeType())
        );
    }

    @Transactional
    // [JMG] CAPSULE-8 S3 메타데이터를 검증한 뒤 PENDING_UPLOAD 미디어를 ACTIVE로 전환한다.
    public CompleteTimeCapsuleMediaUploadResponse completeMediaUpload(
            long requesterMemberId,
            long timeCapsuleEntryId,
            CompleteTimeCapsuleMediaUploadRequest request
    ) {
        TimeCapsuleEntry entry = getOwnedTimeCapsuleEntryForUpdateOrThrow(
                requesterMemberId,
                timeCapsuleEntryId
        );
        assertDraftEntry(entry);

        List<Long> mediaIds = getDistinctMediaIdsOrThrow(request);
        List<TimeCapsuleMedia> media =
                timeCapsuleMediaMapper.findByEntryIdAndIdsForUpdate(
                        timeCapsuleEntryId,
                        mediaIds
                );
        if (media.size() != mediaIds.size()) {
            throw new BusinessException(ErrorCode.TIME_CAPSULE_MEDIA_NOT_FOUND);
        }

        TimeCapsuleMediaType expectedMediaType = getMediaType(entry);
        for (TimeCapsuleMedia currentMedia : media) {
            if (currentMedia.getStatus()
                    != TimeCapsuleMediaStatus.PENDING_UPLOAD
                    || currentMedia.getMediaType() != expectedMediaType) {
                throw new BusinessException(
                        ErrorCode.TIME_CAPSULE_MEDIA_UPLOAD_NOT_ALLOWED
                );
            }

            TimeCapsuleObjectStorage.StoredObjectMetadata metadata =
                    timeCapsuleObjectStorage.getObjectMetadata(
                            currentMedia.getObjectKey()
                    );
            if (!currentMedia.matchesUploadedObject(
                    metadata.mimeType(),
                    metadata.fileSize()
            )) {
                throw new BusinessException(
                        ErrorCode.TIME_CAPSULE_MEDIA_OBJECT_INVALID
                );
            }
        }

        if (timeCapsuleMediaMapper.activatePendingMedia(mediaIds)
                != mediaIds.size()) {
            throw new BusinessException(
                    ErrorCode.TIME_CAPSULE_MEDIA_UPLOAD_NOT_ALLOWED
            );
        }
        media.forEach(TimeCapsuleMedia::activate);

        boolean thumbnailReady = entry.getThumbnailObjectKey() != null;
        if (expectedMediaType == TimeCapsuleMediaType.IMAGE) {
            timeCapsuleMediaMapper.setThumbnailIfAbsent(
                    timeCapsuleEntryId,
                    media.get(0).getObjectKey()
            );
            thumbnailReady = true;
        }

        return new CompleteTimeCapsuleMediaUploadResponse(
                entry,
                timeCapsuleMediaMapper.countActiveByEntryId(timeCapsuleEntryId),
                thumbnailReady,
                media
        );
    }

    // [JMG] CAPSULE-4 부모에게 접근 가능한 보관함만 반환해 보관함 존재 여부를 보호한다.
    private TimeCapsule getAccessibleTimeCapsuleOrThrow(
            long requesterMemberId,
            long timeCapsuleId
    ) {
        TimeCapsule timeCapsule = timeCapsuleMapper.findAccessibleById(
                timeCapsuleId,
                requesterMemberId
        );

        if (timeCapsule == null) {
            throw new BusinessException(ErrorCode.TIME_CAPSULE_NOT_FOUND);
        }

        return timeCapsule;
    }

    // [JMG] CAPSULE-14 부모·보호자 관계가 있고 삭제되지 않은 엔트리만 상세 조회 대상으로 반환한다.
    private TimeCapsuleEntry getAccessibleTimeCapsuleEntryOrThrow(
            long requesterMemberId,
            long timeCapsuleEntryId
    ) {
        TimeCapsuleEntry entry = timeCapsuleEntryMapper.findAccessibleById(
                timeCapsuleEntryId,
                requesterMemberId
        );
        if (entry == null) {
            throw new BusinessException(ErrorCode.TIME_CAPSULE_ENTRY_NOT_FOUND);
        }

        return entry;
    }

    // [JMG] CAPSULE-7 작성자이면서 자녀와 연결된 부모에게만 엔트리를 노출한다.
    private TimeCapsuleEntry getOwnedTimeCapsuleEntryOrThrow(
            long requesterMemberId,
            long timeCapsuleEntryId
    ) {
        TimeCapsuleEntry entry = timeCapsuleEntryMapper.findOwnedById(
                timeCapsuleEntryId,
                requesterMemberId
        );
        if (entry == null) {
            throw new BusinessException(ErrorCode.TIME_CAPSULE_ENTRY_NOT_FOUND);
        }

        return entry;
    }

    // [JMG] CAPSULE-13·15 삭제·봉인 직전 엔트리 행을 잠가 상태 변경 경쟁 조건을 방지한다.
    private TimeCapsuleEntry getOwnedTimeCapsuleEntryForUpdateOrThrow(
            long requesterMemberId,
            long timeCapsuleEntryId
    ) {
        TimeCapsuleEntry entry =
                timeCapsuleEntryMapper.findOwnedByIdForUpdate(
                        timeCapsuleEntryId,
                        requesterMemberId
                );
        if (entry == null) {
            throw new BusinessException(ErrorCode.TIME_CAPSULE_ENTRY_NOT_FOUND);
        }

        return entry;
    }

    // [JMG] CAPSULE-5 대상 타임캡슐 계좌에 실제로 기록된 거래만 조회해 임의 거래 연결을 차단한다.
    private TimeCapsuleEntryTransaction
    getContributionTransactionOrThrow(
            long financialAccountId,
            long accountTransactionId
    ) {
        TimeCapsuleEntryTransaction transaction =
                timeCapsuleEntryMapper.findTransactionByFinancialAccountId(
                        financialAccountId,
                        accountTransactionId
                );

        if (transaction == null) {
            throw new BusinessException(
                    ErrorCode.ACCOUNT_TRANSACTION_NOT_FOUND
            );
        }

        return transaction;
    }

    // [JMG] CAPSULE-5 출금·0원·음수 거래를 타임캡슐 저축 기록으로 사용하지 못하게 검증한다.
    private void assertEligibleContributionTransaction(
            TimeCapsuleEntryTransaction transaction
    ) {
        if (!transaction.isCredit() || !transaction.hasPositiveAmount()) {
            throw new BusinessException(
                    ErrorCode.INELIGIBLE_TIME_CAPSULE_TRANSACTION
            );
        }
    }

    // [JMG] CAPSULE-5 수집 중이고 공개일이 지나지 않은 보관함에만 새 기록을 허용한다.
    private void assertCollectingTimeCapsule(TimeCapsule timeCapsule) {
        LocalDateTime releaseAt = timeCapsule.getExpectedReleaseAt();
        if (timeCapsule.getStatus() != TimeCapsuleStatus.COLLECTING
                || (releaseAt != null
                && !releaseAt.isAfter(LocalDateTime.now(SERVICE_ZONE)))) {
            throw new BusinessException(
                    ErrorCode.TIME_CAPSULE_ENTRY_CREATION_NOT_ALLOWED
            );
        }
    }

    // [JMG] CAPSULE-4 썸네일 객체가 있는 엔트리에만 S3 임시 다운로드 URL을 발급한다.
    private TimeCapsuleEntrySummaryResponse toEntrySummaryResponse(
            TimeCapsuleEntry entry
    ) {
        if (entry.getThumbnailObjectKey() == null) {
            return TimeCapsuleEntrySummaryResponse.from(entry);
        }

        TimeCapsuleObjectStorage.PresignedUrl presignedUrl =
                timeCapsuleObjectStorage.createDownloadUrl(
                        entry.getThumbnailObjectKey(),
                        DOWNLOAD_URL_VALIDITY
                );
        return TimeCapsuleEntrySummaryResponse.from(
                entry,
                presignedUrl.url(),
                LocalDateTime.now().plus(DOWNLOAD_URL_VALIDITY)
        );
    }

    // [JMG] CAPSULE-14 활성 미디어 하나를 객체 키 없이 Presigned GET URL이 포함된 상세 응답 항목으로 변환한다.
    private TimeCapsuleEntryDetailResponse.MediaResponse
    toEntryDetailMediaResponse(
            TimeCapsuleMedia media,
            LocalDateTime expiresAt
    ) {
        TimeCapsuleObjectStorage.PresignedUrl presignedUrl =
                timeCapsuleObjectStorage.createDownloadUrl(
                        media.getObjectKey(),
                        DOWNLOAD_URL_VALIDITY
                );
        return new TimeCapsuleEntryDetailResponse.MediaResponse(
                media,
                presignedUrl.url(),
                expiresAt
        );
    }

    // [JMG] CAPSULE-13·15 봉인 또는 삭제된 엔트리의 변경 시도를 상태 충돌 오류로 처리한다.
    private void assertDraftEntry(TimeCapsuleEntry entry) {
        if (!entry.isDraft()) {
            throw new BusinessException(
                    ErrorCode.TIME_CAPSULE_ENTRY_MODIFICATION_NOT_ALLOWED
            );
        }
    }

    // [JMG] CAPSULE-15 엔트리의 미디어 유형별 활성 개수와 업로드 완료 상태를 봉인 전에 검증한다.
    private void assertMediaRequirementsForSeal(TimeCapsuleEntry entry) {
        if (timeCapsuleEntryMapper.countPendingMediaByEntryId(
                entry.getTimeCapsuleEntryId()
        ) > 0) {
            throw new BusinessException(
                    ErrorCode.TIME_CAPSULE_ENTRY_MEDIA_REQUIREMENT_NOT_MET
            );
        }

        int activeImageCount =
                timeCapsuleEntryMapper.countActiveMediaByEntryIdAndType(
                        entry.getTimeCapsuleEntryId(),
                        TimeCapsuleMediaType.IMAGE
                );
        int activeVideoCount =
                timeCapsuleEntryMapper.countActiveMediaByEntryIdAndType(
                        entry.getTimeCapsuleEntryId(),
                        TimeCapsuleMediaType.VIDEO
                );

        boolean isValid = entry.getMediaMode()
                == TimeCapsuleEntryMediaMode.IMAGE
                && activeImageCount == 1
                && activeVideoCount == 0;
        if (!isValid) {
            throw new BusinessException(
                    ErrorCode.TIME_CAPSULE_ENTRY_MEDIA_REQUIREMENT_NOT_MET
            );
        }
    }

    // [JMG] CAPSULE-7 엔트리의 IMAGE·VIDEO·NONE 미디어 모드를 실제 S3 미디어 유형으로 변환한다.
    private TimeCapsuleMediaType getMediaType(TimeCapsuleEntry entry) {
        return switch (entry.getMediaMode()) {
            case IMAGE -> TimeCapsuleMediaType.IMAGE;
            case VIDEO -> TimeCapsuleMediaType.VIDEO;
            case NONE -> throw new BusinessException(
                    ErrorCode.TIME_CAPSULE_MEDIA_UPLOAD_NOT_ALLOWED
            );
        };
    }

    // [JMG] CAPSULE-7 NONE 초안은 대표 이미지 업로드를 시작할 때 IMAGE 모드로 한 번만 전환한다.
    private void assertEntryCanReceiveRepresentativeImage(
            TimeCapsuleEntry entry
    ) {
        if (timeCapsuleMediaMapper.countByEntryIdAndSlotNo(
                entry.getTimeCapsuleEntryId(),
                REPRESENTATIVE_IMAGE_SLOT_NO
        ) > 0) {
            throw new BusinessException(
                    ErrorCode.TIME_CAPSULE_MEDIA_UPLOAD_NOT_ALLOWED
            );
        }

        if (entry.getMediaMode() == TimeCapsuleEntryMediaMode.NONE) {
            if (timeCapsuleEntryMapper.updateDraftMediaModeIfNone(
                    entry.getTimeCapsuleEntryId(),
                    TimeCapsuleEntryMediaMode.IMAGE
            ) != 1) {
                throw new BusinessException(
                        ErrorCode.TIME_CAPSULE_MEDIA_UPLOAD_NOT_ALLOWED
                );
            }
            return;
        }

        if (entry.getMediaMode() != TimeCapsuleEntryMediaMode.IMAGE) {
            throw new BusinessException(
                    ErrorCode.TIME_CAPSULE_MEDIA_UPLOAD_NOT_ALLOWED
            );
        }
    }

    // [JMG] CAPSULE-7 대표 이미지는 JPEG·PNG·WebP와 최대 10MiB만 허용한다.
    private void assertValidRepresentativeImageRequest(
            CreateTimeCapsuleMediaUploadUrlRequest request,
            String mimeType
    ) {
        if (!Set.of("image/jpeg", "image/png", "image/webp")
                .contains(mimeType)
                || request.getFileSize() == null
                || request.getFileSize() < 1
                || request.getFileSize() > MAX_IMAGE_FILE_SIZE) {
            throw new BusinessException(ErrorCode.BADREQUEST);
        }
    }

    // [JMG] CAPSULE-7 사용자 파일명 대신 UUID 기반 서버 객체 키를 만들어 경로 조작과 이름 충돌을 막는다.
    private String createObjectKey(
            TimeCapsuleEntry entry,
            String mimeType,
            int slotNo
    ) {
        return "time-capsules/" + entry.getTimeCapsuleId()
                + "/entries/" + entry.getTimeCapsuleEntryId()
                + "/" + UUID.randomUUID()
                + "/slot-" + slotNo + getFileExtension(mimeType);
    }

    // [JMG] CAPSULE-7 허용된 MIME 타입에 대응하는 서버 제어 확장자를 반환한다.
    private String getFileExtension(String mimeType) {
        return switch (mimeType.toLowerCase(Locale.ROOT)) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> throw new BusinessException(ErrorCode.BADREQUEST);
        };
    }

    // [JMG] CAPSULE-8 중복된 미디어 ID를 제거하지 않고 오류로 처리해 부분 완료를 방지한다.
    private List<Long> getDistinctMediaIdsOrThrow(
            CompleteTimeCapsuleMediaUploadRequest request
    ) {
        if (request.getMediaIds() == null || request.getMediaIds().isEmpty()) {
            throw new BusinessException(ErrorCode.BADREQUEST);
        }

        Set<Long> distinctIds = new HashSet<>(request.getMediaIds());
        if (distinctIds.size() != request.getMediaIds().size()) {
            throw new BusinessException(ErrorCode.BADREQUEST);
        }

        return List.copyOf(request.getMediaIds());
    }

    // [JMG] CAPSULE-5 DB 고유 제약과 재조회로 이체 이벤트 재시도에도 엔트리를 하나만 유지한다.
    private void insertEntryOrThrow(TimeCapsuleEntry entry) {
        try {
            if (timeCapsuleEntryMapper.insert(entry) != 1) {
                throw new BusinessException(
                        ErrorCode.TIME_CAPSULE_ENTRY_CREATION_NOT_ALLOWED
                );
            }
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(
                    ErrorCode.DUPLICATE_TIME_CAPSULE_ENTRY,
                    exception
            );
        }
    }
}
