package com.azas.domain.family.dto;

import com.azas.domain.auth.entity.FamilyInviteeType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@NoArgsConstructor
public class FamilyInvitationCreateRequest {

    @NotNull
    @JsonProperty("invitee_type")
    private FamilyInviteeType inviteeType;

    @Min(1)
    @Max(168)
    @JsonProperty("expires_in_hours")
    private Integer expiresInHours;
}