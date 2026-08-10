package com.azas.domain.finance.account.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@RequiredArgsConstructor
public final class AccountFinancialGoalResult {

    private final String goalName;
    private final BigDecimal targetAmount;
    private final LocalDate targetDate;
}
