package com.azas.domain.finance.account.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class AccountOpenGoalRequest {
    @JsonProperty("financial_goal_template_id") private Long financialGoalTemplateId;
    private String title;
    @JsonProperty("target_amount") private BigDecimal targetAmount;
    @JsonProperty("target_date") private LocalDate targetDate;
    @JsonProperty("monthly_saving_amount") private BigDecimal monthlySavingAmount;
}
