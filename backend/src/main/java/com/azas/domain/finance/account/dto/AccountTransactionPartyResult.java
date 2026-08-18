package com.azas.domain.finance.account.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class AccountTransactionPartyResult {

    private final String bankName;
    private final String accountName;
    private final String accountNumber;
}
