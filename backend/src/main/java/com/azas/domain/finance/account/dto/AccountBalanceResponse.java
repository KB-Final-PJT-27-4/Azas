package com.azas.domain.finance.account.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@ApiModel(description = "계좌 최신 잔액 조회 응답")
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AccountBalanceResponse {

    @ApiModelProperty(value = "금융 계좌 ID", required = true, example = "2")
    @JsonProperty("account_id")
    private final Long accountId;

    @ApiModelProperty(value = "마지막 금융정보 동기화 기준 잔액",
            required = true, example = "1250000.00")
    private final BigDecimal balance;

    @ApiModelProperty(value = "잔액 기준 시각", required = true,
            example = "2026-08-10T05:30:00Z")
    @JsonProperty("balance_updated_at")
    private final Instant balanceUpdatedAt;

    public static AccountBalanceResponse from(
            AccountBalanceResult result
    ) {
        return new AccountBalanceResponse(
                result.getAccountId(),
                result.getBalance(),
                toInstant(result.getBalanceUpdatedAt())
        );
    }

    private static Instant toInstant(LocalDateTime dateTime) {
        return dateTime.toInstant(ZoneOffset.UTC);
    }
}
