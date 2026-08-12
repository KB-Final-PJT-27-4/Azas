package com.azas.domain.finance.account.dto;

import com.azas.domain.finance.account.entity.ChildUsageMode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class ChildAvailableAmountResult {

    private final Long childId;
    private final Long accountId;
    private final ChildUsageMode childUsageMode;
    private final BigDecimal childMonthlyBudgetAmount;
    private final BigDecimal currentMonthSpentAmount;
    private final BigDecimal remainingGuidanceAmount;
    private final Boolean budgetExceeded;
    private final String period;
    private final LocalDateTime calculatedAt;
}
