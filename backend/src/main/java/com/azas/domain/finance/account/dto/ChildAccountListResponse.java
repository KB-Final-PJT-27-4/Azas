package com.azas.domain.finance.account.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@ApiModel(description = "자녀 계좌 목록 조회 응답")
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ChildAccountListResponse {

    @ApiModelProperty(value = "조회 대상 자녀 ID", required = true,
            example = "6")
    @JsonProperty("child_id")
    private final long childId;

    @ApiModelProperty(value = "자녀 계좌 잔액 합계", required = true,
            example = "14600000.00")
    @JsonProperty("total_balance")
    private final BigDecimal totalBalance;

    @ApiModelProperty(value = "연결 계좌 수", required = true, example = "2")
    @JsonProperty("total_count")
    private final int totalCount;

    @ApiModelProperty(value = "자녀 명의 활성 계좌 목록", required = true)
    private final List<ChildAccountListItemResponse> accounts;

    public static ChildAccountListResponse from(
            ChildAccountListResult result
    ) {
        List<ChildAccountListItemResponse> accounts = result.getAccounts()
                .stream()
                .map(ChildAccountListItemResponse::from)
                .toList();

        return new ChildAccountListResponse(
                result.getChildId(),
                result.getTotalBalance(),
                accounts.size(),
                accounts
        );
    }
}
