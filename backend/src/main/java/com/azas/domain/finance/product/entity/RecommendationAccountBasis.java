package com.azas.domain.finance.product.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class RecommendationAccountBasis {

    private Long financialAccountId;
    private String goalName;
    private BigDecimal goalTargetAmount;
    private LocalDate goalTargetDate;
    private BigDecimal balance;
    private LocalDate maturityDate;
}
