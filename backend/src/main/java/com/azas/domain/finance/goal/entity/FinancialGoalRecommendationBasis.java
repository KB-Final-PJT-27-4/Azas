package com.azas.domain.finance.goal.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class FinancialGoalRecommendationBasis {

    private Long financialGoalRecommendationBasisId;
    private Long financialGoalTemplateId;
    private String recommendationMethod;
    private String organization;
    private String datasetName;
    private Integer referenceYear;
    private String metricName;
    private BigDecimal metricValue;
    private String metricUnit;
    private String sourceUrl;
    private String description;
    private String disclaimer;
}
