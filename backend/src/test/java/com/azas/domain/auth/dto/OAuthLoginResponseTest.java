package com.azas.domain.auth.dto;

import com.azas.domain.member.entity.Member;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OAuthLoginResponseTest {

    private final ObjectMapper objectMapper =
            new ObjectMapper()
                    .registerModule(new JavaTimeModule())
                    .disable(
                            SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
                    );

    @Test
    void serializesOAuthLoginResponseWithSnakeCaseKeys() {
        Member member = Member.createParent(
                "parent@example.com",
                "김하나",
                null
        );

        ReflectionTestUtils.setField(
                member,
                "memberId",
                1L
        );
        ReflectionTestUtils.setField(
                member,
                "birthDate",
                LocalDate.of(1992, 4, 15)
        );
        ReflectionTestUtils.setField(
                member,
                "phoneVerifiedAt",
                LocalDateTime.of(2026, 8, 2, 6, 0)
        );
        ReflectionTestUtils.setField(
                member,
                "createdAt",
                LocalDateTime.of(2026, 7, 23, 3, 0)
        );

        AuthTokenPair tokenPair =
                new AuthTokenPair(
                        "access-token",
                        "refresh-token",
                        3600L
                );

        OAuthLoginResult result =
                new OAuthLoginResult(
                        tokenPair,
                        member,
                        true
                );

        OAuthLoginResponse response =
                OAuthLoginResponse.from(result);

        JsonNode json =
                objectMapper.valueToTree(response);

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
        assertTrue(
                json.get("is_new_member").asBoolean()
        );

        assertFalse(json.has("accessToken"));
        assertFalse(json.has("newMember"));

        JsonNode memberJson = json.get("member");

        assertEquals(
                1L,
                memberJson.get("member_id").asLong()
        );
        assertEquals(
                "PARENT",
                memberJson.get("member_type").asText()
        );
        assertEquals(
                "1992-04-15",
                memberJson.get("birth_date").asText()
        );
        assertTrue(
                memberJson.get("phone_verified").asBoolean()
        );
        assertEquals(
                "2026-08-02T06:00:00Z",
                memberJson.get("phone_verified_at").asText()
        );
        assertEquals(
                "2026-07-23T03:00:00Z",
                memberJson.get("created_at").asText()
        );

        assertTrue(
                memberJson.get("phone_number").isNull()
        );
        assertFalse(memberJson.has("memberType"));
        assertFalse(memberJson.has("role"));
    }
}