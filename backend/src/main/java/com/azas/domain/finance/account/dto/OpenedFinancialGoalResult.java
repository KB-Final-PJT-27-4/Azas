package com.azas.domain.finance.account.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@RequiredArgsConstructor
public class OpenedFinancialGoalResult {
    @JsonProperty("financial_goal_id") private final Long financialGoalId;
    private final String title;
    @JsonProperty("target_amount") private final BigDecimal targetAmount;
    @JsonProperty("target_date") private final LocalDate targetDate;
    private final String status;
}
