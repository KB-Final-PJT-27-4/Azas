package com.azas.domain.finance.product.service;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class MaturityEstimateCalculator {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final BigDecimal MONTHS_IN_YEAR = new BigDecimal("12");
    private static final BigDecimal TAX_RATE = new BigDecimal("15.4");

    public Calculation calculate(
            BigDecimal monthlyAmount,
            int periodMonths,
            BigDecimal annualRate
    ) {
        BigDecimal principalAmount = monthlyAmount
                .multiply(BigDecimal.valueOf(periodMonths))
                .setScale(0, RoundingMode.DOWN);
        long interestBearingMonthSum = (long) periodMonths
                * (periodMonths + 1L) / 2L;
        BigDecimal estimatedInterestBeforeTax = monthlyAmount
                .multiply(annualRate)
                .multiply(BigDecimal.valueOf(interestBearingMonthSum))
                .divide(ONE_HUNDRED, 12, RoundingMode.DOWN)
                .divide(MONTHS_IN_YEAR, 0, RoundingMode.DOWN);
        BigDecimal estimatedTax = estimatedInterestBeforeTax
                .multiply(TAX_RATE)
                .divide(ONE_HUNDRED, 0, RoundingMode.DOWN);
        BigDecimal estimatedInterestAfterTax = estimatedInterestBeforeTax
                .subtract(estimatedTax);
        BigDecimal estimatedMaturityAmount = principalAmount
                .add(estimatedInterestAfterTax);

        return new Calculation(
                principalAmount,
                estimatedInterestBeforeTax,
                estimatedTax,
                estimatedInterestAfterTax,
                estimatedMaturityAmount
        );
    }

    @Getter
    public static class Calculation {

        private final BigDecimal principalAmount;
        private final BigDecimal estimatedInterestBeforeTax;
        private final BigDecimal estimatedTax;
        private final BigDecimal estimatedInterestAfterTax;
        private final BigDecimal estimatedMaturityAmount;

        public Calculation(
                BigDecimal principalAmount,
                BigDecimal estimatedInterestBeforeTax,
                BigDecimal estimatedTax,
                BigDecimal estimatedInterestAfterTax,
                BigDecimal estimatedMaturityAmount
        ) {
            this.principalAmount = principalAmount;
            this.estimatedInterestBeforeTax = estimatedInterestBeforeTax;
            this.estimatedTax = estimatedTax;
            this.estimatedInterestAfterTax = estimatedInterestAfterTax;
            this.estimatedMaturityAmount = estimatedMaturityAmount;
        }
    }
}
