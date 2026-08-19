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
        TimeCapsuleEntryDetailResponse.MediaResponse media =
                timeCapsuleMediaMapper
                        .findActiveByEntryId(timeCapsuleEntryId)
                        .stream()
                        .findFirst()
                        .map(currentMedia ->
                                toEntryDetailMediaResponse(
                                        currentMedia,
                                        expiresAt
                                )
                        )
                        .orElse(null);

        return new TimeCapsuleEntryDetailResponse(entry, media);
    }

    @Transactional
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
    public TimeCapsuleEntrySealResponse sealTimeCapsuleEntry(
            long requesterMemberId,
            long timeCapsuleEntryId
    ) {
        if (timeCapsuleEntryId < 1) {
            throw new BusinessException(ErrorCode.BADREQUEST);
        }

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
    public CompleteTimeCapsuleMediaUploadResponse completeMediaUpload(
            long requesterMemberId,
            long timeCapsuleEntryId,
            CompleteTimeCapsuleMediaUploadRequest request
    ) {
        if (timeCapsuleEntryId < 1
                || request == null
                || request.getTimeCapsuleMediaId() == null
                || request.getTimeCapsuleMediaId() < 1) {
            throw new BusinessException(ErrorCode.BADREQUEST);
        }

        TimeCapsuleEntry entry = getOwnedTimeCapsuleEntryForUpdateOrThrow(
                requesterMemberId,
                timeCapsuleEntryId
        );
        assertDraftEntry(entry);

        long timeCapsuleMediaId = request.getTimeCapsuleMediaId();
        TimeCapsuleMedia media =
                timeCapsuleMediaMapper.findByEntryIdAndIdForUpdate(
                        timeCapsuleEntryId,
                        timeCapsuleMediaId
                );
        if (media == null) {
            throw new BusinessException(ErrorCode.TIME_CAPSULE_MEDIA_NOT_FOUND);
        }

        if (entry.getMediaMode() != TimeCapsuleEntryMediaMode.IMAGE) {
            throw new BusinessException(
                    ErrorCode.TIME_CAPSULE_MEDIA_UPLOAD_NOT_ALLOWED
            );
        }

        if (media.getStatus() == TimeCapsuleMediaStatus.ACTIVE) {
            return new CompleteTimeCapsuleMediaUploadResponse(media);
        }
        if (media.getStatus() != TimeCapsuleMediaStatus.PENDING_UPLOAD) {
            throw new BusinessException(
                    ErrorCode.TIME_CAPSULE_MEDIA_UPLOAD_NOT_ALLOWED
            );
        }

        TimeCapsuleObjectStorage.StoredObjectMetadata metadata =
                timeCapsuleObjectStorage.getObjectMetadata(
                        media.getObjectKey()
                );
        if (!media.matchesUploadedObject(
                metadata.mimeType(),
                metadata.fileSize()
        )) {
            throw new BusinessException(
                    ErrorCode.TIME_CAPSULE_MEDIA_OBJECT_INVALID
            );
        }

        if (timeCapsuleMediaMapper.activatePendingMedia(timeCapsuleMediaId)
                != 1) {
            throw new BusinessException(
                    ErrorCode.TIME_CAPSULE_MEDIA_UPLOAD_NOT_ALLOWED
            );
        }
        media.activate();
        timeCapsuleMediaMapper.setThumbnailIfAbsent(
                timeCapsuleEntryId,
                media.getObjectKey()
        );

        return new CompleteTimeCapsuleMediaUploadResponse(media);
    }

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

    private void assertEligibleContributionTransaction(
            TimeCapsuleEntryTransaction transaction
    ) {
        if (!transaction.isCredit() || !transaction.hasPositiveAmount()) {
            throw new BusinessException(
                    ErrorCode.INELIGIBLE_TIME_CAPSULE_TRANSACTION
            );
        }
    }

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

    private void assertDraftEntry(TimeCapsuleEntry entry) {
        if (!entry.isDraft()) {
            throw new BusinessException(
                    ErrorCode.TIME_CAPSULE_ENTRY_MODIFICATION_NOT_ALLOWED
            );
        }
    }

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
        boolean hasRepresentativeImage =
                entry.getThumbnailObjectKey() != null
                        && !entry.getThumbnailObjectKey().isBlank();

        boolean isValid =
                entry.getMediaMode() == TimeCapsuleEntryMediaMode.IMAGE
                        && activeImageCount == 1
                        && hasRepresentativeImage;

        if (!isValid) {
            throw new BusinessException(
                    ErrorCode.TIME_CAPSULE_ENTRY_MEDIA_REQUIREMENT_NOT_MET
            );
        }
    }

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

    private String getFileExtension(String mimeType) {
        return switch (mimeType.toLowerCase(Locale.ROOT)) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> throw new BusinessException(ErrorCode.BADREQUEST);
        };
    }

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
