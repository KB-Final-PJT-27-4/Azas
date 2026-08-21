package com.azas.domain.finance.goal.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@NoArgsConstructor
public class FinancialGoalCheckpointRow {

    private Long financialGoalCheckpointId;
    private Integer percentage;
    private BigDecimal targetAmount;
    private Instant reachedAt;
}
