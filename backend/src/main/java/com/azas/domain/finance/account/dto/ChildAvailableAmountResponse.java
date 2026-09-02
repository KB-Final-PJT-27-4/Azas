package com.azas.domain.finance.account.dto;

import com.azas.domain.finance.account.entity.ChildUsageMode;
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

@ApiModel(description = "자녀 본인 월간 계좌 사용 관리 현황")
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ChildAvailableAmountResponse {

    @ApiModelProperty(
            value = "자녀 ID",
            required = true,
            example = "6"
    )
    @JsonProperty("child_id")
    private final Long childId;

    @ApiModelProperty(
            value = "대표 입출금 계좌 ID",
            required = true,
            example = "15"
    )
    @JsonProperty("account_id")
    private final Long accountId;

    @ApiModelProperty(
            value = "자녀 계좌 사용 관리 모드",
            required = true,
            allowableValues = "CO_MANAGED,UNRESTRICTED",
            example = "CO_MANAGED"
    )
    @JsonProperty("child_usage_mode")
    private final ChildUsageMode childUsageMode;

    @ApiModelProperty(
            value = "월간 사용 관리 기준 금액. "
                    + "UNRESTRICTED이면 null이며 실제 결제 한도가 아닙니다.",
            example = "100000.00"
    )
    @JsonProperty("child_monthly_budget_amount")
    private final BigDecimal childMonthlyBudgetAmount;

    @ApiModelProperty(
            value = "현재 달 출금 거래 합계",
            required = true,
            example = "35000.00"
    )
    @JsonProperty("current_month_spent_amount")
    private final BigDecimal currentMonthSpentAmount;

    @ApiModelProperty(
            value = "월간 기준액에서 사용액을 뺀 참고 금액. "
                    + "0 미만이 되지 않으며 UNRESTRICTED이면 null입니다.",
            example = "65000.00"
    )
    @JsonProperty("remaining_guidance_amount")
    private final BigDecimal remainingGuidanceAmount;

    @ApiModelProperty(
            value = "월간 관리 기준액 초과 여부. "
                    + "UNRESTRICTED이면 null입니다.",
            example = "false"
    )
    @JsonProperty("budget_exceeded")
    private final Boolean budgetExceeded;

    @ApiModelProperty(
            value = "사용액 집계 대상 월(UTC)",
            required = true,
            example = "2026-08"
    )
    private final String period;

    @ApiModelProperty(
            value = "사용 현황 계산 시각",
            required = true,
            example = "2026-08-11T10:00:00Z"
    )
    @JsonProperty("calculated_at")
    private final Instant calculatedAt;

    public static ChildAvailableAmountResponse from(
            ChildAvailableAmountResult result
    ) {
        return new ChildAvailableAmountResponse(
                result.getChildId(),
                result.getAccountId(),
                result.getChildUsageMode(),
                result.getChildMonthlyBudgetAmount(),
                result.getCurrentMonthSpentAmount(),
                result.getRemainingGuidanceAmount(),
                result.getBudgetExceeded(),
                result.getPeriod(),
                toInstant(result.getCalculatedAt())
        );
    }

    private static Instant toInstant(LocalDateTime dateTime) {
        return dateTime == null
                ? null
                : dateTime.toInstant(ZoneOffset.UTC);
    }
}
