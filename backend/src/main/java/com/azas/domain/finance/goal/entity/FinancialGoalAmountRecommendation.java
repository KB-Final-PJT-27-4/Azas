package com.azas.domain.finance.goal.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class FinancialGoalAmountRecommendation {

    private Long financialGoalAmountRecommendationId;
    private Long financialGoalTemplateId;
    private String recommendationCode;
    private String title;
    private BigDecimal targetAmount;
    private String coverageItems;
    private Integer displayOrder;
}
