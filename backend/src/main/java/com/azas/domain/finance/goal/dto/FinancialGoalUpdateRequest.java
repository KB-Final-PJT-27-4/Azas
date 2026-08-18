package com.azas.domain.finance.goal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
public class FinancialGoalUpdateRequest {

    @JsonProperty("target_amount")
    private BigDecimal targetAmount;

    @JsonProperty("target_date")
    private LocalDate targetDate;

    @JsonProperty("account_ids")
    private List<Long> accountIds;

    public FinancialGoalUpdateRequest(
            BigDecimal targetAmount,
            LocalDate targetDate,
            List<Long> accountIds
    ) {
        this.targetAmount = targetAmount;
        this.targetDate = targetDate;
        this.accountIds = accountIds;
    }
}
