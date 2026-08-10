package com.azas.domain.finance.account.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Getter
@RequiredArgsConstructor
public final class MonthlyAccountBalanceResult {

    private final YearMonth month;
    private final BigDecimal balance;
    private final BigDecimal changeAmount;
    private final LocalDateTime observedAt;
}
