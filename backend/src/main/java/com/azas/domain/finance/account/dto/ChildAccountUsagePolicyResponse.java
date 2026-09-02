package com.azas.domain.finance.account.dto;

import com.azas.domain.finance.account.entity.ChildUsageMode;
import com.azas.domain.finance.account.entity.FinancialAccountUsagePolicy;
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

@ApiModel(description = "자녀 계좌 사용 관리 정책")
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ChildAccountUsagePolicyResponse {

    @ApiModelProperty(
            value = "금융 계좌 ID",
            required = true,
            example = "15"
    )
    @JsonProperty("account_id")
    private final Long accountId;

    @ApiModelProperty(
            value = "자녀 ID",
            required = true,
            example = "6"
    )
    @JsonProperty("child_id")
    private final Long childId;

    @ApiModelProperty(
            value = "자녀 계좌 사용 관리 모드",
            allowableValues = "CO_MANAGED,UNRESTRICTED",
            example = "CO_MANAGED"
    )
    @JsonProperty("child_usage_mode")
    private final ChildUsageMode childUsageMode;

    @ApiModelProperty(
            value = "월간 사용 관리 기준 금액. "
                    + "실제 금융기관 제한 금액이 아닙니다.",
            example = "50000"
    )
    @JsonProperty("child_monthly_budget_amount")
    private final BigDecimal childMonthlyBudgetAmount;

    @ApiModelProperty(
            value = "사용 관리 정책 최종 변경 시각",
            example = "2026-08-08T10:00:00Z"
    )
    @JsonProperty("usage_policy_updated_at")
    private final Instant usagePolicyUpdatedAt;

    public static ChildAccountUsagePolicyResponse from(
            FinancialAccountUsagePolicy policy
    ) {
        return new ChildAccountUsagePolicyResponse(
                policy.getFinancialAccountId(),
                policy.getChildId(),
                policy.getChildUsageMode(),
                policy.getChildMonthlyBudgetAmount(),
                toInstant(policy.getUsagePolicyUpdatedAt())
        );
    }

    private static Instant toInstant(
            LocalDateTime dateTime
    ) {
        return dateTime == null
                ? null
                : dateTime.toInstant(ZoneOffset.UTC);
    }
}