package com.azas.domain.finance.account.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;

@ApiModel(description = "거래내역 상세 응답")
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AccountTransactionDetailResponse {

    @ApiModelProperty(value = "계좌 거래 ID", required = true,
            example = "901")
    @JsonProperty("account_transaction_id")
    private final Long accountTransactionId;

    @ApiModelProperty(value = "거래 발생 시각(UTC)", required = true,
            example = "2026-07-23T06:00:00Z")
    @JsonProperty("occurred_at")
    private final Instant occurredAt;

    @ApiModelProperty(value = "거래 원장 계좌 기준 입출금 구분",
            required = true, allowableValues = "CREDIT,DEBIT")
    private final String direction;

    @ApiModelProperty(value = "양수 거래 금액", required = true,
            example = "100000.00")
    private final BigDecimal amount;

    @ApiModelProperty(value = "거래 메모. 없으면 null")
    private final String memo;

    @ApiModelProperty(value = "입금처", required = true)
    @JsonProperty("deposit_account")
    private final AccountTransactionPartyResponse depositAccount;

    @ApiModelProperty(value = "출금처", required = true)
    @JsonProperty("withdrawal_account")
    private final AccountTransactionPartyResponse withdrawalAccount;

    @ApiModelProperty(value = "거래 직후 원장 계좌 잔액. 없으면 null",
            example = "500000.00")
    @JsonProperty("balance_after")
    private final BigDecimal balanceAfter;

    public static AccountTransactionDetailResponse from(
            AccountTransactionDetailResult result
    ) {
        return new AccountTransactionDetailResponse(
                result.getAccountTransactionId(),
                result.getOccurredAt().toInstant(ZoneOffset.UTC),
                result.getDirection(),
                result.getAmount(),
                result.getMemo(),
                AccountTransactionPartyResponse.from(
                        result.getDepositAccount()
                ),
                AccountTransactionPartyResponse.from(
                        result.getWithdrawalAccount()
                ),
                result.getBalanceAfter()
        );
    }
}
