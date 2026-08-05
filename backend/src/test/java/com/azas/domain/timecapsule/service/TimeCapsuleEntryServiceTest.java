package com.azas.domain.timecapsule.service;

import com.azas.domain.timecapsule.dto.TimeCapsuleEntryAutoCreationResult;
import com.azas.domain.timecapsule.dto.TimeCapsuleEntryDetailResponse;
import com.azas.domain.timecapsule.dto.TimeCapsuleEntryListResponse;
import com.azas.domain.timecapsule.dto.TimeCapsuleEntrySealResponse;
import com.azas.domain.timecapsule.dto.TimeCapsuleEntryUpdateResponse;
import com.azas.domain.timecapsule.dto.UpdateTimeCapsuleEntryRequest;
import com.azas.domain.timecapsule.dto.CompleteTimeCapsuleMediaUploadRequest;
import com.azas.domain.timecapsule.dto.CompleteTimeCapsuleMediaUploadResponse;
import com.azas.domain.timecapsule.dto.CreateTimeCapsuleMediaUploadUrlsRequest;
import com.azas.domain.timecapsule.dto.CreateTimeCapsuleMediaUploadUrlsResponse;
import com.azas.domain.timecapsule.entity.AccountTransactionDirection;
import com.azas.domain.timecapsule.entity.TimeCapsule;
import com.azas.domain.timecapsule.entity.TimeCapsuleEntry;
import com.azas.domain.timecapsule.entity.TimeCapsuleEntryMediaMode;
import com.azas.domain.timecapsule.entity.TimeCapsuleEntryStatus;
import com.azas.domain.timecapsule.entity.TimeCapsuleEntryTransaction;
import com.azas.domain.timecapsule.entity.TimeCapsuleMediaType;
import com.azas.domain.timecapsule.entity.TimeCapsuleMedia;
import com.azas.domain.timecapsule.mapper.TimeCapsuleMediaMapper;
import com.azas.domain.timecapsule.entity.TimeCapsuleStatus;
import com.azas.domain.timecapsule.mapper.TimeCapsuleEntryMapper;
import com.azas.domain.timecapsule.mapper.TimeCapsuleMapper;
import com.azas.domain.timecapsule.storage.TimeCapsuleObjectStorage;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
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
    // [JMG] CAPSULE-4 권한이 있는 부모는 삭제되지 않은 엔트리 목록을 조회할 수 있다.
    void getTimeCapsuleEntriesReturnsVisibleEntries() {
        TimeCapsule timeCapsule = createTimeCapsule(
                100L,
                4L,
                TimeCapsuleStatus.COLLECTING
        );
        TimeCapsuleEntry entry = createEntry(
                1000L,
                100L,
                901L,
                AccountTransactionDirection.CREDIT,
                new BigDecimal("100000.00"),
                TimeCapsuleEntryStatus.SEALED,
                TimeCapsuleEntryMediaMode.IMAGE
        );
        ReflectionTestUtils.setField(entry, "mediaCount", 2);

        given(timeCapsuleMapper.findAccessibleById(100L, 7L))
                .willReturn(timeCapsule);
        given(timeCapsuleEntryMapper.findVisibleEntriesByTimeCapsuleId(100L))
                .willReturn(List.of(entry));

        TimeCapsuleEntryListResponse response =
                timeCapsuleEntryService.getTimeCapsuleEntries(7L, 100L);

        assertEquals(1, response.getEntries().size());
        assertEquals(1000L,
                response.getEntries().get(0).getTimeCapsuleEntryId());
        assertEquals(new BigDecimal("100000.00"),
                response.getEntries().get(0).getContributionAmount());
        assertEquals("IMAGE", response.getEntries().get(0).getMediaMode());
        assertEquals(2, response.getEntries().get(0).getMediaCount());
        assertEquals(null, response.getEntries().get(0).getThumbnailUrl());
    }

    @Test
    // [JMG] CAPSULE-4 목록 썸네일은 객체 키가 아닌 만료 시각이 있는 Presigned GET URL로만 반환한다.
    void getTimeCapsuleEntriesReturnsPresignedThumbnailUrl() {
        TimeCapsule timeCapsule = createTimeCapsule(
                100L,
                4L,
                TimeCapsuleStatus.COLLECTING
        );
        TimeCapsuleEntry entry = createEntry(
                1000L,
                100L,
                901L,
                AccountTransactionDirection.CREDIT,
                new BigDecimal("100000.00"),
                TimeCapsuleEntryStatus.SEALED,
                TimeCapsuleEntryMediaMode.IMAGE
        );
        String objectKey = "time-capsules/100/entries/1000/slot-1.jpg";
        ReflectionTestUtils.setField(entry, "thumbnailObjectKey", objectKey);

        given(timeCapsuleMapper.findAccessibleById(100L, 7L))
                .willReturn(timeCapsule);
        given(timeCapsuleEntryMapper.findVisibleEntriesByTimeCapsuleId(100L))
                .willReturn(List.of(entry));
        given(timeCapsuleObjectStorage.createDownloadUrl(
                objectKey,
                Duration.ofMinutes(10)
        )).willReturn(new TimeCapsuleObjectStorage.PresignedUrl(
                "https://s3.example.test/presigned-get"
        ));

        TimeCapsuleEntryListResponse response =
                timeCapsuleEntryService.getTimeCapsuleEntries(7L, 100L);

        assertEquals(
                "https://s3.example.test/presigned-get",
                response.getEntries().get(0).getThumbnailUrl()
        );
        assertTrue(response.getEntries().get(0).getThumbnailExpiresAt()
                .isAfter(LocalDateTime.now()));
    }

    @Test
    // [JMG] CAPSULE-14 엔트리 상세는 활성 미디어만 객체 키 없이 Presigned GET URL로 반환한다.
    void getTimeCapsuleEntryReturnsActiveMediaWithPresignedUrl() {
        TimeCapsuleEntry entry = createEntry(
                1000L,
                100L,
                901L,
                AccountTransactionDirection.CREDIT,
                new BigDecimal("100000.00"),
                TimeCapsuleEntryStatus.SEALED,
                TimeCapsuleEntryMediaMode.IMAGE
        );
        ReflectionTestUtils.setField(
                entry,
                "message",
                "오늘부터 꿈을 위해 함께 모으기 시작했어."
        );
        TimeCapsuleMedia media = TimeCapsuleMedia.createPendingUpload(
                1000L,
                TimeCapsuleMediaType.IMAGE,
                "time-capsules/100/entries/1000/slot-1.jpg",
                "image/jpeg",
                1024L,
                1
        );
        ReflectionTestUtils.setField(media, "timeCapsuleMediaId", 2001L);
        media.activate();

        given(timeCapsuleEntryMapper.findAccessibleById(1000L, 7L))
                .willReturn(entry);
        given(timeCapsuleMediaMapper.findActiveByEntryId(1000L))
                .willReturn(List.of(media));
        given(timeCapsuleObjectStorage.createDownloadUrl(
                media.getObjectKey(),
                Duration.ofMinutes(10)
        )).willReturn(new TimeCapsuleObjectStorage.PresignedUrl(
                "https://s3.example.test/presigned-media-get"
        ));

        TimeCapsuleEntryDetailResponse response =
                timeCapsuleEntryService.getTimeCapsuleEntry(7L, 1000L);

        assertEquals(1000L, response.getTimeCapsuleEntryId());
        assertEquals("오늘부터 꿈을 위해 함께 모으기 시작했어.",
                response.getMessage());
        assertEquals(1, response.getMedia().size());
        assertEquals("https://s3.example.test/presigned-media-get",
                response.getMedia().get(0).getDownloadUrl());
        assertEquals("ACTIVE", media.getStatus().name());
    }

    @Test
    // [JMG] CAPSULE-13 작성자 초안을 삭제할 때 모든 연결 객체를 먼저 삭제하고 엔트리·미디어 집계를 갱신한다.
    void deleteTimeCapsuleEntryDeletesMediaAndMarksDraftAsDeleted() {
        TimeCapsuleEntry entry = createEntry(
                1000L,
                100L,
                901L,
                AccountTransactionDirection.CREDIT,
                new BigDecimal("100000.00"),
                TimeCapsuleEntryStatus.DRAFT,
                TimeCapsuleEntryMediaMode.IMAGE
        );
        TimeCapsuleMedia firstMedia = TimeCapsuleMedia.createPendingUpload(
                1000L,
                TimeCapsuleMediaType.IMAGE,
                "time-capsules/100/entries/1000/slot-1.jpg",
                "image/jpeg",
                1024L,
                1
        );
        TimeCapsuleMedia secondMedia = TimeCapsuleMedia.createPendingUpload(
                1000L,
                TimeCapsuleMediaType.IMAGE,
                "time-capsules/100/entries/1000/slot-2.jpg",
                "image/jpeg",
                2048L,
                2
        );

        given(timeCapsuleEntryMapper.findOwnedByIdForUpdate(1000L, 7L))
                .willReturn(entry);
        given(timeCapsuleMediaMapper.findNotDeletedByEntryIdForUpdate(1000L))
                .willReturn(List.of(firstMedia, secondMedia));
        given(timeCapsuleMediaMapper.markNotDeletedMediaAsDeleted(1000L))
                .willReturn(2);
        given(timeCapsuleEntryMapper.markDraftEntryAsDeleted(1000L))
                .willReturn(1);
        given(timeCapsuleMapper.decreaseEntryCountAndRefreshLatestEntry(100L))
                .willReturn(1);

        timeCapsuleEntryService.deleteTimeCapsuleEntry(7L, 1000L);

        verify(timeCapsuleObjectStorage).deleteObject(
                firstMedia.getObjectKey()
        );
        verify(timeCapsuleObjectStorage).deleteObject(
                secondMedia.getObjectKey()
        );
        verify(timeCapsuleMediaMapper).markNotDeletedMediaAsDeleted(1000L);
        verify(timeCapsuleEntryMapper).markDraftEntryAsDeleted(1000L);
        verify(timeCapsuleMapper).decreaseEntryCountAndRefreshLatestEntry(100L);
    }

    @Test
    // [JMG] CAPSULE-13 S3 객체 삭제가 실패하면 DB 상태와 보관함 집계를 변경하지 않는다.
    void deleteTimeCapsuleEntryLeavesDatabaseUntouchedWhenStorageDeletionFails() {
        TimeCapsuleEntry entry = createEntry(
                1000L,
                100L,
                901L,
                AccountTransactionDirection.CREDIT,
                new BigDecimal("100000.00"),
                TimeCapsuleEntryStatus.DRAFT,
                TimeCapsuleEntryMediaMode.IMAGE
        );
        TimeCapsuleMedia media = TimeCapsuleMedia.createPendingUpload(
                1000L,
                TimeCapsuleMediaType.IMAGE,
                "time-capsules/100/entries/1000/slot-1.jpg",
                "image/jpeg",
                1024L,
                1
        );
        given(timeCapsuleEntryMapper.findOwnedByIdForUpdate(1000L, 7L))
                .willReturn(entry);
        given(timeCapsuleMediaMapper.findNotDeletedByEntryIdForUpdate(1000L))
                .willReturn(List.of(media));
        doThrow(new BusinessException(ErrorCode.TIME_CAPSULE_STORAGE_UNAVAILABLE))
                .when(timeCapsuleObjectStorage)
                .deleteObject(media.getObjectKey());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> timeCapsuleEntryService.deleteTimeCapsuleEntry(7L, 1000L)
        );

        assertEquals(ErrorCode.TIME_CAPSULE_STORAGE_UNAVAILABLE,
                exception.getErrorCode());
        verify(timeCapsuleMediaMapper, never())
                .markNotDeletedMediaAsDeleted(anyLong());
        verify(timeCapsuleEntryMapper, never())
                .markDraftEntryAsDeleted(anyLong());
        verify(timeCapsuleMapper, never())
                .decreaseEntryCountAndRefreshLatestEntry(anyLong());
    }

    @Test
    // [JMG] CAPSULE-5 성공한 적금 입금 거래는 기본값을 갖는 엔트리 초안으로 자동 생성된다.
    void createDraftForSuccessfulSavingsTransferCreatesDraftEntry() {
        TimeCapsule timeCapsule = createTimeCapsule(
                100L,
                4L,
                TimeCapsuleStatus.COLLECTING
        );
        TimeCapsuleEntryTransaction transaction = createTransaction(
                901L,
                AccountTransactionDirection.CREDIT,
                new BigDecimal("150000.00"),
                LocalDateTime.of(2026, 8, 5, 10, 30)
        );

        given(timeCapsuleMapper.findByFinancialAccountIdForUpdate(4L))
                .willReturn(timeCapsule);
        given(timeCapsuleEntryMapper.findByTimeCapsuleAndTransactionId(
                100L,
                901L
        )).willReturn(null);
        given(timeCapsuleEntryMapper.findTransactionByFinancialAccountId(
                4L,
                901L
        )).willReturn(transaction);
        doAnswer(invocation -> {
            TimeCapsuleEntry entry = invocation.getArgument(0);
            ReflectionTestUtils.setField(entry, "timeCapsuleEntryId", 1000L);
            return 1;
        }).when(timeCapsuleEntryMapper).insert(any(TimeCapsuleEntry.class));
        given(timeCapsuleEntryMapper.increaseEntryCountAndRefreshLatestEntry(
                any(TimeCapsuleEntry.class)
        )).willReturn(1);

        Optional<TimeCapsuleEntryAutoCreationResult> result =
                timeCapsuleEntryService.createDraftForSuccessfulSavingsTransfer(
                        7L,
                        4L,
                        901L
                );

        ArgumentCaptor<TimeCapsuleEntry> captor =
                ArgumentCaptor.forClass(TimeCapsuleEntry.class);
        verify(timeCapsuleEntryMapper).insert(captor.capture());
        TimeCapsuleEntry savedEntry = captor.getValue();

        assertTrue(result.isPresent());
        assertEquals(100L, result.get().getTimeCapsuleId());
        assertEquals(1000L, result.get().getTimeCapsuleEntryId());
        assertEquals("DRAFT", result.get().getStatus());
        assertEquals(7L, savedEntry.getAuthorMemberId());
        assertEquals("2026년 8월 5일 저축 기록", savedEntry.getTitle());
        assertEquals(null, savedEntry.getMessage());
        assertEquals(TimeCapsuleEntryMediaMode.NONE, savedEntry.getMediaMode());
        assertEquals(TimeCapsuleEntryStatus.DRAFT, savedEntry.getStatus());
        assertEquals(new BigDecimal("150000.00"),
                savedEntry.getContributionAmount());
        verify(timeCapsuleEntryMapper)
                .increaseEntryCountAndRefreshLatestEntry(savedEntry);
    }

    @Test
    // [JMG] CAPSULE-5 같은 거래의 이체 이벤트가 재시도되면 기존 엔트리를 반환하고 집계를 증가시키지 않는다.
    void createDraftForSuccessfulSavingsTransferIsIdempotent() {
        TimeCapsule timeCapsule = createTimeCapsule(
                100L,
                4L,
                TimeCapsuleStatus.COLLECTING
        );
        TimeCapsuleEntry existingEntry = createEntry(
                1000L,
                100L,
                901L,
                AccountTransactionDirection.CREDIT,
                new BigDecimal("150000.00"),
                TimeCapsuleEntryStatus.DRAFT,
                TimeCapsuleEntryMediaMode.NONE
        );

        given(timeCapsuleMapper.findByFinancialAccountIdForUpdate(4L))
                .willReturn(timeCapsule);
        given(timeCapsuleEntryMapper.findByTimeCapsuleAndTransactionId(
                100L,
                901L
        )).willReturn(existingEntry);

        Optional<TimeCapsuleEntryAutoCreationResult> result =
                timeCapsuleEntryService.createDraftForSuccessfulSavingsTransfer(
                        7L,
                        4L,
                        901L
                );

        assertTrue(result.isPresent());
        assertEquals(1000L, result.get().getTimeCapsuleEntryId());
        verify(timeCapsuleEntryMapper, never()).insert(any());
        verify(timeCapsuleEntryMapper, never())
                .increaseEntryCountAndRefreshLatestEntry(any());
    }

    @Test
    // [JMG] CAPSULE-5 연결된 보관함이 없거나 수집 중이 아니면 이체 자체를 실패시키지 않고 생성을 생략한다.
    void createDraftForSuccessfulSavingsTransferSkipsUnavailableTimeCapsule() {
        given(timeCapsuleMapper.findByFinancialAccountIdForUpdate(4L))
                .willReturn(null);

        Optional<TimeCapsuleEntryAutoCreationResult> result =
                timeCapsuleEntryService.createDraftForSuccessfulSavingsTransfer(
                        7L,
                        4L,
                        901L
                );

        assertTrue(result.isEmpty());
        verify(timeCapsuleEntryMapper, never())
                .findByTimeCapsuleAndTransactionId(anyLong(), anyLong());
    }

    @Test
    // [JMG] CAPSULE-5 출금·0원·음수 거래는 적금 저축 기록으로 생성하지 않는다.
    void createDraftForSuccessfulSavingsTransferRejectsIneligibleTransaction() {
        TimeCapsule timeCapsule = createTimeCapsule(
                100L,
                4L,
                TimeCapsuleStatus.COLLECTING
        );
        given(timeCapsuleMapper.findByFinancialAccountIdForUpdate(4L))
                .willReturn(timeCapsule);
        given(timeCapsuleEntryMapper.findByTimeCapsuleAndTransactionId(
                100L,
                901L
        )).willReturn(null);
        given(timeCapsuleEntryMapper.findTransactionByFinancialAccountId(
                4L,
                901L
        )).willReturn(createTransaction(
                901L,
                AccountTransactionDirection.CREDIT,
                BigDecimal.ZERO,
                LocalDateTime.of(2026, 8, 5, 10, 30)
        ));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> timeCapsuleEntryService
                        .createDraftForSuccessfulSavingsTransfer(7L, 4L, 901L)
        );

        assertEquals(ErrorCode.INELIGIBLE_TIME_CAPSULE_TRANSACTION,
                exception.getErrorCode());
        verify(timeCapsuleEntryMapper, never()).insert(any());
    }

    @Test
    // [JMG] CAPSULE-12 작성자는 DRAFT 엔트리의 제목 또는 편지를 수정할 수 있다.
    void updateTimeCapsuleEntryUpdatesDraftContent() {
        TimeCapsuleEntry entry = createEntry(
                1000L,
                100L,
                901L,
                AccountTransactionDirection.CREDIT,
                new BigDecimal("150000.00"),
                TimeCapsuleEntryStatus.DRAFT,
                TimeCapsuleEntryMediaMode.NONE
        );
        TimeCapsuleEntry updatedEntry = createEntry(
                1000L,
                100L,
                901L,
                AccountTransactionDirection.CREDIT,
                new BigDecimal("150000.00"),
                TimeCapsuleEntryStatus.DRAFT,
                TimeCapsuleEntryMediaMode.NONE
        );
        ReflectionTestUtils.setField(updatedEntry, "title", "첫 저축 기록");
        ReflectionTestUtils.setField(
                updatedEntry,
                "message",
                "오늘부터 대학자금을 모으기 시작했어."
        );
        ReflectionTestUtils.setField(updatedEntry, "editCount", 1);

        given(timeCapsuleEntryMapper.findOwnedByIdForUpdate(1000L, 7L))
                .willReturn(entry);
        given(timeCapsuleEntryMapper.updateDraftContent(
                1000L,
                "첫 저축 기록",
                "오늘부터 대학자금을 모으기 시작했어."
        )).willReturn(1);
        given(timeCapsuleEntryMapper.findOwnedById(1000L, 7L))
                .willReturn(updatedEntry);

        TimeCapsuleEntryUpdateResponse response =
                timeCapsuleEntryService.updateTimeCapsuleEntry(
                        7L,
                        1000L,
                        createUpdateRequest(
                                "  첫 저축 기록  ",
                                " 오늘부터 대학자금을 모으기 시작했어. "
                        )
                );

        assertEquals("첫 저축 기록", response.getTitle());
        assertEquals("오늘부터 대학자금을 모으기 시작했어.",
                response.getMessage());
        assertEquals(1, response.getEditCount());
        verify(timeCapsuleEntryMapper).updateDraftContent(
                eq(1000L),
                eq("첫 저축 기록"),
                eq("오늘부터 대학자금을 모으기 시작했어.")
        );
    }

    @Test
    // [JMG] CAPSULE-12 봉인된 엔트리는 수정 전에 상태 충돌 오류로 차단한다.
    void updateTimeCapsuleEntryRejectsSealedEntry() {
        TimeCapsuleEntry entry = createEntry(
                1000L,
                100L,
                901L,
                AccountTransactionDirection.CREDIT,
                new BigDecimal("150000.00"),
                TimeCapsuleEntryStatus.SEALED,
                TimeCapsuleEntryMediaMode.NONE
        );
        given(timeCapsuleEntryMapper.findOwnedByIdForUpdate(1000L, 7L))
                .willReturn(entry);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> timeCapsuleEntryService.updateTimeCapsuleEntry(
                        7L,
                        1000L,
                        createUpdateRequest("새 제목", null)
                )
        );

        assertEquals(ErrorCode.TIME_CAPSULE_ENTRY_MODIFICATION_NOT_ALLOWED,
                exception.getErrorCode());
        verify(timeCapsuleEntryMapper, never()).updateDraftContent(
                anyLong(),
                any(),
                any()
        );
    }

    @Test
    // [JMG] CAPSULE-15 미디어가 없는 NONE 엔트리는 작성자가 바로 봉인할 수 있다.
    void sealTimeCapsuleEntrySealsNoneMediaDraft() {
        TimeCapsuleEntry draftEntry = createEntry(
                1000L,
                100L,
                901L,
                AccountTransactionDirection.CREDIT,
                new BigDecimal("150000.00"),
                TimeCapsuleEntryStatus.DRAFT,
                TimeCapsuleEntryMediaMode.NONE
        );
        TimeCapsuleEntry sealedEntry = createEntry(
                1000L,
                100L,
                901L,
                AccountTransactionDirection.CREDIT,
                new BigDecimal("150000.00"),
                TimeCapsuleEntryStatus.SEALED,
                TimeCapsuleEntryMediaMode.NONE
        );
        ReflectionTestUtils.setField(
                sealedEntry,
                "sealedAt",
                LocalDateTime.of(2026, 8, 5, 11, 40)
        );

        given(timeCapsuleEntryMapper.findOwnedByIdForUpdate(1000L, 7L))
                .willReturn(draftEntry);
        given(timeCapsuleEntryMapper.countPendingMediaByEntryId(1000L))
                .willReturn(0);
        given(timeCapsuleEntryMapper.countActiveMediaByEntryIdAndType(
                1000L,
                TimeCapsuleMediaType.IMAGE
        )).willReturn(0);
        given(timeCapsuleEntryMapper.countActiveMediaByEntryIdAndType(
                1000L,
                TimeCapsuleMediaType.VIDEO
        )).willReturn(0);
        given(timeCapsuleEntryMapper.sealDraftEntry(1000L)).willReturn(1);
        given(timeCapsuleEntryMapper.findOwnedById(1000L, 7L))
                .willReturn(sealedEntry);

        TimeCapsuleEntrySealResponse response =
                timeCapsuleEntryService.sealTimeCapsuleEntry(7L, 1000L);

        assertEquals("SEALED", response.getStatus());
        assertEquals(LocalDateTime.of(2026, 8, 5, 11, 40),
                response.getSealedAt());
    }

    @Test
    // [JMG] CAPSULE-15 IMAGE 엔트리는 활성 이미지가 없으면 봉인할 수 없다.
    void sealTimeCapsuleEntryRejectsMissingRequiredImage() {
        TimeCapsuleEntry imageEntry = createEntry(
                1000L,
                100L,
                901L,
                AccountTransactionDirection.CREDIT,
                new BigDecimal("150000.00"),
                TimeCapsuleEntryStatus.DRAFT,
                TimeCapsuleEntryMediaMode.IMAGE
        );
        given(timeCapsuleEntryMapper.findOwnedByIdForUpdate(1000L, 7L))
                .willReturn(imageEntry);
        given(timeCapsuleEntryMapper.countPendingMediaByEntryId(1000L))
                .willReturn(0);
        given(timeCapsuleEntryMapper.countActiveMediaByEntryIdAndType(
                1000L,
                TimeCapsuleMediaType.IMAGE
        )).willReturn(0);
        given(timeCapsuleEntryMapper.countActiveMediaByEntryIdAndType(
                1000L,
                TimeCapsuleMediaType.VIDEO
        )).willReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> timeCapsuleEntryService.sealTimeCapsuleEntry(7L, 1000L)
        );

        assertEquals(ErrorCode.TIME_CAPSULE_ENTRY_MEDIA_REQUIREMENT_NOT_MET,
                exception.getErrorCode());
        verify(timeCapsuleEntryMapper, never()).sealDraftEntry(1000L);
    }

    @Test
    // [JMG] CAPSULE-7 IMAGE DRAFT 엔트리에는 서버 객체 키 기반의 Presigned PUT URL만 발급한다.
    void createMediaUploadUrlsCreatesPendingMediaAndPresignedUrl() {
        TimeCapsuleEntry imageEntry = createEntry(
                1000L,
                100L,
                901L,
                AccountTransactionDirection.CREDIT,
                new BigDecimal("150000.00"),
                TimeCapsuleEntryStatus.DRAFT,
                TimeCapsuleEntryMediaMode.IMAGE
        );
        given(timeCapsuleEntryMapper.findOwnedByIdForUpdate(1000L, 7L))
                .willReturn(imageEntry);
        given(timeCapsuleMediaMapper.countByEntryIdAndSlotNo(1000L, 1))
                .willReturn(0);
        doAnswer(invocation -> {
            TimeCapsuleMedia media = invocation.getArgument(0);
            ReflectionTestUtils.setField(media, "timeCapsuleMediaId", 2001L);
            return 1;
        }).when(timeCapsuleMediaMapper).insert(any(TimeCapsuleMedia.class));
        given(timeCapsuleObjectStorage.createUploadUrl(
                org.mockito.ArgumentMatchers.startsWith("time-capsules/100/entries/1000/"),
                eq("image/jpeg"),
                any()
        )).willReturn(new TimeCapsuleObjectStorage.PresignedUrl(
                "https://s3.example.test/presigned-put"
        ));

        CreateTimeCapsuleMediaUploadUrlsResponse response =
                timeCapsuleEntryService.createMediaUploadUrls(
                        7L,
                        1000L,
                        createUploadUrlsRequest("image/jpeg", 1024L, 1)
                );

        assertEquals(1000L, response.getTimeCapsuleEntryId());
        assertEquals(1, response.getUploads().size());
        assertEquals(2001L,
                response.getUploads().get(0).getTimeCapsuleMediaId());
        assertEquals("image/jpeg",
                response.getUploads().get(0).getRequiredHeaders()
                        .get("Content-Type"));
    }

    @Test
    // [JMG] CAPSULE-8 S3 메타데이터와 요청값이 일치하면 대기 미디어를 ACTIVE로 전환한다.
    void completeMediaUploadActivatesVerifiedPendingMedia() {
        TimeCapsuleEntry imageEntry = createEntry(
                1000L,
                100L,
                901L,
                AccountTransactionDirection.CREDIT,
                new BigDecimal("150000.00"),
                TimeCapsuleEntryStatus.DRAFT,
                TimeCapsuleEntryMediaMode.IMAGE
        );
        TimeCapsuleMedia media = TimeCapsuleMedia.createPendingUpload(
                1000L,
                TimeCapsuleMediaType.IMAGE,
                "time-capsules/100/entries/1000/image.jpg",
                "image/jpeg",
                1024L,
                1
        );
        ReflectionTestUtils.setField(media, "timeCapsuleMediaId", 2001L);

        given(timeCapsuleEntryMapper.findOwnedByIdForUpdate(1000L, 7L))
                .willReturn(imageEntry);
        given(timeCapsuleMediaMapper.findByEntryIdAndIdsForUpdate(
                1000L,
                List.of(2001L)
        )).willReturn(List.of(media));
        given(timeCapsuleObjectStorage.getObjectMetadata(media.getObjectKey()))
                .willReturn(new TimeCapsuleObjectStorage.StoredObjectMetadata(
                        "image/jpeg",
                        1024L
                ));
        given(timeCapsuleMediaMapper.activatePendingMedia(List.of(2001L)))
                .willReturn(1);
        given(timeCapsuleMediaMapper.setThumbnailIfAbsent(
                1000L,
                media.getObjectKey()
        )).willReturn(1);
        given(timeCapsuleMediaMapper.countActiveByEntryId(1000L))
                .willReturn(1);

        CompleteTimeCapsuleMediaUploadResponse response =
                timeCapsuleEntryService.completeMediaUpload(
                        7L,
                        1000L,
                        createCompleteUploadRequest(2001L)
                );

        assertEquals(1, response.getMediaCount());
        assertTrue(response.isThumbnailReady());
        assertEquals("ACTIVE", response.getMedia().get(0).getStatus());
    }

    // [JMG] CAPSULE-4~5 테스트용 보관함을 상태별로 구성한다.
    private TimeCapsule createTimeCapsule(
            long timeCapsuleId,
            long financialAccountId,
            TimeCapsuleStatus status
    ) {
        TimeCapsule timeCapsule = TimeCapsule.create(
                10L,
                financialAccountId,
                "깨비의 대학자금 타임캡슐",
                LocalDate.of(2038, 1, 12)
        );
        ReflectionTestUtils.setField(
                timeCapsule,
                "timeCapsuleId",
                timeCapsuleId
        );
        ReflectionTestUtils.setField(timeCapsule, "status", status);
        return timeCapsule;
    }

    // [JMG] CAPSULE-5 테스트용 적금 계좌 거래 스냅샷을 구성한다.
    private TimeCapsuleEntryTransaction createTransaction(
            long accountTransactionId,
            AccountTransactionDirection direction,
            BigDecimal amount,
            LocalDateTime occurredAt
    ) {
        TimeCapsuleEntryTransaction transaction =
                new TimeCapsuleEntryTransaction();
        ReflectionTestUtils.setField(
                transaction,
                "accountTransactionId",
                accountTransactionId
        );
        ReflectionTestUtils.setField(transaction, "direction", direction);
        ReflectionTestUtils.setField(transaction, "amount", amount);
        ReflectionTestUtils.setField(transaction, "occurredAt", occurredAt);
        return transaction;
    }

    // [JMG] CAPSULE-4~5 테스트용 엔트리를 자동 생성 규칙과 동일한 값으로 구성한다.
    private TimeCapsuleEntry createEntry(
            long entryId,
            long timeCapsuleId,
            long accountTransactionId,
            AccountTransactionDirection direction,
            BigDecimal amount,
            TimeCapsuleEntryStatus status,
            TimeCapsuleEntryMediaMode mediaMode
    ) {
        TimeCapsuleEntry entry =
                TimeCapsuleEntry.createDraftForSuccessfulTransfer(
                        timeCapsuleId,
                        7L,
                        createTransaction(
                                accountTransactionId,
                                direction,
                                amount,
                                LocalDateTime.of(2026, 8, 5, 10, 30)
                        )
                );
        ReflectionTestUtils.setField(entry, "timeCapsuleEntryId", entryId);
        ReflectionTestUtils.setField(entry, "status", status);
        ReflectionTestUtils.setField(entry, "mediaMode", mediaMode);
        return entry;
    }

    // [JMG] CAPSULE-12 테스트용 엔트리 수정 요청을 제목·편지 조합으로 구성한다.
    private UpdateTimeCapsuleEntryRequest createUpdateRequest(
            String title,
            String message
    ) {
        UpdateTimeCapsuleEntryRequest request =
                new UpdateTimeCapsuleEntryRequest();
        ReflectionTestUtils.setField(request, "title", title);
        ReflectionTestUtils.setField(request, "message", message);
        return request;
    }

    // [JMG] CAPSULE-7 테스트용 미디어 URL 발급 요청을 MIME 타입·파일 크기·슬롯으로 구성한다.
    private CreateTimeCapsuleMediaUploadUrlsRequest createUploadUrlsRequest(
            String mimeType,
            long fileSize,
            int slotNo
    ) {
        CreateTimeCapsuleMediaUploadUrlsRequest.FileRequest file =
                new CreateTimeCapsuleMediaUploadUrlsRequest.FileRequest();
        ReflectionTestUtils.setField(file, "mimeType", mimeType);
        ReflectionTestUtils.setField(file, "fileSize", fileSize);
        ReflectionTestUtils.setField(file, "slotNo", slotNo);

        CreateTimeCapsuleMediaUploadUrlsRequest request =
                new CreateTimeCapsuleMediaUploadUrlsRequest();
        ReflectionTestUtils.setField(request, "files", List.of(file));
        return request;
    }

    // [JMG] CAPSULE-8 테스트용 업로드 완료 요청을 미디어 ID 목록으로 구성한다.
    private CompleteTimeCapsuleMediaUploadRequest createCompleteUploadRequest(
            long mediaId
    ) {
        CompleteTimeCapsuleMediaUploadRequest request =
                new CompleteTimeCapsuleMediaUploadRequest();
        ReflectionTestUtils.setField(request, "mediaIds", List.of(mediaId));
        return request;
    }
}
