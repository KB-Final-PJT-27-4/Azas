package com.azas.domain.finance.account.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@ApiModel(description = "연결 가능한 Mock 계좌 목록 응답")
@Getter
@RequiredArgsConstructor
public class DiscoveredAccountListResponse {

    @ApiModelProperty(value = "Mock 계좌 후보 목록", required = true)
    private final List<DiscoveredAccountResponse> accounts;

    public static DiscoveredAccountListResponse from(
            DiscoveredAccountListResult result
    ) {
        return new DiscoveredAccountListResponse(
                result.getAccounts().stream()
                        .map(DiscoveredAccountResponse::from)
                        .toList()
        );
    }
}
