package com.azas.domain.finance.goal.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class FinancialGoalListRow {

    private Long financialGoalId;
    private Long childId;
    private Long financialGoalTemplateId;
    private String title;
    private String iconKey;
    private BigDecimal targetAmount;
    private LocalDate targetDate;
    private String status;
    private LocalDateTime createdAt;
    private Long accountId;
    private String accountName;
    private String bankName;
    private byte[] accountNumberCiphertext;
    private BigDecimal balance;
}
