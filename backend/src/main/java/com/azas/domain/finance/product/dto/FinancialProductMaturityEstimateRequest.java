package com.azas.domain.finance.product.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class FinancialProductMaturityEstimateRequest {

    @NotNull
    @DecimalMin(value = "0", inclusive = false)
    @Digits(integer = 19, fraction = 0)
    @JsonProperty("monthly_amount")
    private BigDecimal monthlyAmount;

    @NotNull
    @Min(1)
    @JsonProperty("period_months")
    private Integer periodMonths;

    public FinancialProductMaturityEstimateRequest(
            BigDecimal monthlyAmount,
            Integer periodMonths
    ) {
        this.monthlyAmount = monthlyAmount;
        this.periodMonths = periodMonths;
    }
}
