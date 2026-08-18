package com.azas.domain.finance.product.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class FinancialProductMaturityEstimateResponseTest {

    @Test
    void serializesConfirmedMaturityEstimateFields() throws Exception {
        FinancialProductMaturityEstimateResponse response =
                new FinancialProductMaturityEstimateResponse(
                        1L,
                        new BigDecimal("300000"),
                        12,
                        "MAX",
                        "최고금리",
                        new BigDecimal("3.4"),
                        new BigDecimal("3600000"),
                        new BigDecimal("66300"),
                        new BigDecimal("10210"),
                        new BigDecimal("56090"),
                        new BigDecimal("3656090")
                );

        JsonNode json = new ObjectMapper().valueToTree(response);

        assertEquals(1L, json.get("financial_product_id").asLong());
        assertEquals(300000, json.get("monthly_amount").asInt());
        assertEquals("MAX", json.at("/applied_interest_rate/type").asText());
        assertEquals(
                3.4,
                json.at("/applied_interest_rate/annual_rate").asDouble()
        );
        assertEquals(3656090, json.get("estimated_maturity_amount").asInt());
        assertEquals(
                "MONTHLY_BEGINNING_SIMPLE_INTEREST",
                json.get("calculation_basis").asText()
        );
        assertFalse(json.has("interest_rate_type"));
    }
}
