package com.azas.domain.finance.goal.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor
public final class FinancialGoalListAccountResult {

    private final long accountId;
    private final String accountName;
    private final String bankName;
    private final String accountNumber;
    private final BigDecimal balance;
}
