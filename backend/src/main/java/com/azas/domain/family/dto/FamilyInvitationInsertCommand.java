package com.azas.domain.family.dto;

import com.azas.domain.auth.entity.FamilyInviteeType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class FamilyInvitationInsertCommand {

    private Long familyInvitationId;
    private Long childId;
    private Long inviterMemberId;
    private FamilyInviteeType inviteeType;
    private String inviteTokenHash;
    private LocalDateTime expiresAt;
}