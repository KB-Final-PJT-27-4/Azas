package com.azas.domain.finance.account.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@ApiModel(description = "자녀 계좌 목록 응답")
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ChildAccountListResponse {

    @ApiModelProperty(value = "조회 대상 자녀 ID", required = true, example = "1")
    @JsonProperty("child_id")
    private final long childId;

    @ApiModelProperty(value = "자녀 연결 계좌 목록", required = true)
    private final List<ChildAccountListItemResponse> accounts;

    @ApiModelProperty(value = "조회된 계좌 수", required = true, example = "2")
    @JsonProperty("total_count")
    private final int totalCount;

    public static ChildAccountListResponse from(
            ChildAccountListResult result
    ) {
        List<ChildAccountListItemResponse> accounts =
                result.getAccounts().stream()
                        .map(ChildAccountListItemResponse::from)
                        .toList();

        return new ChildAccountListResponse(
                result.getChildId(),
                accounts,
                accounts.size()
        );
    }
}
