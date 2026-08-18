package com.azas.domain.finance.goal.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class FinancialGoalInsertCommand {

    @Setter
    private Long financialGoalId;
    private final long childId;
    private final Long financialGoalTemplateId;
    private final String title;
    private final BigDecimal targetAmount;
    private final LocalDate targetDate;
    private final BigDecimal monthlySavingAmount;
    private final LocalDateTime createdAt;

    public FinancialGoalInsertCommand(
            long childId,
            Long financialGoalTemplateId,
            String title,
            BigDecimal targetAmount,
            LocalDate targetDate,
            BigDecimal monthlySavingAmount,
            LocalDateTime createdAt
    ) {
        this.childId = childId;
        this.financialGoalTemplateId = financialGoalTemplateId;
        this.title = title;
        this.targetAmount = targetAmount;
        this.targetDate = targetDate;
        this.monthlySavingAmount = monthlySavingAmount;
        this.createdAt = createdAt;
    }
}
