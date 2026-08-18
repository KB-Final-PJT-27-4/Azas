package com.azas.domain.finance.account.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public final class AccountBalanceResult {

    private final Long accountId;
    private final BigDecimal balance;
    private final LocalDateTime balanceUpdatedAt;
}
