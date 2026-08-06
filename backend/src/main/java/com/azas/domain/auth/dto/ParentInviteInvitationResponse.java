package com.azas.domain.auth.dto;

import com.azas.domain.auth.entity.FamilyInvitationStatus;
import com.azas.domain.auth.entity.FamilyInviteeType;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.time.ZoneOffset;

@ApiModel(description = "수락 완료된 부모 초대 정보")
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ParentInviteInvitationResponse {

    @ApiModelProperty(
            value = "가족 초대 ID",
            required = true,
            example = "1"
    )
    @JsonProperty("family_invitation_id")
    private final Long familyInvitationId;

    @ApiModelProperty(
            value = "초대 대상 유형",
            required = true,
            allowableValues = "PARENT",
            example = "PARENT"
    )
    @JsonProperty("invitee_type")
    private final FamilyInviteeType inviteeType;

    @ApiModelProperty(
            value = "초대 상태",
            required = true,
            allowableValues = "ACCEPTED",
            example = "ACCEPTED"
    )
    private final FamilyInvitationStatus status;

    @ApiModelProperty(
            value = "초대 수락 시각",
            required = true,
            example = "2026-08-05T03:00:00Z"
    )
    @JsonProperty("accepted_at")
    private final Instant acceptedAt;

    public static ParentInviteInvitationResponse from(
            ParentInviteOAuthResult result
    ) {
        return new ParentInviteInvitationResponse(
                result.getFamilyInvitationId(),
                FamilyInviteeType.PARENT,
                FamilyInvitationStatus.ACCEPTED,
                result.getAcceptedAt()
                        .toInstant(ZoneOffset.UTC)
        );
    }
}