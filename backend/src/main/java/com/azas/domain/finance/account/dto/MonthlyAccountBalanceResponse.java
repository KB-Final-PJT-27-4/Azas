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
import java.time.format.DateTimeFormatter;

@ApiModel(description = "계좌 월별 잔액 정보")
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MonthlyAccountBalanceResponse {

    private static final DateTimeFormatter MONTH_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM");

    @ApiModelProperty(value = "조회 월", required = true, example = "2026-08")
    private final String month;

    @ApiModelProperty(value = "해당 월 마지막 잔액. 스냅샷이 없으면 null",
            example = "1250000.00")
    private final BigDecimal balance;

    @ApiModelProperty(
            value = "직전 달 마지막 잔액 대비 순변화액. 비교할 수 없으면 null",
            example = "150000.00"
    )
    @JsonProperty("change_amount")
    private final BigDecimal changeAmount;

    @ApiModelProperty(
            value = "해당 월 마지막 잔액 스냅샷 시각. 스냅샷이 없으면 null",
            example = "2026-08-10T05:30:00Z"
    )
    @JsonProperty("observed_at")
    private final Instant observedAt;

    public static MonthlyAccountBalanceResponse from(
            MonthlyAccountBalanceResult result
    ) {
        return new MonthlyAccountBalanceResponse(
                result.getMonth().format(MONTH_FORMATTER),
                result.getBalance(),
                result.getChangeAmount(),
                result.getObservedAt() == null
                        ? null
                        : result.getObservedAt().toInstant(ZoneOffset.UTC)
        );
    }
}
