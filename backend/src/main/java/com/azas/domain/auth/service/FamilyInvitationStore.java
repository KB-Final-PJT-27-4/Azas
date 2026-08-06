package com.azas.domain.auth.service;

import com.azas.domain.auth.entity.FamilyInvitation;
import com.azas.domain.child.entity.RelationType;

import java.time.LocalDateTime;
import java.util.Optional;

public interface FamilyInvitationStore {

    Optional<FamilyInvitation> findByInviteTokenHash(
            String inviteTokenHash
    );

    boolean acceptIfPending(
            Long familyInvitationId,
            Long acceptedMemberId,
            RelationType relationType,
            LocalDateTime acceptedAt
    );
}