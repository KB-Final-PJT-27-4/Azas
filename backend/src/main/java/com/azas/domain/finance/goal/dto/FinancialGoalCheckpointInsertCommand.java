package com.azas.domain.finance.goal.dto;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class FinancialGoalCheckpointInsertCommand {

    private final long financialGoalId;
    private final int percentage;
    private final BigDecimal targetAmount;
    private final LocalDateTime reachedAt;
    private final LocalDateTime createdAt;

    public FinancialGoalCheckpointInsertCommand(
            long financialGoalId,
            int percentage,
            BigDecimal targetAmount,
            LocalDateTime reachedAt,
            LocalDateTime createdAt
    ) {
        this.financialGoalId = financialGoalId;
        this.percentage = percentage;
        this.targetAmount = targetAmount;
        this.reachedAt = reachedAt;
        this.createdAt = createdAt;
    }
}
