package com.azas.domain.finance.account.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.YearMonth;
import java.util.List;

@Getter
@RequiredArgsConstructor
public final class AccountBalanceHistoryResult {

    private final long accountId;
    private final int months;
    private final YearMonth startMonth;
    private final YearMonth endMonth;
    private final List<MonthlyAccountBalanceResult> balanceHistory;
}
