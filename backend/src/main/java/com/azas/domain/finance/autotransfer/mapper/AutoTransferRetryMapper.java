package com.azas.domain.finance.autotransfer.mapper;

import com.azas.domain.finance.autotransfer.dto.*;
import com.azas.domain.finance.transfer.dto.TransferTransactionInsertCommand;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Mapper
public interface AutoTransferRetryMapper {

    AutoTransferRetryRow findByIdempotencyKey(
            @Param("idempotencyKey") String idempotencyKey
    );

    AutoTransferRetryRow findLatestTransferForUpdate(
            @Param("scheduleId") Long scheduleId
    );

    AutoTransferExecutionAccountRow findAccountForExecutionForUpdate(
            @Param("accountId") Long accountId
    );

    int insertRetryTransfer(
            AutoTransferRetryInsertCommand command
    );

    int decreaseSourceBalance(
            @Param("accountId") Long accountId,
            @Param("amount") BigDecimal amount
    );

    int increaseDestinationBalance(
            @Param("accountId") Long accountId,
            @Param("amount") BigDecimal amount
    );

    int insertDestinationBalanceSnapshot(
            @Param("accountId") Long accountId,
            @Param("childId") Long childId,
            @Param("balance") BigDecimal balance,
            @Param("observedAt") LocalDateTime observedAt
    );

    int insertTransaction(
            TransferTransactionInsertCommand command
    );

    int markRetrySucceeded(
            @Param("transferId") Long transferId,
            @Param("debitTransactionId") Long debitTransactionId,
            @Param("creditTransactionId") Long creditTransactionId,
            @Param("completedAt") LocalDateTime completedAt
    );

    int markRetryFailed(
            @Param("transferId") Long transferId,
            @Param("failureCode") String failureCode,
            @Param("failureMessage") String failureMessage,
            @Param("completedAt") LocalDateTime completedAt
    );

    int updateScheduleLastResult(
            @Param("scheduleId") Long scheduleId,
            @Param("status") String status,
            @Param("transferredAt") LocalDateTime transferredAt
    );

    int insertResultNotification(
            @Param("memberId") Long memberId,
            @Param("childId") Long childId,
            @Param("scheduleId") Long scheduleId,
            @Param("transferId") Long transferId,
            @Param("status") String status,
            @Param("amount") BigDecimal amount,
            @Param("failureCode") String failureCode,
            @Param("failureMessage") String failureMessage,
            @Param("createdAt") LocalDateTime createdAt
    );
}
