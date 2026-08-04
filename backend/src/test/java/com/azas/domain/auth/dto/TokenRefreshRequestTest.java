package com.azas.domain.auth.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class TokenRefreshRequestTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void deserializesRefreshTokenWithSnakeCaseKey() throws Exception {
        TokenRefreshRequest request = objectMapper.readValue(
                "{\"refresh_token\":\"refresh-token\"}",
                TokenRefreshRequest.class
        );

        assertEquals(
                "refresh-token",
                request.getRefreshToken()
        );

        JsonNode json = objectMapper.valueToTree(request);

        assertEquals(
                "refresh-token",
                json.get("refresh_token").asText()
        );
        assertFalse(json.has("refreshToken"));
    }
}
