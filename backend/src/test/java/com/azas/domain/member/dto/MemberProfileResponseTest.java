package com.azas.domain.member.dto;

import com.azas.domain.auth.entity.OAuthProvider;
import com.azas.domain.auth.entity.SocialAccount;
import com.azas.domain.member.entity.Member;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MemberProfileResponseTest {

    private final ObjectMapper objectMapper =
            new ObjectMapper()
                    .registerModule(new JavaTimeModule())
                    .disable(
                            SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
                    );

    @Test
    void serializesProfileWithSnakeCaseAndSafeFields() {
        Member member = Member.createParent(
                "parent@example.com",
                "김하나",
                "https://example.com/profile.png"
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
                LocalDateTime.of(2026, 7, 24, 1, 0)
        );
        ReflectionTestUtils.setField(
                member,
                "createdAt",
                LocalDateTime.of(2026, 7, 23, 3, 0)
        );
        ReflectionTestUtils.setField(
                member,
                "updatedAt",
                LocalDateTime.of(2026, 7, 24, 1, 0)
        );

        SocialAccount socialAccount =
                SocialAccount.create(
                        1L,
                        OAuthProvider.GOOGLE,
                        "google-subject"
                );

        ReflectionTestUtils.setField(
                socialAccount,
                "createdAt",
                LocalDateTime.of(2026, 7, 23, 3, 0)
        );

        MemberProfileResponse response =
                MemberProfileResponse.from(
                        new MemberProfileResult(
                                member,
                                List.of(socialAccount),
                                "010-****-5678"
                        )
                );

        JsonNode json =
                objectMapper.valueToTree(response);

        assertEquals(
                1L,
                json.get("member_id").asLong()
        );
        assertEquals(
                "PARENT",
                json.get("member_type").asText()
        );
        assertEquals(
                "ACTIVE",
                json.get("status").asText()
        );
        assertEquals(
                "1992-04-15",
                json.get("birth_date").asText()
        );
        assertEquals(
                "010-****-5678",
                json.get("phone_number").asText()
        );
        assertTrue(
                json.get("phone_verified").asBoolean()
        );
        assertEquals(
                "2026-07-24T01:00:00Z",
                json.get("phone_verified_at").asText()
        );
        assertEquals(
                "GOOGLE",
                json.get("social_accounts")
                        .get(0)
                        .get("provider")
                        .asText()
        );
        assertEquals(
                "2026-07-23T03:00:00Z",
                json.get("social_accounts")
                        .get(0)
                        .get("connected_at")
                        .asText()
        );
        assertEquals(
                "2026-07-23T03:00:00Z",
                json.get("created_at").asText()
        );
        assertEquals(
                "2026-07-24T01:00:00Z",
                json.get("updated_at").asText()
        );

        assertFalse(json.has("memberId"));
        assertFalse(json.has("phoneNumberCiphertext"));
        assertFalse(json.has("phoneNumberHash"));
        assertFalse(
                json.get("social_accounts")
                        .get(0)
                        .has("provider_subject")
        );
    }
}