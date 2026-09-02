package com.azas.domain.finance.account.dto;

import com.azas.domain.finance.account.entity.ChildUsageMode;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@ApiModel(description = "자녀 계좌 사용 관리 정책 설정 요청")
@Getter
@NoArgsConstructor
public class ChildAccountUsagePolicyRequest {

    @ApiModelProperty(
            value = "자녀 계좌 사용 관리 모드",
            required = true,
            allowableValues = "CO_MANAGED,UNRESTRICTED",
            example = "CO_MANAGED"
    )
    @NotNull
    @JsonProperty("child_usage_mode")
    private ChildUsageMode childUsageMode;

    @ApiModelProperty(
            value = "월간 사용 관리 기준 금액. "
                    + "CO_MANAGED일 때 필수이며 실제 금융기관 제한 금액이 아닙니다.",
            example = "50000"
    )
    @DecimalMin(value = "0.00")
    @Digits(integer = 17, fraction = 2)
    @JsonProperty("child_monthly_budget_amount")
    private BigDecimal childMonthlyBudgetAmount;
}