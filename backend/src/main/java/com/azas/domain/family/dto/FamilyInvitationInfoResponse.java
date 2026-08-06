package com.azas.domain.family.dto;

import com.azas.domain.auth.entity.FamilyInvitationStatus;
import com.azas.domain.auth.entity.FamilyInviteeType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class FamilyInvitationInfoResponse {

    @JsonProperty("child_name")
    private final String childName;

    @JsonProperty("inviter_name")
    private final String inviterName;

    @JsonProperty("invitee_type")
    private final FamilyInviteeType inviteeType;

    private final FamilyInvitationStatus status;

    @JsonProperty("expires_at")
    private final Instant expiresAt;
}