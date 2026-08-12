package com.azas.domain.finance.account.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@ApiModel(description = "계좌 거래내역 목록 응답")
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AccountTransactionListResponse {

    @ApiModelProperty(value = "금융 계좌 ID", required = true, example = "3")
    @JsonProperty("account_id")
    private final Long accountId;

    @ApiModelProperty(value = "거래내역 목록", required = true)
    private final List<AccountTransactionItemResponse> transactions;

    @ApiModelProperty(value = "다음 페이지 커서. 마지막 페이지는 null")
    @JsonProperty("next_cursor")
    private final String nextCursor;

    @ApiModelProperty(value = "다음 페이지 존재 여부", required = true,
            example = "false")
    @JsonProperty("has_next")
    private final boolean hasNext;

    public static AccountTransactionListResponse from(
            AccountTransactionListResult result
    ) {
        List<AccountTransactionItemResponse> transactions = result
                .getTransactions()
                .stream()
                .map(AccountTransactionItemResponse::from)
                .toList();

        return new AccountTransactionListResponse(
                result.getAccountId(),
                transactions,
                result.getNextCursor(),
                result.isHasNext()
        );
    }
}
