package com.azas.domain.family.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FamilyInvitationChildResponse {

    @JsonProperty("child_id")
    private final Long childId;

    private final String name;
}