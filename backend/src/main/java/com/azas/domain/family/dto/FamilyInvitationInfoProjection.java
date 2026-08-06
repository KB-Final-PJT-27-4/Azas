package com.azas.domain.family.dto;

import com.azas.domain.auth.entity.FamilyInvitationStatus;
import com.azas.domain.auth.entity.FamilyInviteeType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FamilyInvitationInfoProjection {

    private String childName;
    private String inviterName;
    private FamilyInviteeType inviteeType;
    private FamilyInvitationStatus status;
    private LocalDateTime expiresAt;
}