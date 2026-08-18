package com.azas.domain.finance.account.dto;

import com.azas.domain.finance.account.entity.ChildUsageMode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChildAvailableAmountResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Test
    void serializesSnakeCaseUsageFields() throws Exception {
        ChildAvailableAmountResponse response =
                ChildAvailableAmountResponse.from(result(
                        ChildUsageMode.CO_MANAGED,
                        new BigDecimal("100000.00"),
                        new BigDecimal("65000.00"),
                        false
                ));

        JsonNode json = objectMapper.readTree(
                objectMapper.writeValueAsString(response)
        );

        assertEquals(6L, json.get("child_id").asLong());
        assertEquals(15L, json.get("account_id").asLong());
        assertEquals(
                "CO_MANAGED",
                json.get("child_usage_mode").asText()
        );
        assertEquals(
                100000.0,
                json.get("child_monthly_budget_amount").asDouble()
        );
        assertEquals(
                35000.0,
                json.get("current_month_spent_amount").asDouble()
        );
        assertEquals(
                65000.0,
                json.get("remaining_guidance_amount").asDouble()
        );
        assertFalse(json.get("budget_exceeded").asBoolean());
        assertEquals("2026-08", json.get("period").asText());
        assertEquals(
                "2026-08-11T10:00:00Z",
                json.get("calculated_at").asText()
        );
    }

    @Test
    void includesNullPolicyAmountsForUnrestrictedMode()
            throws Exception {
        ChildAvailableAmountResponse response =
                ChildAvailableAmountResponse.from(result(
                        ChildUsageMode.UNRESTRICTED,
                        null,
                        null,
                        null
                ));

        JsonNode json = objectMapper.readTree(
                objectMapper.writeValueAsString(response)
        );

        assertTrue(json.has("child_monthly_budget_amount"));
        assertTrue(json.get("child_monthly_budget_amount").isNull());
        assertTrue(json.has("remaining_guidance_amount"));
        assertTrue(json.get("remaining_guidance_amount").isNull());
        assertTrue(json.has("budget_exceeded"));
        assertTrue(json.get("budget_exceeded").isNull());
    }

    private ChildAvailableAmountResult result(
            ChildUsageMode usageMode,
            BigDecimal budgetAmount,
            BigDecimal remainingAmount,
            Boolean exceeded
    ) {
        return new ChildAvailableAmountResult(
                6L,
                15L,
                usageMode,
                budgetAmount,
                new BigDecimal("35000.00"),
                remainingAmount,
                exceeded,
                "2026-08",
                LocalDateTime.of(2026, 8, 11, 10, 0)
        );
    }
}
