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
import com.azas.domain.finance.autotransfer.dto.UpdateAutoTransferScheduleCommand;
import com.azas.domain.finance.autotransfer.dto.UpdateAutoTransferScheduleRequest;
import com.azas.domain.finance.autotransfer.entity.AutoTransferAction;
import org.mockito.ArgumentCaptor;

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

    // 수정 테스트
    @Test
    void 자동이체_일정을_수정한다() {
        AutoTransferScheduleRow schedule =
                modifiableSchedule(
                        AutoTransferScheduleStatus.ACTIVE
                );

        UpdateAutoTransferScheduleRequest request =
                updateRequest(
                        AutoTransferAction.UPDATE,
                        new BigDecimal("100000"),
                        20,
                        LocalDate.of(2029, 3, 20),
                        true
                );

        when(mapper.findScheduleForUpdate(21L))
                .thenReturn(schedule);
        when(mapper.countChildAccess(5L, 7L))
                .thenReturn(1);
        when(mapper.countEquivalentScheduleExcludingId(
                21L,
                7L,
                5L,
                17L,
                24L,
                new BigDecimal("100000"),
                "MONTHLY",
                20,
                LocalDate.of(2026, 9, 10),
                LocalDate.of(2029, 3, 20)
        )).thenReturn(0);
        when(mapper.updateSchedule(any()))
                .thenReturn(1);
        when(mapper.findScheduleDetail(21L))
                .thenReturn(detailRow());

        service.updateSchedule(
                7L,
                21L,
                request
        );

        ArgumentCaptor<UpdateAutoTransferScheduleCommand>
                captor = ArgumentCaptor.forClass(
                UpdateAutoTransferScheduleCommand.class
        );

        verify(mapper).updateSchedule(
                captor.capture()
        );

        UpdateAutoTransferScheduleCommand command =
                captor.getValue();

        assertEquals(
                new BigDecimal("100000"),
                command.getAmount()
        );
        assertEquals(20, command.getTransferDay());
        assertEquals(
                LocalDate.of(2029, 3, 20),
                command.getEndDate()
        );
        assertEquals(
                LocalDateTime.of(2026, 9, 20, 0, 0),
                command.getNextTransferAt()
        );
        assertEquals(
                AutoTransferScheduleStatus.ACTIVE,
                command.getStatus()
        );
    }

    // 일시정지 테스트
    @Test
    void 활성_자동이체_일정을_일시정지한다() {
        AutoTransferScheduleRow schedule =
                modifiableSchedule(
                        AutoTransferScheduleStatus.ACTIVE
                );

        when(mapper.findScheduleForUpdate(21L))
                .thenReturn(schedule);
        when(mapper.countChildAccess(5L, 7L))
                .thenReturn(1);
        when(mapper.updateSchedule(any()))
                .thenReturn(1);
        when(mapper.findScheduleDetail(21L))
                .thenReturn(detailRow());

        service.updateSchedule(
                7L,
                21L,
                updateRequest(
                        AutoTransferAction.PAUSE,
                        null,
                        null,
                        null,
                        false
                )
        );

        ArgumentCaptor<UpdateAutoTransferScheduleCommand>
                captor = ArgumentCaptor.forClass(
                UpdateAutoTransferScheduleCommand.class
        );

        verify(mapper).updateSchedule(
                captor.capture()
        );

        assertEquals(
                AutoTransferScheduleStatus.PAUSED,
                captor.getValue().getStatus()
        );
        assertEquals(
                schedule.getNextTransferAt(),
                captor.getValue().getNextTransferAt()
        );
    }

    // 재개 테스트
    @Test
    void 일시정지된_자동이체_일정을_재개한다() {
        AutoTransferScheduleRow schedule =
                modifiableSchedule(
                        AutoTransferScheduleStatus.PAUSED
                );

        when(mapper.findScheduleForUpdate(21L))
                .thenReturn(schedule);
        when(mapper.countChildAccess(5L, 7L))
                .thenReturn(1);
        when(mapper.updateSchedule(any()))
                .thenReturn(1);
        when(mapper.findScheduleDetail(21L))
                .thenReturn(detailRow());

        service.updateSchedule(
                7L,
                21L,
                updateRequest(
                        AutoTransferAction.RESUME,
                        null,
                        null,
                        null,
                        false
                )
        );

        ArgumentCaptor<UpdateAutoTransferScheduleCommand>
                captor = ArgumentCaptor.forClass(
                UpdateAutoTransferScheduleCommand.class
        );

        verify(mapper).updateSchedule(
                captor.capture()
        );

        assertEquals(
                AutoTransferScheduleStatus.ACTIVE,
                captor.getValue().getStatus()
        );
        assertEquals(
                LocalDateTime.of(2026, 9, 10, 0, 0),
                captor.getValue().getNextTransferAt()
        );
    }

    // 종료된 일정 방지
    @Test
    void 종료된_자동이체_일정은_변경할_수_없다() {
        when(mapper.findScheduleForUpdate(21L))
                .thenReturn(
                        modifiableSchedule(
                                AutoTransferScheduleStatus.ENDED
                        )
                );
        when(mapper.countChildAccess(5L, 7L))
                .thenReturn(1);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.updateSchedule(
                        7L,
                        21L,
                        updateRequest(
                                AutoTransferAction.PAUSE,
                                null,
                                null,
                                null,
                                false
                        )
                )
        );

        assertEquals(
                ErrorCode.INVALID_AUTO_TRANSFER_STATUS_TRANSITION,
                exception.getErrorCode()
        );

        verify(mapper, never())
                .updateSchedule(any());
    }

    // 다른 보호자 일정 변경 방지
    @Test
    void 다른_보호자의_자동이체_일정은_변경할_수_없다() {
        when(mapper.findScheduleForUpdate(21L))
                .thenReturn(
                        modifiableSchedule(
                                AutoTransferScheduleStatus.ACTIVE
                        )
                );
        when(mapper.countChildAccess(5L, 99L))
                .thenReturn(1);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.updateSchedule(
                        99L,
                        21L,
                        updateRequest(
                                AutoTransferAction.PAUSE,
                                null,
                                null,
                                null,
                                false
                        )
                )
        );

        assertEquals(
                ErrorCode.AUTO_TRANSFER_SCHEDULE_ACCESS_DENIED,
                exception.getErrorCode()
        );
    }

    private AutoTransferScheduleRow modifiableSchedule(
            AutoTransferScheduleStatus status
    ) {
        AutoTransferScheduleRow row =
                new AutoTransferScheduleRow();

        row.setAutoTransferScheduleId(21L);
        row.setChildId(5L);
        row.setMemberId(7L);
        row.setFinancialGoalId(31L);
        row.setSourceAccountId(17L);
        row.setDestinationAccountId(24L);
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
        row.setStatus(status);
        row.setCreatedAt(
                LocalDateTime.of(
                        2026, 8, 17, 6, 0
                )
        );

        return row;
    }

    private UpdateAutoTransferScheduleRequest updateRequest(
            AutoTransferAction action,
            BigDecimal amount,
            Integer transferDay,
            LocalDate endDate,
            boolean endDatePresent
    ) {
        UpdateAutoTransferScheduleRequest request =
                new UpdateAutoTransferScheduleRequest();

        ReflectionTestUtils.setField(
                request,
                "action",
                action
        );
        ReflectionTestUtils.setField(
                request,
                "amount",
                amount
        );
        ReflectionTestUtils.setField(
                request,
                "transferDay",
                transferDay
        );
        ReflectionTestUtils.setField(
                request,
                "endDate",
                endDate
        );
        ReflectionTestUtils.setField(
                request,
                "endDatePresent",
                endDatePresent
        );

        return request;
    }
}