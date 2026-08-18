package com.azas.domain.finance.goal.dto;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Getter
public class FinancialGoalCreateResult {
    private final long financialGoalId;
    private final long childId;
    private final Long financialGoalTemplateId;
    private final String title;
    private final BigDecimal targetAmount;
    private final LocalDate targetDate;
    private final BigDecimal monthlySavingAmount;
    private final BigDecimal currentAmount;
    private final BigDecimal remainingAmount;
    private final BigDecimal achievementRate;
    private final String status;
    private final List<FinancialGoalLinkedAccountResult> linkedAccounts;
    private final Instant createdAt;

    public FinancialGoalCreateResult(long financialGoalId, long childId,
                                     Long financialGoalTemplateId, String title,
                                     BigDecimal targetAmount, LocalDate targetDate,
                                     BigDecimal monthlySavingAmount,
                                     BigDecimal currentAmount,
                                     BigDecimal remainingAmount,
                                     BigDecimal achievementRate, String status,
                                     List<FinancialGoalLinkedAccountResult> linkedAccounts,
                                     Instant createdAt) {
        this.financialGoalId = financialGoalId;
        this.childId = childId;
        this.financialGoalTemplateId = financialGoalTemplateId;
        this.title = title;
        this.targetAmount = targetAmount;
        this.targetDate = targetDate;
        this.monthlySavingAmount = monthlySavingAmount;
        this.currentAmount = currentAmount;
        this.remainingAmount = remainingAmount;
        this.achievementRate = achievementRate;
        this.status = status;
        this.linkedAccounts = List.copyOf(linkedAccounts);
        this.createdAt = createdAt;
    }
}
