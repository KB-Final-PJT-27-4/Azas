package com.azas.domain.finance.autotransfer.service;

import com.azas.domain.finance.autotransfer.dto.AutoTransferAccountRow;
import com.azas.domain.finance.autotransfer.dto.AutoTransferScheduleInsertCommand;
import com.azas.domain.finance.autotransfer.dto.AutoTransferScheduleRow;
import com.azas.domain.finance.autotransfer.dto.CreateAutoTransferScheduleRequest;
import com.azas.domain.finance.autotransfer.entity.AutoTransferFrequency;
import com.azas.domain.finance.autotransfer.entity.AutoTransferScheduleStatus;
import com.azas.domain.finance.autotransfer.mapper.AutoTransferScheduleMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
}