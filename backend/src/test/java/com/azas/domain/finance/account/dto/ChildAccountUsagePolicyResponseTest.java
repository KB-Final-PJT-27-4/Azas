package com.azas.domain.finance.account.dto;

import com.azas.domain.finance.account.entity.ChildUsageMode;
import com.azas.domain.finance.account.entity.FinancialAccountUsagePolicy;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ChildAccountUsagePolicyResponseTest {

    private final ObjectMapper objectMapper =
            new ObjectMapper()
                    .registerModule(new JavaTimeModule())
                    .disable(
                            SerializationFeature
                                    .WRITE_DATES_AS_TIMESTAMPS
                    );

    @Test
    void serializesUsagePolicyWithSnakeCaseKeys()
            throws Exception {
        FinancialAccountUsagePolicy policy =
                new FinancialAccountUsagePolicy();

        ReflectionTestUtils.setField(
                policy,
                "financialAccountId",
                15L
        );
        ReflectionTestUtils.setField(
                policy,
                "childId",
                6L
        );
        ReflectionTestUtils.setField(
                policy,
                "childUsageMode",
                ChildUsageMode.CO_MANAGED
        );
        ReflectionTestUtils.setField(
                policy,
                "childMonthlyBudgetAmount",
                new BigDecimal("50000.00")
        );
        ReflectionTestUtils.setField(
                policy,
                "usagePolicyUpdatedAt",
                LocalDateTime.of(
                        2026,
                        8,
                        8,
                        10,
                        0
                )
        );

        String json = objectMapper.writeValueAsString(
                ChildAccountUsagePolicyResponse.from(
                        policy
                )
        );

        assertEquals(
                """
                {"account_id":15,"child_id":6,"child_usage_mode":"CO_MANAGED","child_monthly_budget_amount":50000.00,"usage_policy_updated_at":"2026-08-08T10:00:00Z"}""",
                json
        );

        assertFalse(
                json.contains("financialAccountId")
        );
        assertFalse(
                json.contains("childUsageMode")
        );
    }
}