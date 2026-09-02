package com.azas.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@ApiModel(description = "자녀 초대 대상 정보")
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ChildInviteChildResponse {

    @ApiModelProperty(
            value = "자녀 ID",
            required = true,
            example = "1"
    )
    @JsonProperty("child_id")
    private final Long childId;

    @ApiModelProperty(
            value = "부모가 등록한 자녀 이름",
            required = true,
            example = "김자녀"
    )
    private final String name;

    public static ChildInviteChildResponse from(
            ChildInviteOAuthResult result
    ) {
        return new ChildInviteChildResponse(
                result.getChildId(),
                result.getChildName()
        );
    }
}