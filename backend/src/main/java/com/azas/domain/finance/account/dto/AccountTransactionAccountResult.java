package com.azas.domain.finance.account.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class AccountTransactionAccountResult {

    private final Long accountId;
    private final String bankName;
    private final String accountName;
    private final String accountNumber;
}
