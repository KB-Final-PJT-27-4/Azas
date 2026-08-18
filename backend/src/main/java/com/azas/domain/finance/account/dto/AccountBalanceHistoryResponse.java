package com.azas.domain.finance.account.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.format.DateTimeFormatter;
import java.util.List;

@ApiModel(description = "계좌 월별 잔액 변화 조회 응답")
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AccountBalanceHistoryResponse {

    private static final DateTimeFormatter MONTH_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM");

    @ApiModelProperty(value = "금융 계좌 ID", required = true, example = "2")
    @JsonProperty("account_id")
    private final Long accountId;

    @ApiModelProperty(value = "조회 개월 수", required = true, example = "6")
    private final int months;

    @ApiModelProperty(value = "조회 시작 월", required = true,
            example = "2026-03")
    @JsonProperty("start_month")
    private final String startMonth;

    @ApiModelProperty(value = "조회 종료 월", required = true,
            example = "2026-08")
    @JsonProperty("end_month")
    private final String endMonth;

    @ApiModelProperty(value = "월별 잔액 및 순변화 목록", required = true)
    @JsonProperty("balance_history")
    private final List<MonthlyAccountBalanceResponse> balanceHistory;

    public static AccountBalanceHistoryResponse from(
            AccountBalanceHistoryResult result
    ) {
        List<MonthlyAccountBalanceResponse> history = result
                .getBalanceHistory()
                .stream()
                .map(MonthlyAccountBalanceResponse::from)
                .toList();

        return new AccountBalanceHistoryResponse(
                result.getAccountId(),
                result.getMonths(),
                result.getStartMonth().format(MONTH_FORMATTER),
                result.getEndMonth().format(MONTH_FORMATTER),
                history
        );
    }
}
