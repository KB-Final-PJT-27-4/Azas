package com.azas.domain.finance.goal.dto;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class FinancialGoalLinkedAccountResult {
    private final long accountId;
    private final String accountName;
    private final String bankName;
    private final BigDecimal balance;

    public FinancialGoalLinkedAccountResult(long accountId, String accountName,
                                             String bankName, BigDecimal balance) {
        this.accountId = accountId;
        this.accountName = accountName;
        this.bankName = bankName;
        this.balance = balance;
    }
}
