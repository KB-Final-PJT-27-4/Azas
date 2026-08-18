package com.azas.domain.finance.goal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
public class FinancialGoalCreateRequest {

    @JsonProperty("financial_goal_template_id")
    private Long financialGoalTemplateId;

    private String title;

    @JsonProperty("target_amount")
    private BigDecimal targetAmount;

    @JsonProperty("target_date")
    private LocalDate targetDate;

    @JsonProperty("account_ids")
    private List<Long> accountIds;

    public FinancialGoalCreateRequest(
            Long financialGoalTemplateId,
            String title,
            BigDecimal targetAmount,
            LocalDate targetDate,
            List<Long> accountIds
    ) {
        this.financialGoalTemplateId = financialGoalTemplateId;
        this.title = title;
        this.targetAmount = targetAmount;
        this.targetDate = targetDate;
        this.accountIds = accountIds;
    }

    public FinancialGoalCreateCommand toCommand() {
        return new FinancialGoalCreateCommand(
                financialGoalTemplateId,
                title,
                targetAmount,
                targetDate,
                accountIds
        );
    }
}
