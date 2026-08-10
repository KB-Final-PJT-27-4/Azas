package com.azas.domain.finance.account.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@ApiModel(description = "자녀 명의 계좌의 자녀 정보")
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AccountDetailChildResponse {

    @ApiModelProperty(value = "자녀 ID", required = true, example = "1")
    @JsonProperty("child_id")
    private final Long childId;

    @ApiModelProperty(value = "자녀 이름", required = true, example = "김하늘")
    private final String name;

    public static AccountDetailChildResponse from(
            AccountDetailChildResult result
    ) {
        return new AccountDetailChildResponse(
                result.getChildId(),
                result.getName()
        );
    }
}
