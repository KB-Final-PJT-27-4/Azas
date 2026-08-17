package com.azas.domain.finance.autotransfer.service;

import com.azas.domain.finance.autotransfer.dto.*;
import com.azas.domain.finance.autotransfer.entity.AutoTransferFrequency;
import com.azas.domain.finance.autotransfer.entity.AutoTransferScheduleStatus;
import com.azas.domain.finance.autotransfer.mapper.AutoTransferScheduleMapper;
import com.azas.domain.finance.transfer.entity.TransferStatus;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import com.azas.domain.finance.autotransfer.dto.AutoTransferScheduleDetailRow;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AutoTransferScheduleServiceImplTest {

    private AutoTransferScheduleMapper mapper;
    private AutoTransferScheduleServiceImpl service;

    @BeforeEach
    void setUp() {
        mapper = mock(AutoTransferScheduleMapper.class);

        Clock clock = Clock.fixed(
                Instant.parse("2026-08-17T06:00:00Z"),
                ZoneOffset.UTC
        );

        service = new AutoTransferScheduleServiceImpl(
                mapper,
                clock
        );
    }

    @Test
    void 자동이체_일정을_등록한다() {
        String key = UUID.randomUUID().toString();
        CreateAutoTransferScheduleRequest request =
                createRequest();

        when(mapper.findByIdempotencyKey(key))
                .thenReturn(null);
        when(mapper.countChildAccess(6L, 7L))
                .thenReturn(1);
        when(mapper.findAccountForUpdate(1L))
                .thenReturn(parentAccount(1L, 7L));
        when(mapper.findAccountForUpdate(12L))
                .thenReturn(childSavingsAccount(
                        12L,
                        6L,
                        31L
                ));
        when(mapper.countEquivalentSchedule(
                7L,
                6L,
                1L,
                12L,
                new BigDecimal("80000"),
                "MONTHLY",
                10,
                LocalDate.of(2026, 9, 10),
                LocalDate.of(2029, 2, 10)
        )).thenReturn(0);

        when(mapper.insertSchedule(any()))
                .thenAnswer(invocation -> {
                    AutoTransferScheduleInsertCommand command =
                            invocation.getArgument(0);

                    command.setAutoTransferScheduleId(21L);
                    return 1;
                });

        var response = service.createSchedule(
                7L,
                key,
                request
        );

        assertEquals(
                21L,
                response.getAutoTransferScheduleId()
        );
        assertEquals(
                31L,
                response.getFinancialGoalId()
        );
        assertEquals(
                Instant.parse("2026-09-10T00:00:00Z"),
                response.getNextTransferAt()
        );
        assertEquals(
                AutoTransferScheduleStatus.ACTIVE,
                response.getStatus()
        );

        verify(mapper).insertSchedule(any());
    }

    @Test
    void 동일한_멱등키와_요청은_기존_응답을_반환한다() {
        String key = UUID.randomUUID().toString();
        CreateAutoTransferScheduleRequest request =
                createRequest();

        when(mapper.findByIdempotencyKey(key))
                .thenReturn(existingSchedule(key));

        var response = service.createSchedule(
                7L,
                key,
                request
        );

        assertEquals(
                21L,
                response.getAutoTransferScheduleId()
        );

        verify(
                mapper,
                never()
        ).insertSchedule(any());
        verify(
                mapper,
                never()
        ).findAccountForUpdate(any());
    }

    @Test
    void 동일한_활성_일정은_거절한다() {
        String key = UUID.randomUUID().toString();
        CreateAutoTransferScheduleRequest request =
                createRequest();

        when(mapper.findByIdempotencyKey(key))
                .thenReturn(null);
        when(mapper.countChildAccess(6L, 7L))
                .thenReturn(1);
        when(mapper.findAccountForUpdate(1L))
                .thenReturn(parentAccount(1L, 7L));
        when(mapper.findAccountForUpdate(12L))
                .thenReturn(childSavingsAccount(
                        12L,
                        6L,
                        31L
                ));
        when(mapper.countEquivalentSchedule(
                7L,
                6L,
                1L,
                12L,
                new BigDecimal("80000"),
                "MONTHLY",
                10,
                LocalDate.of(2026, 9, 10),
                LocalDate.of(2029, 2, 10)
        )).thenReturn(1);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.createSchedule(
                        7L,
                        key,
                        request
                )
        );

        assertEquals(
                ErrorCode.DUPLICATE_AUTO_TRANSFER_SCHEDULE,
                exception.getErrorCode()
        );

        verify(mapper, never())
                .insertSchedule(any());
    }

    @Test
    void 출금계좌_소유자가_아니면_거절한다() {
        String key = UUID.randomUUID().toString();

        when(mapper.findByIdempotencyKey(key))
                .thenReturn(null);
        when(mapper.countChildAccess(6L, 7L))
                .thenReturn(1);
        when(mapper.findAccountForUpdate(1L))
                .thenReturn(parentAccount(1L, 99L));
        when(mapper.findAccountForUpdate(12L))
                .thenReturn(childSavingsAccount(
                        12L,
                        6L,
                        31L
                ));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.createSchedule(
                        7L,
                        key,
                        createRequest()
                )
        );

        assertEquals(
                ErrorCode.FINANCIAL_ACCOUNT_ACCESS_DENIED,
                exception.getErrorCode()
        );
    }

    @Test
    void 목표가_없는_입금계좌는_거절한다() {
        String key = UUID.randomUUID().toString();

        when(mapper.findByIdempotencyKey(key))
                .thenReturn(null);
        when(mapper.countChildAccess(6L, 7L))
                .thenReturn(1);
        when(mapper.findAccountForUpdate(1L))
                .thenReturn(parentAccount(1L, 7L));
        when(mapper.findAccountForUpdate(12L))
                .thenReturn(childSavingsAccount(
                        12L,
                        6L,
                        null
                ));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.createSchedule(
                        7L,
                        key,
                        createRequest()
                )
        );

        assertEquals(
                ErrorCode.INVALID_AUTO_TRANSFER_SCHEDULE,
                exception.getErrorCode()
        );
    }

    @Test
    void 잘못된_멱등키는_400_오류이다() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.createSchedule(
                        7L,
                        "not-a-uuid",
                        createRequest()
                )
        );

        assertEquals(
                ErrorCode.BADREQUEST,
                exception.getErrorCode()
        );
    }

    private CreateAutoTransferScheduleRequest createRequest() {
        CreateAutoTransferScheduleRequest request =
                new CreateAutoTransferScheduleRequest();

        ReflectionTestUtils.setField(
                request, "childId", 6L
        );
        ReflectionTestUtils.setField(
                request, "sourceAccountId", 1L
        );
        ReflectionTestUtils.setField(
                request, "destinationAccountId", 12L
        );
        ReflectionTestUtils.setField(
                request,
                "amount",
                new BigDecimal("80000")
        );
        ReflectionTestUtils.setField(
                request,
                "frequency",
                AutoTransferFrequency.MONTHLY
        );
        ReflectionTestUtils.setField(
                request, "transferDay", 10
        );
        ReflectionTestUtils.setField(
                request,
                "startDate",
                LocalDate.of(2026, 9, 10)
        );
        ReflectionTestUtils.setField(
                request,
                "endDate",
                LocalDate.of(2029, 2, 10)
        );

        return request;
    }

    private AutoTransferAccountRow parentAccount(
            Long accountId,
            Long memberId
    ) {
        AutoTransferAccountRow row =
                new AutoTransferAccountRow();

        row.setFinancialAccountId(accountId);
        row.setOwnerType("PARENT");
        row.setOwnerMemberId(memberId);
        row.setAccountProductType("DEMAND_DEPOSIT");
        row.setAccountStatus("ACTIVE");
        row.setLinkStatus("ACTIVE");

        return row;
    }

    private AutoTransferAccountRow childSavingsAccount(
            Long accountId,
            Long childId,
            Long financialGoalId
    ) {
        AutoTransferAccountRow row =
                new AutoTransferAccountRow();

        row.setFinancialAccountId(accountId);
        row.setOwnerType("CHILD");
        row.setChildId(childId);
        row.setAccountProductType("SAVINGS");
        row.setAccountStatus("ACTIVE");
        row.setLinkStatus("ACTIVE");
        row.setFinancialGoalId(financialGoalId);

        return row;
    }

    private AutoTransferScheduleRow existingSchedule(
            String key
    ) {
        AutoTransferScheduleRow row =
                new AutoTransferScheduleRow();

        row.setAutoTransferScheduleId(21L);
        row.setChildId(6L);
        row.setMemberId(7L);
        row.setRequestIdempotencyKey(key);
        row.setFinancialGoalId(31L);
        row.setSourceAccountId(1L);
        row.setDestinationAccountId(12L);
        row.setAmount(new BigDecimal("80000.00"));
        row.setFrequency(AutoTransferFrequency.MONTHLY);
        row.setTransferDay(10);
        row.setStartDate(LocalDate.of(2026, 9, 10));
        row.setEndDate(LocalDate.of(2029, 2, 10));
        row.setNextTransferAt(
                LocalDateTime.of(2026, 9, 10, 0, 0)
        );
        row.setStatus(AutoTransferScheduleStatus.ACTIVE);
        row.setCreatedAt(
                LocalDateTime.of(2026, 8, 17, 6, 0)
        );

        return row;
    }

    @Test
    void 자녀_자동이체_일정_목록을_커서로_조회한다() {
        when(mapper.countChildAccess(1L, 1L))
                .thenReturn(1);

        when(mapper.findSchedules(any(
                AutoTransferScheduleListQuery.class
        ))).thenReturn(
                List.of(
                        listRow(30L),
                        listRow(29L),
                        listRow(28L)
                )
        );

        var response = service.getSchedules(
                1L,
                1L,
                "ACTIVE",
                null,
                2
        );

        assertEquals(2, response.getItems().size());
        assertEquals("29", response.getNextCursor());
        assertEquals(true, response.isHasNext());
        assertEquals(
                30L,
                response.getItems()
                        .get(0)
                        .getAutoTransferScheduleId()
        );
    }

    @Test
    void 자녀_접근권한이_없으면_목록조회를_거절한다() {
        when(mapper.countChildAccess(1L, 99L))
                .thenReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.getSchedules(
                        99L,
                        1L,
                        null,
                        null,
                        null
                )
        );

        assertEquals(
                ErrorCode.CHILD_ACCESS_DENIED,
                exception.getErrorCode()
        );

        verify(mapper, never())
                .findSchedules(any());
    }

    @Test
    void 잘못된_상태값은_400으로_거절한다() {
        when(mapper.countChildAccess(1L, 1L))
                .thenReturn(1);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.getSchedules(
                        1L,
                        1L,
                        "UNKNOWN",
                        null,
                        20
                )
        );

        assertEquals(
                ErrorCode.BADREQUEST,
                exception.getErrorCode()
        );
    }

    private AutoTransferScheduleListRow listRow(
            Long scheduleId
    ) {
        AutoTransferScheduleListRow row =
                new AutoTransferScheduleListRow();

        row.setAutoTransferScheduleId(scheduleId);
        row.setFinancialGoalId(1L);
        row.setGoalTitle("대학자금 마련");
        row.setAmount(new BigDecimal("80000"));
        row.setFrequency(
                AutoTransferFrequency.MONTHLY
        );
        row.setTransferDay(10);
        row.setNextTransferAt(
                LocalDateTime.of(
                        2026,
                        9,
                        10,
                        0,
                        0
                )
        );
        row.setStatus(
                AutoTransferScheduleStatus.ACTIVE
        );

        return row;
    }

    @Test
    void 자동이체_일정_상세를_조회한다() {
        AutoTransferScheduleDetailRow row =
                detailRow();

        when(mapper.findScheduleDetail(21L))
                .thenReturn(row);
        when(mapper.countChildAccess(5L, 7L))
                .thenReturn(1);

        var response =
                service.getScheduleDetail(7L, 21L);

        assertEquals(
                21L,
                response.getAutoTransferScheduleId()
        );
        assertEquals(
                5L,
                response.getChildId()
        );
        assertEquals(
                31L,
                response.getFinancialGoalId()
        );
        assertEquals(
                "초등학교 입학 준비금",
                response.getGoalTitle()
        );
        assertEquals(
                17L,
                response.getSourceAccountId()
        );
        assertEquals(
                24L,
                response.getDestinationAccountId()
        );
        assertEquals(
                TransferStatus.FAILED,
                response.getLastTransferStatus()
        );
        assertEquals(
                "INSUFFICIENT_BALANCE",
                response.getLastFailureCode()
        );
    }

    @Test
    void 자녀_접근권한이_없으면_상세조회를_거절한다() {
        when(mapper.findScheduleDetail(21L))
                .thenReturn(detailRow());
        when(mapper.countChildAccess(5L, 99L))
                .thenReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.getScheduleDetail(
                        99L,
                        21L
                )
        );

        assertEquals(
                ErrorCode.CHILD_ACCESS_DENIED,
                exception.getErrorCode()
        );
    }

    private AutoTransferScheduleDetailRow detailRow() {
        AutoTransferScheduleDetailRow row =
                new AutoTransferScheduleDetailRow();

        row.setAutoTransferScheduleId(21L);
        row.setChildId(5L);
        row.setFinancialGoalId(31L);
        row.setGoalTitle("초등학교 입학 준비금");

        row.setSourceAccountId(17L);
        row.setSourceAccountName("KB국민 1234");

        row.setDestinationAccountId(24L);
        row.setDestinationAccountName("아이사랑적금");

        row.setAmount(new BigDecimal("80000"));
        row.setFrequency(
                AutoTransferFrequency.MONTHLY
        );
        row.setTransferDay(10);
        row.setStartDate(
                LocalDate.of(2026, 9, 10)
        );
        row.setEndDate(
                LocalDate.of(2029, 2, 10)
        );
        row.setNextTransferAt(
                LocalDateTime.of(
                        2026, 9, 10, 0, 0
                )
        );

        row.setLastTransferId(101L);
        row.setLastTransferStatus(
                TransferStatus.FAILED
        );
        row.setLastFailureCode(
                "INSUFFICIENT_BALANCE"
        );
        row.setLastFailureMessage(
                "출금 계좌의 잔액이 부족합니다."
        );
        row.setLastTransferredAt(
                LocalDateTime.of(
                        2026, 8, 10, 0, 0, 1
                )
        );

        row.setStatus(
                AutoTransferScheduleStatus.ACTIVE
        );
        row.setCreatedAt(
                LocalDateTime.of(
                        2026, 8, 17, 6, 0
                )
        );
        row.setUpdatedAt(
                LocalDateTime.of(
                        2026, 8, 17, 6, 0
                )
        );

        return row;
    }
}