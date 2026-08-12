package com.azas.domain.finance.account.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public final class AccountTransactionItemResult {

    private final Long accountTransactionId;
    private final LocalDateTime occurredAt;
    private final String direction;
    private final BigDecimal amount;
    private final String memo;
    private final AccountTransactionAccountResult depositAccount;
    private final AccountTransactionAccountResult withdrawalAccount;
    private final BigDecimal balanceAfter;
}
