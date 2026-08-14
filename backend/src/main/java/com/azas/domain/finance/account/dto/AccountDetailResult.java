package com.azas.domain.finance.account.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor
public final class AccountDetailResult {

    private final Long accountId;
    private final String ownerType;
    private final String bankName;
    private final String accountName;
    private final String accountNumber;
    private final String accountHolderName;
    private final String accountProductType;
    private final BigDecimal balance;
}
