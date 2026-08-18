package com.azas.domain.finance.account.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AccountBalanceResponseTest {

    @Test
    void serializesSnakeCaseFieldsAndUtcTime()
            throws Exception {
        AccountBalanceResponse response = AccountBalanceResponse.from(
                new AccountBalanceResult(
                        2L,
                        new BigDecimal("1250000.00"),
                        LocalDateTime.of(2026, 8, 10, 5, 30)
                )
        );

        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        JsonNode json = objectMapper.readTree(
                objectMapper.writeValueAsString(response)
        );

        assertEquals(2L, json.get("account_id").asLong());
        assertEquals(
                0,
                new BigDecimal("1250000.00").compareTo(
                        json.get("balance").decimalValue()
                )
        );
        assertEquals(
                "2026-08-10T05:30:00Z",
                json.get("balance_updated_at").asText()
        );
    }
}
