package com.azas.domain.family.dto;

import com.azas.domain.auth.entity.FamilyInvitationStatus;
import com.azas.domain.auth.entity.FamilyInviteeType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class FamilyInvitationCreateResponse {

    @JsonProperty("family_invitation_id")
    private final Long familyInvitationId;

    @JsonProperty("child_id")
    private final Long childId;

    @JsonProperty("invitee_type")
    private final FamilyInviteeType inviteeType;

    @JsonProperty("invite_token")
    private final String inviteToken;

    @JsonProperty("invite_url")
    private final String inviteUrl;

    private final FamilyInvitationStatus status;

    @JsonProperty("expires_at")
    private final Instant expiresAt;

    @JsonProperty("created_at")
    private final Instant createdAt;
}