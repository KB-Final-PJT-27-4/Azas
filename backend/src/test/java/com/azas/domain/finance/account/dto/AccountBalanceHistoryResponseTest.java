package com.azas.domain.finance.account.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountBalanceHistoryResponseTest {

    @Test
    void serializesFixedMonthlyBucketsInSnakeCaseAndUtc()
            throws Exception {
        AccountBalanceHistoryResponse response =
                AccountBalanceHistoryResponse.from(historyResult());

        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        JsonNode json = objectMapper.readTree(
                objectMapper.writeValueAsString(response)
        );

        assertEquals(3L, json.get("account_id").asLong());
        assertEquals(3, json.get("months").asInt());
        assertEquals("2026-06", json.get("start_month").asText());
        assertEquals("2026-08", json.get("end_month").asText());
        assertEquals(3, json.get("balance_history").size());

        JsonNode june = json.get("balance_history").get(0);
        assertEquals("2026-06", june.get("month").asText());
        assertMoney("1000000.00", june.get("balance"));
        assertMoney("100000.00", june.get("change_amount"));
        assertEquals(
                "2026-06-30T14:00:00Z",
                june.get("observed_at").asText()
        );

        JsonNode july = json.get("balance_history").get(1);
        assertEquals("2026-07", july.get("month").asText());
        assertTrue(july.get("balance").isNull());
        assertTrue(july.get("change_amount").isNull());
        assertTrue(july.get("observed_at").isNull());

        JsonNode august = json.get("balance_history").get(2);
        assertMoney("1300000.00", august.get("balance"));
        assertTrue(august.get("change_amount").isNull());
        assertFalse(json.has("account_number"));
    }

    private AccountBalanceHistoryResult historyResult() {
        return new AccountBalanceHistoryResult(
                3L,
                3,
                YearMonth.of(2026, 6),
                YearMonth.of(2026, 8),
                List.of(
                        new MonthlyAccountBalanceResult(
                                YearMonth.of(2026, 6),
                                new BigDecimal("1000000.00"),
                                new BigDecimal("100000.00"),
                                LocalDateTime.of(2026, 6, 30, 14, 0)
                        ),
                        new MonthlyAccountBalanceResult(
                                YearMonth.of(2026, 7),
                                null,
                                null,
                                null
                        ),
                        new MonthlyAccountBalanceResult(
                                YearMonth.of(2026, 8),
                                new BigDecimal("1300000.00"),
                                null,
                                LocalDateTime.of(2026, 8, 10, 5, 30)
                        )
                )
        );
    }

    private void assertMoney(String expected, JsonNode actual) {
        assertEquals(
                0,
                new BigDecimal(expected).compareTo(actual.decimalValue())
        );
    }
}
