package com.azas.domain.finance.goal.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class FinancialGoalDetailRow {

    private Long financialGoalId;
    private Long childId;
    private Long financialGoalTemplateId;
    private String title;
    private String iconKey;
    private BigDecimal targetAmount;
    private LocalDate targetDate;
    private BigDecimal monthlySavingAmount;
    private String status;
    private Long accountId;
    private String accountName;
    private String bankName;
    private byte[] accountNumberCiphertext;
    private BigDecimal balance;
}
