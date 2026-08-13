package com.azas.domain.finance.account.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class LinkedAccountResult {
    private final Long accountId;
    private final String ownerType;
    private final Long childId;
    private final String bankName;
    private final String accountName;
    private final String accountNumber;
    private final String accountProductType;
    private final BigDecimal balance;
    private final String accountStatus;
    private final String linkStatus;
    private final boolean primary;
    private final boolean requiresGoalSetup;
    private final LocalDateTime linkedAt;
}
