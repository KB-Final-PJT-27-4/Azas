package com.azas.domain.finance.account.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public final class ChildAccountListItemResult {

    private final Long accountId;
    private final String organizationCode;
    private final String bankName;
    private final String accountName;
    private final String accountNumber;
    private final String accountProductType;
    private final BigDecimal balance;
    private final LocalDateTime balanceUpdatedAt;
    private final String accountStatus;
    private final boolean primary;
}
