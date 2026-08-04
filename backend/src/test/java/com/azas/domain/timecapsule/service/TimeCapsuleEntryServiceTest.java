package com.azas.domain.timecapsule.service;

import com.azas.domain.timecapsule.dto.CreateTimeCapsuleEntryRequest;
import com.azas.domain.timecapsule.dto.TimeCapsuleEntryListResponse;
import com.azas.domain.timecapsule.dto.TimeCapsuleEntryResponse;
import com.azas.domain.timecapsule.entity.AccountTransactionDirection;
import com.azas.domain.timecapsule.entity.TimeCapsule;
import com.azas.domain.timecapsule.entity.TimeCapsuleEntry;
import com.azas.domain.timecapsule.entity.TimeCapsuleEntryMediaMode;
import com.azas.domain.timecapsule.entity.TimeCapsuleEntryStatus;
import com.azas.domain.timecapsule.entity.TimeCapsuleEntryTransaction;
import com.azas.domain.timecapsule.entity.TimeCapsuleStatus;
import com.azas.domain.timecapsule.mapper.TimeCapsuleEntryMapper;
import com.azas.domain.timecapsule.mapper.TimeCapsuleMapper;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TimeCapsuleEntryServiceTest {

    @Mock
    private TimeCapsuleMapper timeCapsuleMapper;

    @Mock
    private TimeCapsuleEntryMapper timeCapsuleEntryMapper;

    @InjectMocks
    private TimeCapsuleEntryService timeCapsuleEntryService;

    @Test
    // [JMG] CAPSULE-4 접근 가능한 보관함의 삭제되지 않은 기록 목록을 응답 계약으로 변환한다.
    void getTimeCapsuleEntriesReturnsVisibleEntries() {
        TimeCapsule timeCapsule = createTimeCapsule(
                100L,
                TimeCapsuleStatus.COLLECTING
        );
        TimeCapsuleEntry entry = createEntry(
                1000L,
                100L,
                AccountTransactionDirection.CREDIT,
                TimeCapsuleEntryStatus.SEALED,
                TimeCapsuleEntryMediaMode.IMAGE
        );
        ReflectionTestUtils.setField(entry, "mediaCount", 2);
        ReflectionTestUtils.setField(
                entry,
                "thumbnailObjectKey",
                "time-capsules/100/entries/1000/thumbnail.jpg"
        );

        given(timeCapsuleMapper.findAccessibleById(100L, 7L))
                .willReturn(timeCapsule);
        given(timeCapsuleEntryMapper.findVisibleEntriesByTimeCapsuleId(100L))
                .willReturn(List.of(entry));

        TimeCapsuleEntryListResponse response =
                timeCapsuleEntryService.getTimeCapsuleEntries(7L, 100L);

        assertEquals(1, response.getEntries().size());
        assertEquals(
                1000L,
                response.getEntries().get(0).getTimeCapsuleEntryId()
        );
        assertEquals(
                new BigDecimal("100000.00"),
                response.getEntries().get(0).getContributionAmount()
        );
        assertEquals("IMAGE", response.getEntries().get(0).getMediaMode());
        assertEquals(2, response.getEntries().get(0).getMediaCount());
    }

    @Test
    // [JMG] CAPSULE-4 권한 없는 보관함 조회는 존재 여부를 숨긴 404 오류로 처리한다.
    void getTimeCapsuleEntriesHidesInaccessibleTimeCapsule() {
        given(timeCapsuleMapper.findAccessibleById(100L, 7L))
                .willReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> timeCapsuleEntryService.getTimeCapsuleEntries(
                        7L,
                        100L
                )
        );

        assertEquals(
                ErrorCode.TIME_CAPSULE_NOT_FOUND,
                exception.getErrorCode()
        );
        verify(timeCapsuleEntryMapper, never())
                .findVisibleEntriesByTimeCapsuleId(100L);
    }

    @Test
    // [JMG] CAPSULE-5 적금 입금 거래로 기록을 만들면 거래 스냅샷과 보관함 집계값을 함께 저장한다.
    void createTimeCapsuleEntryCreatesDraftForCreditTransaction() {
        TimeCapsule timeCapsule = createTimeCapsule(
                100L,
                TimeCapsuleStatus.COLLECTING
        );
        TimeCapsuleEntryTransaction transaction = createTransaction(
                901L,
                AccountTransactionDirection.CREDIT,
                new BigDecimal("150000.00"),
                LocalDateTime.of(2026, 7, 15, 10, 0)
        );

        given(timeCapsuleMapper.findAccessibleByIdForUpdate(100L, 7L))
                .willReturn(timeCapsule);
        given(timeCapsuleEntryMapper.findTransactionByTimeCapsuleAndId(
                100L,
                901L
        )).willReturn(transaction);
        doAnswer(invocation -> {
            TimeCapsuleEntry entry = invocation.getArgument(0);
            ReflectionTestUtils.setField(
                    entry,
                    "timeCapsuleEntryId",
                    1000L
            );
            return 1;
        }).when(timeCapsuleEntryMapper).insert(any(TimeCapsuleEntry.class));
        given(timeCapsuleEntryMapper.increaseEntryCountAndRefreshLatestEntry(
                any(TimeCapsuleEntry.class)
        )).willReturn(1);

        TimeCapsuleEntryResponse response =
                timeCapsuleEntryService.createTimeCapsuleEntry(
                        7L,
                        100L,
                        createRequest(901L, "  첫 생일 축하  ", " 오늘도 저축했어. ", "IMAGE")
                );

        ArgumentCaptor<TimeCapsuleEntry> captor =
                ArgumentCaptor.forClass(TimeCapsuleEntry.class);
        verify(timeCapsuleEntryMapper).insert(captor.capture());

        TimeCapsuleEntry savedEntry = captor.getValue();
        assertEquals(7L, savedEntry.getAuthorMemberId());
        assertEquals(901L, savedEntry.getAccountTransactionId());
        assertEquals("첫 생일 축하", savedEntry.getTitle());
        assertEquals("오늘도 저축했어.", savedEntry.getMessage());
        assertEquals(
                new BigDecimal("150000.00"),
                savedEntry.getContributionAmount()
        );
        assertEquals(TimeCapsuleEntryStatus.DRAFT, savedEntry.getStatus());
        assertEquals(1000L, response.getTimeCapsuleEntryId());
        assertEquals("DRAFT", response.getStatus());
        verify(timeCapsuleEntryMapper)
                .increaseEntryCountAndRefreshLatestEntry(savedEntry);
    }

    @Test
    // [JMG] CAPSULE-5 공개된 보관함에는 신규 기록을 만들지 않고 거래 정보도 조회하지 않는다.
    void createTimeCapsuleEntryRejectsReleasedTimeCapsule() {
        given(timeCapsuleMapper.findAccessibleByIdForUpdate(100L, 7L))
                .willReturn(
                        createTimeCapsule(
                                100L,
                                TimeCapsuleStatus.RELEASED
                        )
                );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> timeCapsuleEntryService.createTimeCapsuleEntry(
                        7L,
                        100L,
                        createRequest(901L, "제목", "메시지", "NONE")
                )
        );

        assertEquals(
                ErrorCode.TIME_CAPSULE_ENTRY_CREATION_NOT_ALLOWED,
                exception.getErrorCode()
        );
        verify(timeCapsuleEntryMapper, never())
                .findTransactionByTimeCapsuleAndId(anyLong(), anyLong());
    }

    @Test
    // [JMG] CAPSULE-5 적금 계좌의 출금 거래는 기록 생성 대상으로 거부한다.
    void createTimeCapsuleEntryRejectsDebitTransaction() {
        given(timeCapsuleMapper.findAccessibleByIdForUpdate(100L, 7L))
                .willReturn(
                        createTimeCapsule(
                                100L,
                                TimeCapsuleStatus.COLLECTING
                        )
                );
        given(timeCapsuleEntryMapper.findTransactionByTimeCapsuleAndId(
                100L,
                902L
        )).willReturn(
                createTransaction(
                        902L,
                        AccountTransactionDirection.DEBIT,
                        new BigDecimal("15000.00"),
                        LocalDateTime.of(2026, 7, 15, 10, 0)
                )
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> timeCapsuleEntryService.createTimeCapsuleEntry(
                        7L,
                        100L,
                        createRequest(902L, "제목", "메시지", "NONE")
                )
        );

        assertEquals(
                ErrorCode.INELIGIBLE_TIME_CAPSULE_TRANSACTION,
                exception.getErrorCode()
        );
        verify(timeCapsuleEntryMapper, never())
                .insert(any(TimeCapsuleEntry.class));
    }

    @Test
    // [JMG] CAPSULE-5 다른 계좌 거래 ID는 존재를 노출하지 않고 찾을 수 없음으로 처리한다.
    void createTimeCapsuleEntryHidesTransactionOutsideTimeCapsuleAccount() {
        given(timeCapsuleMapper.findAccessibleByIdForUpdate(100L, 7L))
                .willReturn(
                        createTimeCapsule(
                                100L,
                                TimeCapsuleStatus.COLLECTING
                        )
                );
        given(timeCapsuleEntryMapper.findTransactionByTimeCapsuleAndId(
                100L,
                999L
        )).willReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> timeCapsuleEntryService.createTimeCapsuleEntry(
                        7L,
                        100L,
                        createRequest(999L, "제목", "메시지", "NONE")
                )
        );

        assertEquals(
                ErrorCode.ACCOUNT_TRANSACTION_NOT_FOUND,
                exception.getErrorCode()
        );
    }

    @Test
    // [JMG] CAPSULE-5 같은 거래의 동시 또는 재시도 생성은 DB 고유 제약을 409 오류로 변환한다.
    void createTimeCapsuleEntryConvertsDuplicateKeyToConflict() {
        given(timeCapsuleMapper.findAccessibleByIdForUpdate(100L, 7L))
                .willReturn(
                        createTimeCapsule(
                                100L,
                                TimeCapsuleStatus.COLLECTING
                        )
                );
        given(timeCapsuleEntryMapper.findTransactionByTimeCapsuleAndId(
                100L,
                901L
        )).willReturn(
                createTransaction(
                        901L,
                        AccountTransactionDirection.CREDIT,
                        new BigDecimal("150000.00"),
                        LocalDateTime.of(2026, 7, 15, 10, 0)
                )
        );
        given(timeCapsuleEntryMapper.insert(any(TimeCapsuleEntry.class)))
                .willThrow(new DuplicateKeyException("duplicate entry"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> timeCapsuleEntryService.createTimeCapsuleEntry(
                        7L,
                        100L,
                        createRequest(901L, "제목", "메시지", "NONE")
                )
        );

        assertEquals(
                ErrorCode.DUPLICATE_TIME_CAPSULE_ENTRY,
                exception.getErrorCode()
        );
        verify(timeCapsuleEntryMapper, never())
                .increaseEntryCountAndRefreshLatestEntry(
                        any(TimeCapsuleEntry.class)
                );
    }

    // [JMG] CAPSULE-4~5 테스트용 ERD 타임캡슐을 상태별로 구성한다.
    private TimeCapsule createTimeCapsule(
            long timeCapsuleId,
            TimeCapsuleStatus status
    ) {
        TimeCapsule timeCapsule = TimeCapsule.create(
                10L,
                3L,
                "깨비의 적금 타임캡슐",
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

    // [JMG] CAPSULE-4~5 테스트용 적금 계좌 거래 스냅샷을 구성한다.
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
        ReflectionTestUtils.setField(
                transaction,
                "occurredAt",
                occurredAt
        );
        return transaction;
    }

    // [JMG] CAPSULE-4 테스트용 조회 기록을 미디어 유형과 상태별로 구성한다.
    private TimeCapsuleEntry createEntry(
            long entryId,
            long timeCapsuleId,
            AccountTransactionDirection direction,
            TimeCapsuleEntryStatus status,
            TimeCapsuleEntryMediaMode mediaMode
    ) {
        TimeCapsuleEntry entry = TimeCapsuleEntry.create(
                timeCapsuleId,
                7L,
                createTransaction(
                        901L,
                        direction,
                        new BigDecimal("100000.00"),
                        LocalDateTime.of(2026, 7, 20, 9, 0)
                ),
                "7월 저축 기록",
                "깨비를 위한 저축이야.",
                mediaMode
        );
        ReflectionTestUtils.setField(
                entry,
                "timeCapsuleEntryId",
                entryId
        );
        ReflectionTestUtils.setField(entry, "status", status);
        return entry;
    }

    // [JMG] CAPSULE-5 테스트용 기록 생성 요청을 API 필드명 기준으로 구성한다.
    private CreateTimeCapsuleEntryRequest createRequest(
            long accountTransactionId,
            String title,
            String message,
            String mediaMode
    ) {
        CreateTimeCapsuleEntryRequest request =
                new CreateTimeCapsuleEntryRequest();
        ReflectionTestUtils.setField(
                request,
                "accountTransactionId",
                accountTransactionId
        );
        ReflectionTestUtils.setField(request, "title", title);
        ReflectionTestUtils.setField(request, "message", message);
        ReflectionTestUtils.setField(request, "mediaMode", mediaMode);
        return request;
    }
}
