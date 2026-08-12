package com.azas.domain.finance.account.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public final class AccountTransactionListResult {

    private final Long accountId;
    private final List<AccountTransactionItemResult> transactions;
    private final String nextCursor;
    private final boolean hasNext;
}
