package com.azas.domain.finance.goal.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class FinancialGoalUpdateTargetRow {

    private Long financialGoalId;
    private Long childId;
    private Long financialGoalTemplateId;
    private String title;
    private BigDecimal targetAmount;
    private LocalDate targetDate;
    private String status;
}
