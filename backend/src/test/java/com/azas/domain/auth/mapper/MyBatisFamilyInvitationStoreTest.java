package com.azas.domain.auth.mapper;

import com.azas.domain.auth.entity.FamilyInvitation;
import com.azas.domain.child.entity.RelationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MyBatisFamilyInvitationStoreTest {

    @Mock
    private FamilyInvitationMapper familyInvitationMapper;

    private MyBatisFamilyInvitationStore store;

    @BeforeEach
    void setUp() {
        store = new MyBatisFamilyInvitationStore(
                familyInvitationMapper
        );
    }

    @Test
    void returnsInvitationWhenTokenHashExists() {
        FamilyInvitation invitation =
                FamilyInvitation.builder()
                        .familyInvitationId(1L)
                        .inviteTokenHash("token-hash")
                        .build();

        when(
                familyInvitationMapper.findByInviteTokenHash(
                        "token-hash"
                )
        ).thenReturn(invitation);

        Optional<FamilyInvitation> result =
                store.findByInviteTokenHash("token-hash");

        assertTrue(result.isPresent());
        assertSame(invitation, result.get());
    }

    @Test
    void returnsEmptyWhenTokenHashDoesNotExist() {
        when(
                familyInvitationMapper.findByInviteTokenHash(
                        "unknown-token-hash"
                )
        ).thenReturn(null);

        Optional<FamilyInvitation> result =
                store.findByInviteTokenHash(
                        "unknown-token-hash"
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void returnsTrueWhenInvitationIsAccepted() {
        LocalDateTime acceptedAt =
                LocalDateTime.of(2026, 8, 4, 12, 0);

        when(
                familyInvitationMapper.acceptIfPending(
                        1L,
                        22L,
                        null,
                        acceptedAt
                )
        ).thenReturn(1);

        boolean accepted = store.acceptIfPending(
                1L,
                22L,
                null,
                acceptedAt
        );

        assertTrue(accepted);

        verify(familyInvitationMapper)
                .acceptIfPending(
                        1L,
                        22L,
                        null,
                        acceptedAt
                );
    }

    @Test
    void returnsFalseWhenInvitationCannotBeAccepted() {
        LocalDateTime acceptedAt =
                LocalDateTime.of(2026, 8, 4, 12, 0);

        when(
                familyInvitationMapper.acceptIfPending(
                        1L,
                        23L,
                        RelationType.FATHER,
                        acceptedAt
                )
        ).thenReturn(0);

        boolean accepted = store.acceptIfPending(
                1L,
                23L,
                RelationType.FATHER,
                acceptedAt
        );

        assertFalse(accepted);
    }
}