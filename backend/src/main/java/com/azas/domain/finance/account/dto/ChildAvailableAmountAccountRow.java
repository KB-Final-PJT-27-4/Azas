package com.azas.domain.finance.account.dto;

import com.azas.domain.finance.account.entity.ChildUsageMode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class ChildAvailableAmountAccountRow {

    private Long accountId;
    private Long childId;
    private ChildUsageMode childUsageMode;
    private BigDecimal childMonthlyBudgetAmount;
}
