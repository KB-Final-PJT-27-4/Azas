package com.azas.domain.finance.product.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class FinancialProductMaturityEstimateResponse {

    private static final BigDecimal TAX_RATE = new BigDecimal("15.4");
    private static final String CALCULATION_BASIS =
            "MONTHLY_BEGINNING_SIMPLE_INTEREST";
    @JsonProperty("financial_product_id")
    private final long financialProductId;

    @JsonProperty("monthly_amount")
    private final BigDecimal monthlyAmount;

    @JsonProperty("period_months")
    private final int periodMonths;

    @JsonProperty("applied_interest_rate")
    private final AppliedInterestRate appliedInterestRate;

    @JsonProperty("principal_amount")
    private final BigDecimal principalAmount;

    @JsonProperty("estimated_interest_before_tax")
    private final BigDecimal estimatedInterestBeforeTax;

    @JsonProperty("tax_rate")
    private final BigDecimal taxRate;

    @JsonProperty("estimated_tax")
    private final BigDecimal estimatedTax;

    @JsonProperty("estimated_interest_after_tax")
    private final BigDecimal estimatedInterestAfterTax;

    @JsonProperty("estimated_maturity_amount")
    private final BigDecimal estimatedMaturityAmount;

    @JsonProperty("calculation_basis")
    private final String calculationBasis;

    private final String disclaimer;

    public FinancialProductMaturityEstimateResponse(
            long financialProductId,
            BigDecimal monthlyAmount,
            int periodMonths,
            String rateType,
            String rateLabel,
            BigDecimal annualRate,
            BigDecimal principalAmount,
            BigDecimal estimatedInterestBeforeTax,
            BigDecimal estimatedTax,
            BigDecimal estimatedInterestAfterTax,
            BigDecimal estimatedMaturityAmount
    ) {
        this.financialProductId = financialProductId;
        this.monthlyAmount = monthlyAmount;
        this.periodMonths = periodMonths;
        this.appliedInterestRate = new AppliedInterestRate(
                rateType,
                rateLabel,
                annualRate
        );
        this.principalAmount = principalAmount;
        this.estimatedInterestBeforeTax = estimatedInterestBeforeTax;
        this.taxRate = TAX_RATE;
        this.estimatedTax = estimatedTax;
        this.estimatedInterestAfterTax = estimatedInterestAfterTax;
        this.estimatedMaturityAmount = estimatedMaturityAmount;
        this.calculationBasis = CALCULATION_BASIS;
        this.disclaimer = "월초에 동일 금액을 납입하고 "
                + rateLabel
                + "와 일반과세 15.4%를 적용한 예상값입니다. "
                + "실제 만기금액은 납입일, 우대조건, 세금 및 상품 약관에 "
                + "따라 달라질 수 있습니다.";
    }

    @Getter
    public static class AppliedInterestRate {

        private final String type;
        private final String label;

        @JsonProperty("annual_rate")
        private final BigDecimal annualRate;

        public AppliedInterestRate(
                String type,
                String label,
                BigDecimal annualRate
        ) {
            this.type = type;
            this.label = label;
            this.annualRate = annualRate;
        }
    }
}
