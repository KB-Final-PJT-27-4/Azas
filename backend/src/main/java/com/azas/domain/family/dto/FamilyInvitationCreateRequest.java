package com.azas.domain.family.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Getter
@NoArgsConstructor
public class FamilyInvitationCreateRequest {

    @Min(1)
    @Max(168)
    @JsonProperty("expires_in_hours")
    private Integer expiresInHours;
}
