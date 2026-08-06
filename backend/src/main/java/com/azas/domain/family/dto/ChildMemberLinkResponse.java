package com.azas.domain.family.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ChildMemberLinkResponse {

    @JsonProperty("child_id")
    private Long childId;

    private Boolean linked;

    @JsonProperty("child_member_id")
    private Long childMemberId;

    @JsonProperty("child_member_name")
    private String childMemberName;

    @JsonProperty("child_member_email")
    private String childMemberEmail;

    @JsonProperty("child_member_profile_image_url")
    private String childMemberProfileImageUrl;

    @JsonProperty("child_member_status")
    private String childMemberStatus;

    @JsonProperty("linked_at")
    private LocalDateTime linkedAt;
}