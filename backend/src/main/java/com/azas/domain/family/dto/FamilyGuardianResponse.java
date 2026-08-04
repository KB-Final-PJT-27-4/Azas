package com.azas.domain.family.dto;

import com.azas.domain.child.entity.RelationType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class FamilyGuardianResponse {

    @JsonProperty("member_id")
    private Long memberId;

    private String name;
    private String email;

    @JsonProperty("profile_image_url")
    private String profileImageUrl;

    @JsonProperty("relation_type")
    private RelationType relationType;

    @JsonProperty("family_role")
    private FamilyRole familyRole;

    @JsonProperty("linked_at")
    private LocalDateTime linkedAt;

    @JsonProperty("is_me")
    private Boolean isMe;



}
