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

@ApiModel(description = "수락 대기 중인 자녀 초대 정보")
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ChildInviteInvitationResponse {

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
            allowableValues = "CHILD",
            example = "CHILD"
    )
    @JsonProperty("invitee_type")
    private final FamilyInviteeType inviteeType;

    @ApiModelProperty(
            value = "초대 상태",
            required = true,
            allowableValues = "PENDING",
            example = "PENDING"
    )
    private final FamilyInvitationStatus status;

    @ApiModelProperty(
            value = "초대 만료 시각",
            required = true,
            example = "2026-08-08T03:00:00Z"
    )
    @JsonProperty("expires_at")
    private final Instant expiresAt;

    public static ChildInviteInvitationResponse from(
            ChildInviteOAuthResult result
    ) {
        return new ChildInviteInvitationResponse(
                result.getFamilyInvitationId(),
                FamilyInviteeType.CHILD,
                FamilyInvitationStatus.PENDING,
                result.getExpiresAt()
                        .toInstant(ZoneOffset.UTC)
        );
    }
}