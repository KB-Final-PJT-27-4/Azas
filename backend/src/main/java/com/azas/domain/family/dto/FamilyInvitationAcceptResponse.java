package com.azas.domain.family.dto;

import com.azas.domain.auth.entity.FamilyInvitationStatus;
import com.azas.domain.auth.entity.FamilyInviteeType;
import com.azas.domain.child.entity.RelationType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@AllArgsConstructor
public class FamilyInvitationAcceptResponse {

    @JsonProperty("family_invitation_id")
    private final Long familyInvitationId;

    private final FamilyInvitationStatus status;

    @JsonProperty("invitee_type")
    private final FamilyInviteeType inviteeType;

    @JsonProperty("accepted_at")
    private final Instant acceptedAt;

    private final FamilyInvitationChildResponse child;

    private final List<FamilyInvitationChildResponse> children;

    @JsonProperty("child_count")
    private final int childCount;

    @JsonProperty("relation_type")
    private final RelationType relationType;
}
