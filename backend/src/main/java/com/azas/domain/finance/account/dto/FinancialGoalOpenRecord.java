package com.azas.domain.finance.account.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class FinancialGoalOpenRecord {
    @Setter
    private Long financialGoalId;
    private final long childId;
    private final long financialAccountId;
    private final Long financialGoalTemplateId;
    private final String title;
    private final BigDecimal targetAmount;
    private final LocalDate targetDate;
    private final BigDecimal monthlySavingAmount;
    private final LocalDateTime createdAt;
}
