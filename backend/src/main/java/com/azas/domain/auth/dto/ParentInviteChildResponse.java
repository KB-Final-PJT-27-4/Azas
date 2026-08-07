package com.azas.domain.auth.dto;

import com.azas.domain.child.entity.RelationType;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@ApiModel(description = "부모 회원과 관계가 등록된 자녀 정보")
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ParentInviteChildResponse {

    @ApiModelProperty(
            value = "자녀 ID",
            required = true,
            example = "1"
    )
    @JsonProperty("child_id")
    private final Long childId;

    @ApiModelProperty(
            value = "자녀 이름",
            required = true,
            example = "김자녀"
    )
    private final String name;

    @ApiModelProperty(
            value = "부모와 자녀의 관계",
            required = true,
            allowableValues = "MOTHER,FATHER,GUARDIAN",
            example = "GUARDIAN"
    )
    @JsonProperty("relation_type")
    private final RelationType relationType;

    public static ParentInviteChildResponse from(
            ParentInviteOAuthResult result
    ) {
        return new ParentInviteChildResponse(
                result.getChildId(),
                result.getChildName(),
                result.getRelationType()
        );
    }
}