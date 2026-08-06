package com.azas.domain.auth.mapper;

import com.azas.domain.auth.entity.FamilyInvitation;
import com.azas.domain.auth.service.FamilyInvitationStore;
import com.azas.domain.child.entity.RelationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MyBatisFamilyInvitationStore
        implements FamilyInvitationStore {

    private final FamilyInvitationMapper familyInvitationMapper;

    @Override
    public Optional<FamilyInvitation> findByInviteTokenHash(
            String inviteTokenHash
    ) {
        return Optional.ofNullable(
                familyInvitationMapper.findByInviteTokenHash(
                        inviteTokenHash
                )
        );
    }

    @Override
    public boolean acceptIfPending(
            Long familyInvitationId,
            Long acceptedMemberId,
            RelationType relationType,
            LocalDateTime acceptedAt
    ) {
        return familyInvitationMapper.acceptIfPending(
                familyInvitationId,
                acceptedMemberId,
                relationType,
                acceptedAt
        ) == 1;
    }
}