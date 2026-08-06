package com.azas.domain.auth.dto;

import com.azas.domain.member.entity.Member;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChildInviteOAuthResponseTest {

    private final ObjectMapper objectMapper =
            new ObjectMapper()
                    .registerModule(new JavaTimeModule())
                    .disable(
                            SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
                    );

    @Test
    void serializesChildInviteResponseWithSnakeCaseKeys() {
        Member member = Member.createChild(
                "child@example.com",
                "김자녀",
                null
        );

        ReflectionTestUtils.setField(
                member,
                "memberId",
                20L
        );
        ReflectionTestUtils.setField(
                member,
                "createdAt",
                LocalDateTime.of(2026, 8, 5, 2, 0)
        );

        ChildInviteOAuthResult result =
                new ChildInviteOAuthResult(
                        new AuthTokenPair(
                                "access-token",
                                "refresh-token",
                                3600L
                        ),
                        member,
                        true,
                        10L,
                        "김자녀",
                        30L,
                        LocalDateTime.of(
                                2026,
                                8,
                                5,
                                3,
                                0
                        )
                );

        JsonNode json =
                objectMapper.valueToTree(
                        ChildInviteOAuthResponse.from(result)
                );

        assertEquals(
                "access-token",
                json.get("access_token").asText()
        );
        assertEquals(
                "refresh-token",
                json.get("refresh_token").asText()
        );
        assertTrue(
                json.get("is_new_member").asBoolean()
        );

        assertEquals(
                "CHILD",
                json.get("member")
                        .get("member_type")
                        .asText()
        );

        assertEquals(
                10L,
                json.get("child")
                        .get("child_id")
                        .asLong()
        );
        assertTrue(
                json.get("child")
                        .get("member_linked")
                        .asBoolean()
        );

        assertEquals(
                30L,
                json.get("invitation")
                        .get("family_invitation_id")
                        .asLong()
        );
        assertEquals(
                "CHILD",
                json.get("invitation")
                        .get("invitee_type")
                        .asText()
        );
        assertEquals(
                "ACCEPTED",
                json.get("invitation")
                        .get("status")
                        .asText()
        );
        assertEquals(
                "2026-08-05T03:00:00Z",
                json.get("invitation")
                        .get("accepted_at")
                        .asText()
        );

        assertFalse(json.has("accessToken"));
        assertFalse(json.get("child").has("childId"));
        assertFalse(
                json.get("invitation")
                        .has("familyInvitationId")
        );
    }
}