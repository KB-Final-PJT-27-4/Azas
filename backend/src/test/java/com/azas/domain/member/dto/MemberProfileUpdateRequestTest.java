package com.azas.domain.member.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MemberProfileUpdateRequestTest {

    private final ObjectMapper objectMapper =
            new ObjectMapper()
                    .registerModule(new JavaTimeModule());

    @Test
    void convertsProvidedFieldsToCommand()
            throws Exception {
        MemberProfileUpdateRequest request =
                objectMapper.readValue(
                        """
                        {
                          "birth_date": "1992-04-15",
                          "profile_image_url":
                            "https://example.com/profile.png",
                          "phone_verification_token":
                            "verification-token"
                        }
                        """,
                        MemberProfileUpdateRequest.class
                );

        MemberProfileUpdateCommand command =
                request.toCommand();

        assertTrue(command.isBirthDateProvided());
        assertEquals(
                LocalDate.of(1992, 4, 15),
                command.getBirthDate()
        );

        assertTrue(
                command.isProfileImageUrlProvided()
        );
        assertEquals(
                "https://example.com/profile.png",
                command.getProfileImageUrl()
        );

        assertTrue(
                command.isPhoneVerificationTokenProvided()
        );
        assertEquals(
                "verification-token",
                command.getPhoneVerificationToken()
        );
    }

    @Test
    void distinguishesMissingFieldsFromExplicitNull()
            throws Exception {
        MemberProfileUpdateRequest emptyRequest =
                objectMapper.readValue(
                        "{}",
                        MemberProfileUpdateRequest.class
                );

        MemberProfileUpdateCommand emptyCommand =
                emptyRequest.toCommand();

        assertFalse(emptyCommand.isBirthDateProvided());
        assertFalse(
                emptyCommand.isProfileImageUrlProvided()
        );
        assertFalse(
                emptyCommand
                        .isPhoneVerificationTokenProvided()
        );

        MemberProfileUpdateRequest nullRequest =
                objectMapper.readValue(
                        """
                        {
                          "birth_date": null,
                          "profile_image_url": null
                        }
                        """,
                        MemberProfileUpdateRequest.class
                );

        MemberProfileUpdateCommand nullCommand =
                nullRequest.toCommand();

        assertTrue(nullCommand.isBirthDateProvided());
        assertNull(nullCommand.getBirthDate());

        assertTrue(
                nullCommand.isProfileImageUrlProvided()
        );
        assertNull(nullCommand.getProfileImageUrl());

        assertFalse(
                nullCommand
                        .isPhoneVerificationTokenProvided()
        );
    }
}