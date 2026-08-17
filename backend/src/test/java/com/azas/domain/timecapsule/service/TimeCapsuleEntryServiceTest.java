package com.azas.domain.timecapsule.service;

import com.azas.domain.timecapsule.dto.CreateTimeCapsuleEntryRequest;
import com.azas.domain.timecapsule.dto.CreateTimeCapsuleEntryResponse;
import com.azas.domain.timecapsule.dto.CompleteTimeCapsuleMediaUploadRequest;
import com.azas.domain.timecapsule.dto.CompleteTimeCapsuleMediaUploadResponse;
import com.azas.domain.timecapsule.dto.CreateTimeCapsuleMediaUploadUrlRequest;
import com.azas.domain.timecapsule.dto.CreateTimeCapsuleMediaUploadUrlResponse;
import com.azas.domain.timecapsule.dto.TimeCapsuleEntryListResponse;
import com.azas.domain.timecapsule.dto.TimeCapsuleEntrySealResponse;
import com.azas.domain.timecapsule.entity.AccountTransactionDirection;
import com.azas.domain.timecapsule.entity.TimeCapsule;
import com.azas.domain.timecapsule.entity.TimeCapsuleEntry;
import com.azas.domain.timecapsule.entity.TimeCapsuleEntryMediaMode;
import com.azas.domain.timecapsule.entity.TimeCapsuleEntryStatus;
import com.azas.domain.timecapsule.entity.TimeCapsuleEntryTransaction;
import com.azas.domain.timecapsule.entity.TimeCapsuleMedia;
import com.azas.domain.timecapsule.entity.TimeCapsuleMediaStatus;
import com.azas.domain.timecapsule.entity.TimeCapsuleMediaType;
import com.azas.domain.timecapsule.entity.TimeCapsuleStatus;
import com.azas.domain.timecapsule.mapper.TimeCapsuleEntryMapper;
import com.azas.domain.timecapsule.mapper.TimeCapsuleMapper;
import com.azas.domain.timecapsule.mapper.TimeCapsuleMediaMapper;
import com.azas.domain.timecapsule.storage.TimeCapsuleObjectStorage;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TimeCapsuleEntryServiceTest {

    @Mock
    private TimeCapsuleMapper timeCapsuleMapper;
    @Mock
    private TimeCapsuleEntryMapper timeCapsuleEntryMapper;
    @Mock
    private TimeCapsuleMediaMapper timeCapsuleMediaMapper;
    @Mock
    private TimeCapsuleObjectStorage timeCapsuleObjectStorage;
    @InjectMocks
    private TimeCapsuleEntryService timeCapsuleEntryService;

    @Test
    void createMediaUploadUrlReturnsSingleRepresentativeImageUrl() {
        TimeCapsuleEntry draft = createEntry(
                1000L, 100L, 901L, AccountTransactionDirection.CREDIT,
                new BigDecimal("150000.00"), TimeCapsuleEntryStatus.DRAFT,
                TimeCapsuleEntryMediaMode.NONE
        );
        CreateTimeCapsuleMediaUploadUrlRequest request =
                createMediaUploadUrlRequest(" image/jpeg ", 1048576L);
        given(timeCapsuleEntryMapper.findOwnedByIdForUpdate(1000L, 7L))
                .willReturn(draft);
        given(timeCapsuleMediaMapper.countByEntryIdAndSlotNo(1000L, 1))
                .willReturn(0);
        given(timeCapsuleEntryMapper.updateDraftMediaModeIfNone(
                1000L, TimeCapsuleEntryMediaMode.IMAGE
        )).willReturn(1);
        given(timeCapsuleMediaMapper.insert(any(TimeCapsuleMedia.class)))
                .willAnswer(invocation -> {
                    TimeCapsuleMedia media = invocation.getArgument(0);
                    ReflectionTestUtils.setField(
                            media, "timeCapsuleMediaId", 2000L
                    );
                    return 1;
                });
        given(timeCapsuleObjectStorage.createUploadUrl(
                any(String.class),
                org.mockito.ArgumentMatchers.eq("image/jpeg"),
                org.mockito.ArgumentMatchers.eq(Duration.ofMinutes(15))
        )).willReturn(new TimeCapsuleObjectStorage.PresignedUrl(
                "https://storage.example/presigned-put"
        ));

        CreateTimeCapsuleMediaUploadUrlResponse response =
                timeCapsuleEntryService.createMediaUploadUrl(
                        7L, 1000L, request
                );

        assertEquals(1000L, response.getTimeCapsuleEntryId());
        assertEquals(2000L, response.getTimeCapsuleMediaId());
        assertEquals("https://storage.example/presigned-put",
                response.getUploadUrl());
        assertEquals("image/jpeg",
                response.getRequiredHeaders().get("Content-Type"));

        ArgumentCaptor<TimeCapsuleMedia> mediaCaptor =
                ArgumentCaptor.forClass(TimeCapsuleMedia.class);
        verify(timeCapsuleMediaMapper).insert(mediaCaptor.capture());
        TimeCapsuleMedia media = mediaCaptor.getValue();
        assertEquals(TimeCapsuleMediaType.IMAGE, media.getMediaType());
        assertEquals("image/jpeg", media.getMimeType());
        assertEquals(1048576L, media.getFileSize());
        assertEquals(1, media.getSlotNo());
        assertTrue(media.getObjectKey().startsWith(
                "time-capsules/100/entries/1000/"
        ));
        assertTrue(media.getObjectKey().endsWith("/slot-1.jpg"));
    }

    @Test
    void createMediaUploadUrlRejectsSecondRepresentativeImage() {
        TimeCapsuleEntry draft = createEntry(
                1000L, 100L, 901L, AccountTransactionDirection.CREDIT,
                new BigDecimal("150000.00"), TimeCapsuleEntryStatus.DRAFT,
                TimeCapsuleEntryMediaMode.IMAGE
        );
        CreateTimeCapsuleMediaUploadUrlRequest request =
                createMediaUploadUrlRequest("image/png", 2048L);
        given(timeCapsuleEntryMapper.findOwnedByIdForUpdate(1000L, 7L))
                .willReturn(draft);
        given(timeCapsuleMediaMapper.countByEntryIdAndSlotNo(1000L, 1))
                .willReturn(1);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> timeCapsuleEntryService.createMediaUploadUrl(
                        7L, 1000L, request
                )
        );

        assertEquals(ErrorCode.TIME_CAPSULE_MEDIA_UPLOAD_NOT_ALLOWED,
                exception.getErrorCode());
        verify(timeCapsuleMediaMapper, never())
                .insert(any(TimeCapsuleMedia.class));
        verify(timeCapsuleObjectStorage, never())
                .createUploadUrl(any(), any(), any());
    }

    @Test
    void createMediaUploadUrlRejectsVideoMimeType() {
        TimeCapsuleEntry draft = createEntry(
                1000L, 100L, 901L, AccountTransactionDirection.CREDIT,
                new BigDecimal("150000.00"), TimeCapsuleEntryStatus.DRAFT,
                TimeCapsuleEntryMediaMode.NONE
        );
        CreateTimeCapsuleMediaUploadUrlRequest request =
                createMediaUploadUrlRequest("video/mp4", 2048L);
        given(timeCapsuleEntryMapper.findOwnedByIdForUpdate(1000L, 7L))
                .willReturn(draft);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> timeCapsuleEntryService.createMediaUploadUrl(
                        7L, 1000L, request
                )
        );

        assertEquals(ErrorCode.BADREQUEST, exception.getErrorCode());
        verify(timeCapsuleMediaMapper, never())
                .insert(any(TimeCapsuleMedia.class));
    }

    @Test
    void completeMediaUploadActivatesSingleUploadedImage() {
        TimeCapsuleEntry draft = createEntry(
                1000L, 100L, 901L, AccountTransactionDirection.CREDIT,
                new BigDecimal("150000.00"), TimeCapsuleEntryStatus.DRAFT,
                TimeCapsuleEntryMediaMode.IMAGE
        );
        TimeCapsuleMedia media = createMedia(
                2000L, 1000L, TimeCapsuleMediaStatus.PENDING_UPLOAD
        );
        CompleteTimeCapsuleMediaUploadRequest request =
                createMediaUploadCompleteRequest(2000L);
        given(timeCapsuleEntryMapper.findOwnedByIdForUpdate(1000L, 7L))
                .willReturn(draft);
        given(timeCapsuleMediaMapper.findByEntryIdAndIdForUpdate(
                1000L, 2000L
        )).willReturn(media);
        given(timeCapsuleObjectStorage.getObjectMetadata(
                media.getObjectKey()
        )).willReturn(new TimeCapsuleObjectStorage.StoredObjectMetadata(
                "image/jpeg", 1048576L
        ));
        given(timeCapsuleMediaMapper.activatePendingMedia(2000L))
                .willReturn(1);

        CompleteTimeCapsuleMediaUploadResponse response =
                timeCapsuleEntryService.completeMediaUpload(
                        7L, 1000L, request
                );

        assertEquals(1000L, response.getTimeCapsuleEntryId());
        assertEquals(2000L, response.getTimeCapsuleMediaId());
        assertEquals("ACTIVE", response.getMediaStatus());
        verify(timeCapsuleMediaMapper).setThumbnailIfAbsent(
                1000L, media.getObjectKey()
        );
    }

    @Test
    void completeMediaUploadIsIdempotentForActiveImage() {
        TimeCapsuleEntry draft = createEntry(
                1000L, 100L, 901L, AccountTransactionDirection.CREDIT,
                new BigDecimal("150000.00"), TimeCapsuleEntryStatus.DRAFT,
                TimeCapsuleEntryMediaMode.IMAGE
        );
        TimeCapsuleMedia media = createMedia(
                2000L, 1000L, TimeCapsuleMediaStatus.ACTIVE
        );
        given(timeCapsuleEntryMapper.findOwnedByIdForUpdate(1000L, 7L))
                .willReturn(draft);
        given(timeCapsuleMediaMapper.findByEntryIdAndIdForUpdate(
                1000L, 2000L
        )).willReturn(media);

        CompleteTimeCapsuleMediaUploadResponse response =
                timeCapsuleEntryService.completeMediaUpload(
                        7L,
                        1000L,
                        createMediaUploadCompleteRequest(2000L)
                );

        assertEquals("ACTIVE", response.getMediaStatus());
        verify(timeCapsuleObjectStorage, never())
                .getObjectMetadata(any(String.class));
        verify(timeCapsuleMediaMapper, never()).activatePendingMedia(2000L);
        verify(timeCapsuleMediaMapper, never())
                .setThumbnailIfAbsent(
                        org.mockito.ArgumentMatchers.anyLong(),
                        any(String.class)
                );
    }

    @Test
    void completeMediaUploadRejectsMismatchedStoredObject() {
        TimeCapsuleEntry draft = createEntry(
                1000L, 100L, 901L, AccountTransactionDirection.CREDIT,
                new BigDecimal("150000.00"), TimeCapsuleEntryStatus.DRAFT,
                TimeCapsuleEntryMediaMode.IMAGE
        );
        TimeCapsuleMedia media = createMedia(
                2000L, 1000L, TimeCapsuleMediaStatus.PENDING_UPLOAD
        );
        given(timeCapsuleEntryMapper.findOwnedByIdForUpdate(1000L, 7L))
                .willReturn(draft);
        given(timeCapsuleMediaMapper.findByEntryIdAndIdForUpdate(
                1000L, 2000L
        )).willReturn(media);
        given(timeCapsuleObjectStorage.getObjectMetadata(
                media.getObjectKey()
        )).willReturn(new TimeCapsuleObjectStorage.StoredObjectMetadata(
                "image/png", 1048576L
        ));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> timeCapsuleEntryService.completeMediaUpload(
                        7L,
                        1000L,
                        createMediaUploadCompleteRequest(2000L)
                )
        );

        assertEquals(ErrorCode.TIME_CAPSULE_MEDIA_OBJECT_INVALID,
                exception.getErrorCode());
        verify(timeCapsuleMediaMapper, never()).activatePendingMedia(2000L);
    }

    @Test
    void getTimeCapsuleEntriesReturnsOnlySealedEntries() {
        TimeCapsule timeCapsule = createTimeCapsule(
                100L, 4L, TimeCapsuleStatus.COLLECTING
        );
        TimeCapsuleEntry entry = createEntry(
                1000L, 100L, 901L, AccountTransactionDirection.CREDIT,
                new BigDecimal("100000.00"), TimeCapsuleEntryStatus.SEALED,
                TimeCapsuleEntryMediaMode.IMAGE
        );
        given(timeCapsuleMapper.findAccessibleById(100L, 7L))
                .willReturn(timeCapsule);
        given(timeCapsuleEntryMapper.findSealedEntriesByTimeCapsuleId(100L))
                .willReturn(List.of(entry));

        TimeCapsuleEntryListResponse response =
                timeCapsuleEntryService.getTimeCapsuleEntries(7L, 100L);

        assertEquals(1, response.getTotalCount());
        assertEquals(1000L,
                response.getEntries().get(0).getTimeCapsuleEntryId());
    }

    @Test
    void createTimeCapsuleEntryCreatesDraftFromSelectedCreditTransaction() {
        TimeCapsule timeCapsule = createTimeCapsule(
                100L, 4L, TimeCapsuleStatus.COLLECTING
        );
        TimeCapsuleEntryTransaction transaction = createTransaction(
                901L,
                AccountTransactionDirection.CREDIT,
                new BigDecimal("150000.00"),
                LocalDateTime.of(2026, 8, 16, 10, 30)
        );
        TimeCapsuleEntry persistedEntry = createEntry(
                1000L, 100L, 901L, AccountTransactionDirection.CREDIT,
                new BigDecimal("150000.00"), TimeCapsuleEntryStatus.DRAFT,
                TimeCapsuleEntryMediaMode.NONE
        );
        ReflectionTestUtils.setField(
                persistedEntry, "createdAt",
                LocalDateTime.of(2026, 8, 16, 10, 31)
        );
        given(timeCapsuleMapper.findAccessibleByIdForUpdate(100L, 7L))
                .willReturn(timeCapsule);
        given(timeCapsuleEntryMapper.findByTimeCapsuleAndTransactionId(
                100L, 901L
        )).willReturn(null);
        given(timeCapsuleEntryMapper.findTransactionByFinancialAccountId(
                4L, 901L
        )).willReturn(transaction);
        given(timeCapsuleEntryMapper.insert(any(TimeCapsuleEntry.class)))
                .willAnswer(invocation -> {
                    TimeCapsuleEntry inserted = invocation.getArgument(0);
                    ReflectionTestUtils.setField(
                            inserted, "timeCapsuleEntryId", 1000L
                    );
                    return 1;
                });
        given(timeCapsuleEntryMapper.findOwnedById(1000L, 7L))
                .willReturn(persistedEntry);

        CreateTimeCapsuleEntryResponse response =
                timeCapsuleEntryService.createTimeCapsuleEntry(
                        7L,
                        100L,
                        createRequest(
                                901L,
                                "  첫 용돈을 받은 날  ",
                                "  남은 돈은 꼭 저축하자.  "
                        )
                );

        ArgumentCaptor<TimeCapsuleEntry> captor =
                ArgumentCaptor.forClass(TimeCapsuleEntry.class);
        verify(timeCapsuleEntryMapper).insert(captor.capture());
        TimeCapsuleEntry inserted = captor.getValue();
        assertEquals("첫 용돈을 받은 날", inserted.getTitle());
        assertEquals("남은 돈은 꼭 저축하자.", inserted.getMessage());
        assertEquals(new BigDecimal("150000.00"),
                inserted.getContributionAmount());
        assertEquals(TimeCapsuleEntryStatus.DRAFT, inserted.getStatus());
        assertEquals(1000L, response.getTimeCapsuleEntryId());
        assertEquals("DRAFT", response.getStatus());
        verify(timeCapsuleEntryMapper, never()).increaseEntryAggregates(any());
    }

    @Test
    void createTimeCapsuleEntryRejectsTransactionAlreadyRecorded() {
        TimeCapsule timeCapsule = createTimeCapsule(
                100L, 4L, TimeCapsuleStatus.COLLECTING
        );
        given(timeCapsuleMapper.findAccessibleByIdForUpdate(100L, 7L))
                .willReturn(timeCapsule);
        given(timeCapsuleEntryMapper.findByTimeCapsuleAndTransactionId(
                100L, 901L
        )).willReturn(createEntry(
                1000L, 100L, 901L, AccountTransactionDirection.CREDIT,
                new BigDecimal("150000.00"), TimeCapsuleEntryStatus.DRAFT,
                TimeCapsuleEntryMediaMode.NONE
        ));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> timeCapsuleEntryService.createTimeCapsuleEntry(
                        7L, 100L,
                        createRequest(901L, "제목", "메시지")
                )
        );

        assertEquals(ErrorCode.DUPLICATE_TIME_CAPSULE_ENTRY,
                exception.getErrorCode());
        verify(timeCapsuleEntryMapper, never())
                .findTransactionByFinancialAccountId(4L, 901L);
    }

    @Test
    void createTimeCapsuleEntryRejectsDebitTransaction() {
        TimeCapsule timeCapsule = createTimeCapsule(
                100L, 4L, TimeCapsuleStatus.COLLECTING
        );
        given(timeCapsuleMapper.findAccessibleByIdForUpdate(100L, 7L))
                .willReturn(timeCapsule);
        given(timeCapsuleEntryMapper.findTransactionByFinancialAccountId(
                4L, 901L
        )).willReturn(createTransaction(
                901L, AccountTransactionDirection.DEBIT,
                new BigDecimal("150000.00"),
                LocalDateTime.of(2026, 8, 16, 10, 30)
        ));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> timeCapsuleEntryService.createTimeCapsuleEntry(
                        7L, 100L,
                        createRequest(901L, "제목", "메시지")
                )
        );

        assertEquals(ErrorCode.INELIGIBLE_TIME_CAPSULE_TRANSACTION,
                exception.getErrorCode());
        verify(timeCapsuleEntryMapper, never()).insert(any());
    }

    @Test
    void createTimeCapsuleEntryRejectsUnavailableTimeCapsule() {
        given(timeCapsuleMapper.findAccessibleByIdForUpdate(100L, 7L))
                .willReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> timeCapsuleEntryService.createTimeCapsuleEntry(
                        7L, 100L,
                        createRequest(901L, "제목", "메시지")
                )
        );

        assertEquals(ErrorCode.TIME_CAPSULE_NOT_FOUND,
                exception.getErrorCode());
    }

    @Test
    void createTimeCapsuleEntryMapsConcurrentDuplicateToConflict() {
        TimeCapsule timeCapsule = createTimeCapsule(
                100L, 4L, TimeCapsuleStatus.COLLECTING
        );
        given(timeCapsuleMapper.findAccessibleByIdForUpdate(100L, 7L))
                .willReturn(timeCapsule);
        given(timeCapsuleEntryMapper.findTransactionByFinancialAccountId(
                4L, 901L
        )).willReturn(createTransaction(
                901L, AccountTransactionDirection.CREDIT,
                new BigDecimal("150000.00"),
                LocalDateTime.of(2026, 8, 16, 10, 30)
        ));
        given(timeCapsuleEntryMapper.insert(any(TimeCapsuleEntry.class)))
                .willThrow(new DuplicateKeyException("duplicate"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> timeCapsuleEntryService.createTimeCapsuleEntry(
                        7L, 100L,
                        createRequest(901L, "제목", "메시지")
                )
        );

        assertEquals(ErrorCode.DUPLICATE_TIME_CAPSULE_ENTRY,
                exception.getErrorCode());
    }

    @Test
    void deleteTimeCapsuleEntryDeletesDraftWithoutChangingAggregates() {
        TimeCapsuleEntry draft = createEntry(
                1000L, 100L, 901L, AccountTransactionDirection.CREDIT,
                new BigDecimal("150000.00"), TimeCapsuleEntryStatus.DRAFT,
                TimeCapsuleEntryMediaMode.NONE
        );
        given(timeCapsuleEntryMapper.findOwnedByIdForUpdate(1000L, 7L))
                .willReturn(draft);
        given(timeCapsuleMediaMapper.findNotDeletedByEntryIdForUpdate(1000L))
                .willReturn(List.of());
        given(timeCapsuleMediaMapper.markNotDeletedMediaAsDeleted(1000L))
                .willReturn(0);
        given(timeCapsuleEntryMapper.markDraftEntryAsDeleted(1000L))
                .willReturn(1);

        timeCapsuleEntryService.deleteTimeCapsuleEntry(7L, 1000L);

        verify(timeCapsuleEntryMapper).markDraftEntryAsDeleted(1000L);
        verify(timeCapsuleEntryMapper, never()).increaseEntryAggregates(any());
    }

    @Test
    void sealTimeCapsuleEntryRequiresOneActiveImageAndUpdatesAggregates() {
        TimeCapsuleEntry draft = createEntry(
                1000L, 100L, 901L, AccountTransactionDirection.CREDIT,
                new BigDecimal("150000.00"), TimeCapsuleEntryStatus.DRAFT,
                TimeCapsuleEntryMediaMode.IMAGE
        );
        TimeCapsuleEntry sealed = createEntry(
                1000L, 100L, 901L, AccountTransactionDirection.CREDIT,
                new BigDecimal("150000.00"), TimeCapsuleEntryStatus.SEALED,
                TimeCapsuleEntryMediaMode.IMAGE
        );
        ReflectionTestUtils.setField(
                sealed, "sealedAt", LocalDateTime.of(2026, 8, 16, 11, 30)
        );
        given(timeCapsuleEntryMapper.findOwnedByIdForUpdate(1000L, 7L))
                .willReturn(draft);
        given(timeCapsuleEntryMapper.countPendingMediaByEntryId(1000L))
                .willReturn(0);
        given(timeCapsuleEntryMapper.countActiveMediaByEntryIdAndType(
                1000L, TimeCapsuleMediaType.IMAGE
        )).willReturn(1);
        given(timeCapsuleEntryMapper.countActiveMediaByEntryIdAndType(
                1000L, TimeCapsuleMediaType.VIDEO
        )).willReturn(0);
        given(timeCapsuleEntryMapper.sealDraftEntry(1000L)).willReturn(1);
        given(timeCapsuleEntryMapper.increaseEntryAggregates(draft))
                .willReturn(1);
        given(timeCapsuleEntryMapper.findOwnedById(1000L, 7L))
                .willReturn(sealed);

        TimeCapsuleEntrySealResponse response =
                timeCapsuleEntryService.sealTimeCapsuleEntry(7L, 1000L);

        assertEquals("SEALED", response.getStatus());
        verify(timeCapsuleEntryMapper).increaseEntryAggregates(draft);
    }

    @Test
    void sealTimeCapsuleEntryRejectsMissingRepresentativeImage() {
        TimeCapsuleEntry draft = createEntry(
                1000L, 100L, 901L, AccountTransactionDirection.CREDIT,
                new BigDecimal("150000.00"), TimeCapsuleEntryStatus.DRAFT,
                TimeCapsuleEntryMediaMode.NONE
        );
        given(timeCapsuleEntryMapper.findOwnedByIdForUpdate(1000L, 7L))
                .willReturn(draft);
        given(timeCapsuleEntryMapper.countPendingMediaByEntryId(1000L))
                .willReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> timeCapsuleEntryService.sealTimeCapsuleEntry(7L, 1000L)
        );

        assertEquals(ErrorCode.TIME_CAPSULE_ENTRY_MEDIA_REQUIREMENT_NOT_MET,
                exception.getErrorCode());
        verify(timeCapsuleEntryMapper, never()).sealDraftEntry(1000L);
    }

    @Test
    void deleteTimeCapsuleEntryRemovesStoredImageBeforeSoftDelete() {
        TimeCapsuleEntry draft = createEntry(
                1000L, 100L, 901L, AccountTransactionDirection.CREDIT,
                new BigDecimal("150000.00"), TimeCapsuleEntryStatus.DRAFT,
                TimeCapsuleEntryMediaMode.IMAGE
        );
        TimeCapsuleMedia media = TimeCapsuleMedia.createPendingUpload(
                1000L,
                TimeCapsuleMediaType.IMAGE,
                "entries/image.webp",
                "image/webp",
                1024L,
                1
        );
        given(timeCapsuleEntryMapper.findOwnedByIdForUpdate(1000L, 7L))
                .willReturn(draft);
        given(timeCapsuleMediaMapper.findNotDeletedByEntryIdForUpdate(1000L))
                .willReturn(List.of(media));
        given(timeCapsuleMediaMapper.markNotDeletedMediaAsDeleted(1000L))
                .willReturn(1);
        given(timeCapsuleEntryMapper.markDraftEntryAsDeleted(1000L))
                .willReturn(1);

        timeCapsuleEntryService.deleteTimeCapsuleEntry(7L, 1000L);

        verify(timeCapsuleObjectStorage).deleteObject("entries/image.webp");
    }

    private TimeCapsule createTimeCapsule(
            long timeCapsuleId,
            long financialAccountId,
            TimeCapsuleStatus status
    ) {
        TimeCapsule timeCapsule = TimeCapsule.create(
                10L,
                financialAccountId,
                "아이사랑적금",
                LocalDate.of(2038, 1, 12)
        );
        ReflectionTestUtils.setField(
                timeCapsule, "timeCapsuleId", timeCapsuleId
        );
        ReflectionTestUtils.setField(timeCapsule, "status", status);
        return timeCapsule;
    }

    private TimeCapsuleEntryTransaction createTransaction(
            long accountTransactionId,
            AccountTransactionDirection direction,
            BigDecimal amount,
            LocalDateTime occurredAt
    ) {
        TimeCapsuleEntryTransaction transaction =
                new TimeCapsuleEntryTransaction();
        ReflectionTestUtils.setField(
                transaction, "accountTransactionId", accountTransactionId
        );
        ReflectionTestUtils.setField(transaction, "direction", direction);
        ReflectionTestUtils.setField(transaction, "amount", amount);
        ReflectionTestUtils.setField(transaction, "occurredAt", occurredAt);
        return transaction;
    }

    private TimeCapsuleEntry createEntry(
            long entryId,
            long timeCapsuleId,
            long accountTransactionId,
            AccountTransactionDirection direction,
            BigDecimal amount,
            TimeCapsuleEntryStatus status,
            TimeCapsuleEntryMediaMode mediaMode
    ) {
        TimeCapsuleEntry entry = TimeCapsuleEntry.createDraft(
                timeCapsuleId,
                7L,
                createTransaction(
                        accountTransactionId,
                        direction,
                        amount,
                        LocalDateTime.of(2026, 8, 16, 10, 30)
                ),
                "첫 용돈을 받은 날",
                "남은 돈은 꼭 저축하자."
        );
        ReflectionTestUtils.setField(entry, "timeCapsuleEntryId", entryId);
        ReflectionTestUtils.setField(entry, "status", status);
        ReflectionTestUtils.setField(entry, "mediaMode", mediaMode);
        return entry;
    }

    private CreateTimeCapsuleEntryRequest createRequest(
            long accountTransactionId,
            String title,
            String message
    ) {
        CreateTimeCapsuleEntryRequest request =
                new CreateTimeCapsuleEntryRequest();
        ReflectionTestUtils.setField(
                request, "accountTransactionId", accountTransactionId
        );
        ReflectionTestUtils.setField(request, "title", title);
        ReflectionTestUtils.setField(request, "message", message);
        return request;
    }

    private CreateTimeCapsuleMediaUploadUrlRequest
    createMediaUploadUrlRequest(String mimeType, long fileSize) {
        CreateTimeCapsuleMediaUploadUrlRequest request =
                new CreateTimeCapsuleMediaUploadUrlRequest();
        ReflectionTestUtils.setField(request, "mimeType", mimeType);
        ReflectionTestUtils.setField(request, "fileSize", fileSize);
        return request;
    }

    private CompleteTimeCapsuleMediaUploadRequest
    createMediaUploadCompleteRequest(long timeCapsuleMediaId) {
        CompleteTimeCapsuleMediaUploadRequest request =
                new CompleteTimeCapsuleMediaUploadRequest();
        ReflectionTestUtils.setField(
                request, "timeCapsuleMediaId", timeCapsuleMediaId
        );
        return request;
    }

    private TimeCapsuleMedia createMedia(
            long timeCapsuleMediaId,
            long timeCapsuleEntryId,
            TimeCapsuleMediaStatus status
    ) {
        TimeCapsuleMedia media = TimeCapsuleMedia.createPendingUpload(
                timeCapsuleEntryId,
                TimeCapsuleMediaType.IMAGE,
                "time-capsules/100/entries/1000/media/slot-1.jpg",
                "image/jpeg",
                1048576L,
                1
        );
        ReflectionTestUtils.setField(
                media, "timeCapsuleMediaId", timeCapsuleMediaId
        );
        ReflectionTestUtils.setField(media, "status", status);
        return media;
    }
}
