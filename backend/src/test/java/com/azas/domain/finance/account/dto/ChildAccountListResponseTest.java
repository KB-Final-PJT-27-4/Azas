package com.azas.domain.finance.account.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChildAccountListResponseTest {

    @Test
    void serializesSnakeCaseFieldsAndIso8601Time()
            throws Exception {
        ChildAccountListResponse response =
                ChildAccountListResponse.from(
                        new ChildAccountListResult(
                                1L,
                                List.of(accountResult())
                        )
                );

        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        JsonNode json = objectMapper.readTree(
                objectMapper.writeValueAsString(response)
        );

        assertEquals(1L, json.get("child_id").asLong());
        assertEquals(1, json.get("total_count").asInt());
        assertEquals(
                "123-4567-8901",
                json.get("accounts").get(0)
                        .get("account_number").asText()
        );
        assertEquals(
                "2026-08-09T05:30:00Z",
                json.get("accounts").get(0)
                        .get("balance_updated_at").asText()
        );
    }

    private ChildAccountListItemResult accountResult() {
        return new ChildAccountListItemResult(
                2L,
                "004",
                "KB국민은행",
                "KB Young Youth 입출금통장",
                "123-4567-8901",
                "DEMAND_DEPOSIT",
                new BigDecimal("1250000.00"),
                LocalDateTime.of(2026, 8, 9, 5, 30),
                "ACTIVE",
                true
        );
    }
}
