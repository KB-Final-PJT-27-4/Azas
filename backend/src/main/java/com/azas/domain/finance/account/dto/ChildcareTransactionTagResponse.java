package com.azas.domain.finance.account.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@ApiModel(description = "거래내역 양육비 포함 설정 응답")
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ChildcareTransactionTagResponse {

    @ApiModelProperty(value = "계좌 거래 ID", required = true, example = "901")
    @JsonProperty("account_transaction_id")
    private final long accountTransactionId;

    @ApiModelProperty(value = "양육비 포함 여부", required = true, example = "true")
    @JsonProperty("childcare_included")
    private final boolean childcareIncluded;

    @ApiModelProperty(value = "양육비 대상 자녀 ID. 미포함이면 null", example = "6")
    @JsonProperty("childcare_child_id")
    private final Long childcareChildId;

    public static ChildcareTransactionTagResponse of(
            long accountTransactionId,
            Long childId
    ) {
        return new ChildcareTransactionTagResponse(
                accountTransactionId,
                childId != null,
                childId
        );
    }
}
