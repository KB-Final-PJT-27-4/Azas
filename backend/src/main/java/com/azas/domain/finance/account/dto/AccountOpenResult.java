package com.azas.domain.finance.account.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class AccountOpenResult {
    private final Long accountId;
    private final String ownerType;
    private final Long childId;
    private final Long financialProductId;
    private final String bankName;
    private final String accountName;
    private final String accountNumber;
    private final String accountProductType;
    private final BigDecimal balance;
    private final boolean primary;
    private final OpenedFinancialGoalResult financialGoal;
    private final LocalDateTime createdAt;
}
