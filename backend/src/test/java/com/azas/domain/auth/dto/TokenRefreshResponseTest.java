package com.azas.domain.auth.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class TokenRefreshResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void serializesTokenPairWithSnakeCaseKeys() {
        AuthTokenPair tokenPair = new AuthTokenPair(
                "access-token",
                "refresh-token",
                3600L
        );

        TokenRefreshResponse response =
                TokenRefreshResponse.from(tokenPair);

        JsonNode json = objectMapper.valueToTree(response);

        assertEquals(
                "access-token",
                json.get("access_token").asText()
        );
        assertEquals(
                "refresh-token",
                json.get("refresh_token").asText()
        );
        assertEquals(
                "Bearer",
                json.get("token_type").asText()
        );
        assertEquals(
                3600L,
                json.get("expires_in").asLong()
        );

        assertFalse(json.has("accessToken"));
        assertFalse(json.has("refreshToken"));
        assertFalse(json.has("tokenType"));
        assertFalse(json.has("expiresIn"));
    }
}
