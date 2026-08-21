package com.azas.domain.finance.goal.dto;

import com.azas.domain.finance.goal.entity.FinancialGoalAmountRecommendation;
import com.azas.domain.finance.goal.entity.FinancialGoalRecommendationBasis;
import com.azas.domain.finance.goal.entity.FinancialGoalTemplate;
import lombok.Getter;

import java.util.List;

@Getter
public class FinancialGoalAmountRecommendationResult {

    private final FinancialGoalTemplate template;
    private final FinancialGoalRecommendationBasis basis;
    private final List<FinancialGoalAmountRecommendation> recommendations;

    public FinancialGoalAmountRecommendationResult(
            FinancialGoalTemplate template,
            FinancialGoalRecommendationBasis basis,
            List<FinancialGoalAmountRecommendation> recommendations
    ) {
        this.template = template;
        this.basis = basis;
        this.recommendations = List.copyOf(recommendations);
    }
}
