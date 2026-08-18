package com.azas.domain.finance.account.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ParentAccountListResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void serializesOnlyAccountListScreenFields() throws Exception {
        ParentAccountListResult result = new ParentAccountListResult(
                new BigDecimal("9600000.00"),
                List.of(accountResult())
        );

        String json = objectMapper.writeValueAsString(
                ParentAccountListResponse.from(result)
        );
        JsonNode root = objectMapper.readTree(json);
        JsonNode account = root.path("accounts").get(0);

        assertEquals(
                0,
                new BigDecimal("9600000.00").compareTo(
                        root.path("total_balance").decimalValue()
                )
        );
        assertEquals(1, root.path("total_count").asInt());
        assertEquals(2L, account.path("account_id").asLong());
        assertEquals("아이사랑적금1", account.path("account_name").asText());
        assertEquals("952-17362605-43",
                account.path("account_number").asText());
        assertEquals("SAVINGS",
                account.path("account_product_type").asText());
        assertEquals(
                0,
                new BigDecimal("9600000.00").compareTo(
                        account.path("balance").decimalValue()
                )
        );

        assertFalse(account.has("organization_code"));
        assertFalse(account.has("bank_name"));
        assertFalse(account.has("balance_updated_at"));
        assertFalse(account.has("account_status"));
        assertFalse(account.has("is_primary"));
        assertFalse(json.contains("accountNumberCiphertext"));
    }

    private ParentAccountListItemResult accountResult() {
        return new ParentAccountListItemResult(
                2L,
                "아이사랑적금1",
                "952-17362605-43",
                "SAVINGS",
                new BigDecimal("9600000.00")
        );
    }
}
