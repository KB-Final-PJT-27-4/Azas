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

@ApiModel(description = "계좌 거래내역 목록 항목")
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AccountTransactionItemResponse {

    @ApiModelProperty(value = "계좌 거래 ID", required = true, example = "901")
    @JsonProperty("account_transaction_id")
    private final Long accountTransactionId;

    @ApiModelProperty(value = "거래 발생 시각(UTC)", required = true,
            example = "2026-07-21T02:01:00Z")
    @JsonProperty("occurred_at")
    private final Instant occurredAt;

    @ApiModelProperty(value = "거래 상대 표시명. 확인할 수 없으면 null",
            example = "KB국민 5678")
    @JsonProperty("counterparty_name")
    private final String counterpartyName;

    @ApiModelProperty(value = "현재 계좌 기준 입출금 구분", required = true,
            allowableValues = "CREDIT,DEBIT", example = "CREDIT")
    private final String direction;

    @ApiModelProperty(value = "양수 거래 금액", required = true,
            example = "100000.00")
    private final BigDecimal amount;

    @JsonProperty("childcare_included")
    private final boolean childcareIncluded;

    @JsonProperty("childcare_child_id")
    private final Long childcareChildId;

    public static AccountTransactionItemResponse from(
            AccountTransactionItemResult result
    ) {
        return new AccountTransactionItemResponse(
                result.getAccountTransactionId(),
                result.getOccurredAt().toInstant(ZoneOffset.UTC),
                result.getCounterpartyName(),
                result.getDirection(),
                result.getAmount(),
                result.isChildcareIncluded(),
                result.getChildcareChildId()
        );
    }
}
