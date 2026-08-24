package com.azas.domain.finance.account.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChildAccountListResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void serializesOnlyChildAccountListScreenFields() throws Exception {
        ChildAccountListResponse response = ChildAccountListResponse.from(
                new ChildAccountListResult(
                        6L,
                        new BigDecimal("14600000.00"),
                        List.of(accountResult())
                )
        );

        String body = objectMapper.writeValueAsString(response);
        JsonNode root = objectMapper.readTree(body);
        JsonNode account = root.path("accounts").get(0);

        assertEquals(6L, root.path("child_id").asLong());
        assertEquals(0, new BigDecimal("14600000.00").compareTo(
                root.path("total_balance").decimalValue()
        ));
        assertEquals(1, root.path("total_count").asInt());
        assertEquals(5L, account.path("account_id").asLong());
        assertEquals("아이사랑적금1", account.path("account_name").asText());
        assertEquals("952-17362605-43",
                account.path("account_number").asText());
        assertEquals("SAVINGS",
                account.path("account_product_type").asText());

        assertFalse(account.has("organization_code"));
        assertFalse(account.has("bank_name"));
        assertFalse(account.has("balance_updated_at"));
        assertFalse(account.has("account_status"));
        assertTrue(account.path("is_primary").asBoolean());
        assertFalse(account.has("financial_goal"));
        assertFalse(account.has("time_capsule"));
        assertFalse(body.contains("accountNumberCiphertext"));
    }

    private ChildAccountListItemResult accountResult() {
        return new ChildAccountListItemResult(
                5L,
                "아이사랑적금1",
                "952-17362605-43",
                "SAVINGS",
                new BigDecimal("14600000.00"),
                true
        );
    }
}
