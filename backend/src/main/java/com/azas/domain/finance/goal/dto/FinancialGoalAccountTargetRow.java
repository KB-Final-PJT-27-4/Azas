package com.azas.domain.finance.goal.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class FinancialGoalAccountTargetRow {

    private Long accountId;
    private String ownerType;
    private Long childId;
    private String bankName;
    private String accountName;
    private String accountProductType;
    private BigDecimal balance;
    private String accountStatus;
    private String linkStatus;
    private Long financialGoalId;
}
