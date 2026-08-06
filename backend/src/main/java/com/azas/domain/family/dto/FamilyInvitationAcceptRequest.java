package com.azas.domain.family.dto;

import com.azas.domain.child.entity.RelationType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FamilyInvitationAcceptRequest {

    @JsonProperty("relation_type")
    private RelationType relationType;
}