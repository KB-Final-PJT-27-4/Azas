package com.azas.domain.finance.account.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor
public final class ChildAccountListItemResult {

    private final Long accountId;
    private final String accountName;
    private final String accountNumber;
    private final String accountProductType;
    private final BigDecimal balance;
    private final boolean isPrimary;
}
