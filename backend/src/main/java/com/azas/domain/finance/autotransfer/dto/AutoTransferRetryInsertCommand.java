package com.azas.domain.finance.autotransfer.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class AutoTransferRetryInsertCommand {

    private Long financialTransferId;
    private final Long childId;
    private final Long memberId;
    private final Long financialGoalId;
    private final Long sourceAccountId;
    private final Long destinationAccountId;
    private final BigDecimal amount;
    private final Long scheduleId;
    private final Long retryOfTransferId;
    private final String idempotencyKey;
    private final LocalDateTime requestedAt;
}