package com.azas.domain.finance.goal.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@RequiredArgsConstructor
public final class FinancialGoalListItemResult {

    private final long financialGoalId;
    private final Long financialGoalTemplateId;
    private final String title;
    private final String iconKey;
    private final BigDecimal targetAmount;
    private final BigDecimal currentAmount;
    private final BigDecimal remainingAmount;
    private final BigDecimal achievementRate;
    private final LocalDate targetDate;
    private final String status;
    private final List<FinancialGoalListAccountResult> linkedAccounts;
}
