package com.azas.domain.finance.account.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@ApiModel(description = "거래내역 양육비 포함 설정 요청")
@Getter
@NoArgsConstructor
public class ChildcareTransactionTagRequest {

    @ApiModelProperty(
            value = "양육비 대상 자녀 ID. null이면 양육비 포함을 해제합니다.",
            example = "6",
            allowEmptyValue = true
    )
    @JsonProperty("child_id")
    private Long childId;
}
