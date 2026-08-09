package com.azas.domain.finance.account.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@ApiModel(description = "부모 계좌 목록 조회 응답")
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ParentAccountListResponse {

    @ApiModelProperty(
            value = "부모 본인 명의 연결 계좌 목록",
            required = true
    )
    private final List<ParentAccountListItemResponse> accounts;

    @ApiModelProperty(
            value = "조회된 계좌 수",
            required = true,
            example = "1"
    )
    @JsonProperty("total_count")
    private final int totalCount;

    public static ParentAccountListResponse from(
            ParentAccountListResult result
    ) {
        List<ParentAccountListItemResponse> accounts =
                result.getAccounts()
                        .stream()
                        .map(ParentAccountListItemResponse::from)
                        .toList();

        return new ParentAccountListResponse(
                accounts,
                accounts.size()
        );
    }
}
