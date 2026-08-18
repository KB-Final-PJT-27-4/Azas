package com.azas.domain.finance.goal.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FinancialGoalTemplate {

    private Long financialGoalTemplateId;
    private String goalName;
    private String description;
    private String iconKey;
    private Integer sortOrder;
}
