package com.azas.domain.finance.product.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MaturityEstimateCalculatorTest {

    private final MaturityEstimateCalculator calculator =
            new MaturityEstimateCalculator();

    @Test
    void calculatesMonthlyBeginningSimpleInterestAndTax() {
        MaturityEstimateCalculator.Calculation result = calculator.calculate(
                new BigDecimal("300000"),
                12,
                new BigDecimal("3.4")
        );

        assertEquals(new BigDecimal("3600000"), result.getPrincipalAmount());
        assertEquals(
                new BigDecimal("66300"),
                result.getEstimatedInterestBeforeTax()
        );
        assertEquals(new BigDecimal("10210"), result.getEstimatedTax());
        assertEquals(
                new BigDecimal("56090"),
                result.getEstimatedInterestAfterTax()
        );
        assertEquals(
                new BigDecimal("3656090"),
                result.getEstimatedMaturityAmount()
        );
    }

    @Test
    void truncatesFractionalWonForInterestAndTax() {
        MaturityEstimateCalculator.Calculation result = calculator.calculate(
                new BigDecimal("10001"),
                6,
                new BigDecimal("2.13")
        );

        assertEquals(new BigDecimal("372"), result.getEstimatedInterestBeforeTax());
        assertEquals(new BigDecimal("57"), result.getEstimatedTax());
        assertEquals(new BigDecimal("60321"), result.getEstimatedMaturityAmount());
    }
}
