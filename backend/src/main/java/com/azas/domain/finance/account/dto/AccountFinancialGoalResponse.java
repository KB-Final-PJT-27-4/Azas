package com.azas.domain.finance.account.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@ApiModel(description = "계좌에 설정된 금융 목표 스냅샷")
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AccountFinancialGoalResponse {

    @ApiModelProperty(value = "목표명", example = "노트북 구매")
    @JsonProperty("goal_name")
    private final String goalName;

    @ApiModelProperty(value = "목표 금액", example = "1500000.00")
    @JsonProperty("target_amount")
    private final BigDecimal targetAmount;

    @ApiModelProperty(value = "목표일", example = "2027-02-28")
    @JsonProperty("target_date")
    private final LocalDate targetDate;

    public static AccountFinancialGoalResponse from(
            AccountFinancialGoalResult result
    ) {
        return new AccountFinancialGoalResponse(
                result.getGoalName(),
                result.getTargetAmount(),
                result.getTargetDate()
        );
    }
}
