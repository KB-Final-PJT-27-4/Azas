package com.azas.domain.finance.autotransfer.service;

import org.junit.jupiter.api.Test;

import com.azas.domain.finance.autotransfer.dto.AutoTransferExecutionAccountRow;
import com.azas.domain.finance.autotransfer.dto.AutoTransferRetryInsertCommand;
import com.azas.domain.finance.autotransfer.dto.AutoTransferRetryResponse;
import com.azas.domain.finance.autotransfer.dto.AutoTransferRetryRow;
import com.azas.domain.finance.autotransfer.dto.AutoTransferScheduleRow;
import com.azas.domain.finance.autotransfer.entity.AutoTransferScheduleStatus;
import com.azas.domain.finance.autotransfer.mapper.AutoTransferRetryMapper;
import com.azas.domain.finance.autotransfer.mapper.AutoTransferScheduleMapper;
import com.azas.domain.finance.transfer.dto.TransferTransactionInsertCommand;
import com.azas.domain.finance.transfer.entity.TransferStatus;
import com.azas.domain.notification.service.PushNotificationPublisher;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class AutoTransferRetryServiceImplTest {

    private static final String KEY =
            "93c7a60c-5664-4994-b8f5-b77aa573403d";

    private AutoTransferScheduleMapper scheduleMapper;
    private AutoTransferRetryMapper retryMapper;
    private AutoTransferRetryServiceImpl service;
    private PushNotificationPublisher pushNotificationPublisher;

    @BeforeEach
    void setUp() {
        scheduleMapper =
                mock(AutoTransferScheduleMapper.class);

        retryMapper =
                mock(AutoTransferRetryMapper.class);
        pushNotificationPublisher =
                mock(PushNotificationPublisher.class);

        Clock clock = Clock.fixed(
                Instant.parse("2026-08-18T01:30:00Z"),
                ZoneOffset.UTC
        );

        service = new AutoTransferRetryServiceImpl(
                scheduleMapper,
                retryMapper,
                clock,
                pushNotificationPublisher
        );
    }

    // 성공 테스트
    @Test
    void 실패한_자동이체를_성공적으로_재시도한다() {

        when(scheduleMapper.findScheduleForUpdate(21L))
                .thenReturn(activeSchedule());
        when(scheduleMapper.countChildAccess(5L, 7L))
                .thenReturn(1);
        when(retryMapper.findByIdempotencyKey(KEY))
                .thenReturn(null);
        when(retryMapper.findLatestTransferForUpdate(21L))
                .thenReturn(failedTransfer(501L));
        when(retryMapper.findAccountForExecutionForUpdate(1L))
                .thenReturn(sourceAccount(new BigDecimal("200000")));
        when(retryMapper.findAccountForExecutionForUpdate(3L))
                .thenReturn(destinationDemandDepositAccount(
                        new BigDecimal("500000")
                ));

        when(retryMapper.insertRetryTransfer(any()))
                .thenAnswer(invocation -> {
                    AutoTransferRetryInsertCommand command =
                            invocation.getArgument(0);
                    command.setFinancialTransferId(502L);
                    return 1;
                });

        when(retryMapper.decreaseSourceBalance(
                1L,
                new BigDecimal("100000")
        )).thenReturn(1);

        when(retryMapper.increaseDestinationBalance(
                3L,
                new BigDecimal("100000")
        )).thenReturn(1);

        when(retryMapper.insertTransaction(any()))
                .thenAnswer(invocation -> {
                    TransferTransactionInsertCommand command =
                            invocation.getArgument(0);
                    command.setAccountTransactionId(
                            command.getChildId() == null
                                    ? 9001L
                                    : 9002L
                    );
                    return 1;
                });

        when(retryMapper.markRetrySucceeded(
                eq(502L),
                eq(9001L),
                eq(9002L),
                any(LocalDateTime.class)
        )).thenReturn(1);

        when(retryMapper.updateScheduleLastResult(
                eq(21L),
                eq("SUCCEEDED"),
                any(LocalDateTime.class)
        )).thenReturn(1);
        when(retryMapper.insertResultNotification(
                eq(7L),
                eq(5L),
                eq(21L),
                eq(502L),
                eq("SUCCEEDED"),
                eq(new BigDecimal("100000")),
                eq(null),
                eq(null),
                any(LocalDateTime.class)
        )).thenReturn(1);

        AutoTransferRetryResponse response =
                service.retry(7L, 21L, KEY);

        assertEquals(502L, response.getFinancialTransferId());
        assertEquals(501L, response.getRetryOfTransferId());
        assertEquals(
                TransferStatus.SUCCEEDED,
                response.getStatus()
        );
        verify(pushNotificationPublisher).publish(
                eq(7L),
                argThat(message ->
                        "자동이체가 완료되었어요".equals(
                                message.getTitle()
                        )
                                && "/assets".equals(
                                message.getActionUrl()
                        )
                                && "AUTO_TRANSFER_SUCCEEDED".equals(
                                message.getData().get(
                                        "notification_type"
                                )
                        )
                )
        );
    }

    // 잔액 부족 실패 이력 보존
    @Test
    void 잔액이_부족하면_실패_이력을_저장한다() {
        when(scheduleMapper.findScheduleForUpdate(21L))
                .thenReturn(activeSchedule());
        when(scheduleMapper.countChildAccess(5L, 7L))
                .thenReturn(1);
        when(retryMapper.findByIdempotencyKey(KEY))
                .thenReturn(null);
        when(retryMapper.findLatestTransferForUpdate(21L))
                .thenReturn(failedTransfer(501L));
        when(retryMapper.findAccountForExecutionForUpdate(1L))
                .thenReturn(sourceAccount(new BigDecimal("10000")));
        when(retryMapper.findAccountForExecutionForUpdate(3L))
                .thenReturn(destinationAccount(new BigDecimal("500000")));

        when(retryMapper.insertRetryTransfer(any()))
                .thenAnswer(invocation -> {
                    AutoTransferRetryInsertCommand command =
                            invocation.getArgument(0);
                    command.setFinancialTransferId(502L);
                    return 1;
                });

        when(retryMapper.decreaseSourceBalance(
                1L,
                new BigDecimal("100000")
        )).thenReturn(0);

        when(retryMapper.markRetryFailed(
                eq(502L),
                eq("INSUFFICIENT_BALANCE"),
                anyString(),
                any(LocalDateTime.class)
        )).thenReturn(1);

        when(retryMapper.updateScheduleLastResult(
                eq(21L),
                eq("FAILED"),
                any(LocalDateTime.class)
        )).thenReturn(1);

        AutoTransferRetryResponse response =
                service.retry(7L, 21L, KEY);

        assertEquals(TransferStatus.FAILED, response.getStatus());
        assertEquals(
                "INSUFFICIENT_BALANCE",
                response.getFailureCode()
        );

        verify(retryMapper, never())
                .increaseDestinationBalance(anyLong(), any());
        verify(retryMapper, never())
                .insertTransaction(any());
    }

    // 최근 결과가 성공이면 재시도 불가
    @Test
    void 최근_자동이체가_성공이면_재시도할_수_없다() {
        when(scheduleMapper.findScheduleForUpdate(21L))
                .thenReturn(activeSchedule());
        when(scheduleMapper.countChildAccess(5L, 7L))
                .thenReturn(1);
        when(retryMapper.findByIdempotencyKey(KEY))
                .thenReturn(null);

        AutoTransferRetryRow succeeded = new AutoTransferRetryRow();
        succeeded.setStatus(TransferStatus.SUCCEEDED);

        when(retryMapper.findLatestTransferForUpdate(21L))
                .thenReturn(succeeded);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.retry(7L, 21L, KEY)
        );

        assertEquals(
                ErrorCode.AUTO_TRANSFER_RETRY_NOT_AVAILABLE,
                exception.getErrorCode()
        );

        verify(retryMapper, never())
                .insertRetryTransfer(any());
    }

    // 같은 Idempotency-Key 재호출
    @Test
    void 같은_멱등성_키는_기존_재시도_결과를_반환한다() {
        when(scheduleMapper.findScheduleForUpdate(21L))
                .thenReturn(activeSchedule());
        when(scheduleMapper.countChildAccess(5L, 7L))
                .thenReturn(1);

        AutoTransferRetryRow existing = new AutoTransferRetryRow();
        existing.setFinancialTransferId(502L);
        existing.setOriginId(21L);
        existing.setRetryOfTransferId(501L);
        existing.setStatus(TransferStatus.SUCCEEDED);
        existing.setRequestedAt(
                LocalDateTime.of(2026, 8, 18, 1, 30)
        );
        existing.setCompletedAt(
                LocalDateTime.of(2026, 8, 18, 1, 30, 1)
        );

        when(retryMapper.findByIdempotencyKey(KEY))
                .thenReturn(existing);

        AutoTransferRetryResponse response =
                service.retry(7L, 21L, KEY);

        assertEquals(502L, response.getFinancialTransferId());

        verify(retryMapper, never())
                .findLatestTransferForUpdate(anyLong());
        verify(retryMapper, never())
                .insertRetryTransfer(any());
    }

    private AutoTransferScheduleRow activeSchedule() {
        AutoTransferScheduleRow row =
                new AutoTransferScheduleRow();

        row.setAutoTransferScheduleId(21L);
        row.setChildId(5L);
        row.setMemberId(7L);
        row.setFinancialGoalId(31L);
        row.setSourceAccountId(1L);
        row.setDestinationAccountId(3L);
        row.setAmount(new BigDecimal("100000"));
        row.setStatus(AutoTransferScheduleStatus.ACTIVE);

        return row;
    }


    private AutoTransferExecutionAccountRow sourceAccount(
            BigDecimal balance
    ) {
        AutoTransferExecutionAccountRow row =
                new AutoTransferExecutionAccountRow();

        row.setFinancialAccountId(1L);
        row.setOwnerType("PARENT");
        row.setOwnerMemberId(7L);
        row.setChildId(null);
        row.setAccountName("부모 생활비 계좌");
        row.setAccountProductType("DEMAND_DEPOSIT");
        row.setAccountStatus("ACTIVE");
        row.setLinkStatus("ACTIVE");
        row.setBalance(balance);

        return row;
    }

    private AutoTransferExecutionAccountRow destinationAccount(
            BigDecimal balance
    ) {
        AutoTransferExecutionAccountRow row =
                new AutoTransferExecutionAccountRow();

        row.setFinancialAccountId(3L);
        row.setOwnerType("CHILD");
        row.setOwnerMemberId(null);
        row.setChildId(5L);
        row.setAccountName("자녀 저축 계좌");
        row.setAccountProductType("SAVINGS");
        row.setAccountStatus("ACTIVE");
        row.setLinkStatus("ACTIVE");
        row.setBalance(balance);

        return row;
    }

    private AutoTransferExecutionAccountRow
    destinationDemandDepositAccount(BigDecimal balance) {
        AutoTransferExecutionAccountRow row =
                destinationAccount(balance);
        row.setAccountProductType("DEMAND_DEPOSIT");
        row.setAccountName("자녀 입출금 계좌");
        return row;
    }
    private AutoTransferRetryRow failedTransfer(
            Long transferId
    ) {
        AutoTransferRetryRow row =
                new AutoTransferRetryRow();

        row.setFinancialTransferId(transferId);
        row.setOriginId(21L);
        row.setStatus(TransferStatus.FAILED);
        row.setFailureCode("INSUFFICIENT_BALANCE");
        row.setFailureMessage(
                "출금 계좌의 잔액이 부족합니다."
        );
        row.setRequestedAt(
                LocalDateTime.of(2026, 8, 17, 6, 0)
        );
        row.setCompletedAt(
                LocalDateTime.of(2026, 8, 17, 6, 0, 1)
        );

        return row;
    }
}
