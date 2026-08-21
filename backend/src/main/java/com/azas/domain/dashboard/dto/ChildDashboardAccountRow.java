package com.azas.domain.dashboard.dto;

import com.azas.domain.finance.account.entity.ChildUsageMode;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ChildDashboardAccountRow {

    private Long accountId;
    private BigDecimal accountBalance;
    private ChildUsageMode childUsageMode;
    private BigDecimal monthlyBudgetAmount;
    private BigDecimal currentMonthSpentAmount;
}
