package com.azas.domain.finance.account.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountDetailResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Test
    void serializesChildAccountDetailWithSnakeCaseAndUtcTime()
            throws Exception {
        AccountDetailResponse response = AccountDetailResponse.from(
                childAccountResult()
        );

        JsonNode json = objectMapper.readTree(
                objectMapper.writeValueAsString(response)
        );

        assertEquals(2L, json.get("account_id").asLong());
        assertEquals("CHILD", json.get("owner_type").asText());
        assertEquals(1L, json.get("child").get("child_id").asLong());
        assertEquals("김하늘", json.get("child").get("name").asText());
        assertEquals(
                "123-4567-8901",
                json.get("account_number").asText()
        );
        assertEquals(
                "2026-08-09T05:30:00Z",
                json.get("balance_updated_at").asText()
        );
        assertEquals(
                "2026-08-04T07:29:20Z",
                json.get("linked_at").asText()
        );
        assertEquals(
                "노트북 구매",
                json.get("financial_goal").get("goal_name").asText()
        );
        assertEquals(
                "2027-02-28",
                json.get("financial_goal").get("target_date").asText()
        );
    }

    @Test
    void serializesParentAccountWithoutChildAndGoal()
            throws Exception {
        AccountDetailResult result = new AccountDetailResult(
                1L,
                "PARENT",
                null,
                "004",
                "KB국민은행",
                "KB국민 5678",
                "987-6543-5678",
                "DEMAND_DEPOSIT",
                new BigDecimal("2000000.00"),
                null,
                "ACTIVE",
                true,
                null,
                null,
                LocalDateTime.of(2026, 8, 4, 7, 29, 20),
                null
        );

        JsonNode json = objectMapper.readTree(
                objectMapper.writeValueAsString(
                        AccountDetailResponse.from(result)
                )
        );

        assertTrue(json.get("child").isNull());
        assertTrue(json.get("financial_goal").isNull());
        assertTrue(json.get("balance_updated_at").isNull());
        assertTrue(json.get("opened_at").isNull());
        assertTrue(json.get("maturity_date").isNull());
    }

    private AccountDetailResult childAccountResult() {
        return new AccountDetailResult(
                2L,
                "CHILD",
                new AccountDetailChildResult(1L, "김하늘"),
                "004",
                "KB국민은행",
                "KB Young Youth 입출금통장",
                "123-4567-8901",
                "DEMAND_DEPOSIT",
                new BigDecimal("1250000.00"),
                LocalDateTime.of(2026, 8, 9, 5, 30),
                "ACTIVE",
                true,
                LocalDateTime.of(2024, 3, 1, 0, 0),
                LocalDate.of(2027, 3, 1),
                LocalDateTime.of(2026, 8, 4, 7, 29, 20),
                new AccountFinancialGoalResult(
                        "노트북 구매",
                        new BigDecimal("1500000.00"),
                        LocalDate.of(2027, 2, 28)
                )
        );
    }
}
