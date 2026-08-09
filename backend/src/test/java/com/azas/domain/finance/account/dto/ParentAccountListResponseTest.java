package com.azas.domain.finance.account.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ParentAccountListResponseTest {

    private final ObjectMapper objectMapper =
            new ObjectMapper()
                    .registerModule(new JavaTimeModule())
                    .disable(
                            SerializationFeature
                                    .WRITE_DATES_AS_TIMESTAMPS
                    );

    @Test
    void serializesFullAccountNumberWithSnakeCaseKeys()
            throws Exception {
        ParentAccountListResult result =
                new ParentAccountListResult(
                        List.of(accountResult())
                );

        String json = objectMapper.writeValueAsString(
                ParentAccountListResponse.from(result)
        );

        JsonNode root = objectMapper.readTree(json);
        JsonNode account = root.path("accounts").get(0);

        assertEquals(1, root.path("total_count").asInt());
        assertEquals(2L, account.path("account_id").asLong());
        assertEquals(
                "987-6543-5678",
                account.path("account_number").asText()
        );
        assertEquals(
                "2026-08-08T05:30:00Z",
                account.path("balance_updated_at").asText()
        );
        assertFalse(json.contains("masked_account_number"));
        assertFalse(json.contains("accountNumberCiphertext"));
    }

    private ParentAccountListItemResult accountResult() {
        return new ParentAccountListItemResult(
                2L,
                "004",
                "KB국민은행",
                "KB Young Youth 입출금통장",
                "987-6543-5678",
                "DEMAND_DEPOSIT",
                new BigDecimal("1250000.00"),
                LocalDateTime.of(2026, 8, 8, 5, 30),
                "ACTIVE",
                true
        );
    }
}
