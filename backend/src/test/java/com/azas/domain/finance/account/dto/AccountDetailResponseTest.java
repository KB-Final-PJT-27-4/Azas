package com.azas.domain.finance.account.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AccountDetailResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void serializesOnlyAccountDetailScreenFields() throws Exception {
        AccountDetailResponse response = AccountDetailResponse.from(
                new AccountDetailResult(
                        5L,
                        "CHILD",
                        "KB국민은행",
                        "아이사랑적금1",
                        "952-17362605-43",
                        "깨비",
                        "SAVINGS",
                        new BigDecimal("100000.00")
                )
        );

        JsonNode json = objectMapper.readTree(
                objectMapper.writeValueAsString(response)
        );

        assertEquals(5L, json.get("account_id").asLong());
        assertEquals("CHILD", json.get("owner_type").asText());
        assertEquals("KB국민은행", json.get("bank_name").asText());
        assertEquals("아이사랑적금1", json.get("account_name").asText());
        assertEquals("952-17362605-43",
                json.get("account_number").asText());
        assertEquals("깨비", json.get("account_holder_name").asText());
        assertEquals("SAVINGS",
                json.get("account_product_type").asText());
        assertEquals(0, new BigDecimal("100000.00").compareTo(
                json.get("balance").decimalValue()
        ));

        Set<String> fields = new HashSet<>();
        json.fieldNames().forEachRemaining(fields::add);

        assertEquals(Set.of(
                "account_id",
                "owner_type",
                "bank_name",
                "account_name",
                "account_number",
                "account_holder_name",
                "account_product_type",
                "balance"
        ), fields);
        assertFalse(json.has("financial_goal"));
        assertFalse(json.has("time_capsule"));
    }
}
