package com.azas.domain.auth.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FamilyInvitationTest {

    private static final LocalDateTime NOW =
            LocalDateTime.of(2026, 8, 4, 12, 0);

    @Test
    void returnsTrueForPendingUnexpiredChildInvitation() {
        FamilyInvitation invitation = invitation(
                FamilyInviteeType.CHILD,
                FamilyInvitationStatus.PENDING,
                NOW.plusHours(1)
        );

        assertTrue(
                invitation.isUsableFor(
                        FamilyInviteeType.CHILD,
                        NOW
                )
        );
    }

    @Test
    void returnsFalseWhenInviteeTypeDoesNotMatch() {
        FamilyInvitation invitation = invitation(
                FamilyInviteeType.PARENT,
                FamilyInvitationStatus.PENDING,
                NOW.plusHours(1)
        );

        assertFalse(
                invitation.isUsableFor(
                        FamilyInviteeType.CHILD,
                        NOW
                )
        );
    }

    @Test
    void returnsFalseWhenInvitationIsNotPending() {
        FamilyInvitation invitation = invitation(
                FamilyInviteeType.CHILD,
                FamilyInvitationStatus.ACCEPTED,
                NOW.plusHours(1)
        );

        assertFalse(
                invitation.isUsableFor(
                        FamilyInviteeType.CHILD,
                        NOW
                )
        );
    }

    @Test
    void returnsFalseWhenInvitationIsExpired() {
        FamilyInvitation invitation = invitation(
                FamilyInviteeType.CHILD,
                FamilyInvitationStatus.PENDING,
                NOW
        );

        assertFalse(
                invitation.isUsableFor(
                        FamilyInviteeType.CHILD,
                        NOW
                )
        );
    }

    private FamilyInvitation invitation(
            FamilyInviteeType inviteeType,
            FamilyInvitationStatus status,
            LocalDateTime expiresAt
    ) {
        return FamilyInvitation.builder()
                .familyInvitationId(1L)
                .childId(10L)
                .inviterMemberId(1L)
                .inviteeType(inviteeType)
                .inviteTokenHash("hashed-invite-token")
                .status(status)
                .expiresAt(expiresAt)
                .createdAt(NOW.minusHours(1))
                .updatedAt(NOW.minusHours(1))
                .build();
    }
}