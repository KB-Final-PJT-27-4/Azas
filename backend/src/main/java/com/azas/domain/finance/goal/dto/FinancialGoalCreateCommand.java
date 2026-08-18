package com.azas.domain.finance.goal.dto;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
public class FinancialGoalCreateCommand {
    private final Long financialGoalTemplateId;
    private final String title;
    private final BigDecimal targetAmount;
    private final LocalDate targetDate;
    private final List<Long> accountIds;

    public FinancialGoalCreateCommand(Long financialGoalTemplateId, String title,
                                      BigDecimal targetAmount, LocalDate targetDate,
                                      List<Long> accountIds) {
        this.financialGoalTemplateId = financialGoalTemplateId;
        this.title = title;
        this.targetAmount = targetAmount;
        this.targetDate = targetDate;
        this.accountIds = accountIds;
    }
}
