package com.azas.domain.auth.entity;

import com.azas.domain.child.entity.RelationType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FamilyInvitation {

    private Long familyInvitationId;
    private Long childId;
    private Long inviterMemberId;
    private FamilyInviteeType inviteeType;
    private RelationType relationType;
    private String inviteTokenHash;
    private FamilyInvitationStatus status;
    private LocalDateTime expiresAt;
    private Long acceptedMemberId;
    private LocalDateTime acceptedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public boolean isUsableFor(
            FamilyInviteeType expectedInviteeType,
            LocalDateTime now
    ) {
        return inviteeType == expectedInviteeType
                && status == FamilyInvitationStatus.PENDING
                && expiresAt != null
                && expiresAt.isAfter(now);
    }
}