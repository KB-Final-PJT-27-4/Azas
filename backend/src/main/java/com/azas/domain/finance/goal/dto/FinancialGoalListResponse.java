package com.azas.domain.finance.goal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@ApiModel(description = "자녀 금융 목표 목록 조회 응답")
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class FinancialGoalListResponse {

    @ApiModelProperty(value = "자녀 ID", required = true, example = "6")
    @JsonProperty("child_id")
    private final long childId;

    @ApiModelProperty(value = "목표 수", required = true, example = "2")
    @JsonProperty("total_count")
    private final int totalCount;

    @ApiModelProperty(value = "진행 중·달성 목표 목록", required = true)
    @JsonProperty("financial_goals")
    private final List<GoalResponse> financialGoals;

    public static FinancialGoalListResponse from(FinancialGoalListResult result) {
        List<GoalResponse> goals = result.getFinancialGoals().stream()
                .map(GoalResponse::from)
                .toList();
        return new FinancialGoalListResponse(
                result.getChildId(), goals.size(), goals);
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class GoalResponse {

        @JsonProperty("financial_goal_id")
        private final long financialGoalId;

        @JsonProperty("financial_goal_template_id")
        private final Long financialGoalTemplateId;

        private final String title;

        @JsonProperty("icon_key")
        private final String iconKey;

        @JsonProperty("target_amount")
        private final BigDecimal targetAmount;

        @JsonProperty("current_amount")
        private final BigDecimal currentAmount;

        @JsonProperty("remaining_amount")
        private final BigDecimal remainingAmount;

        @JsonProperty("achievement_rate")
        private final BigDecimal achievementRate;

        @JsonProperty("target_date")
        private final LocalDate targetDate;

        private final String status;

        @JsonProperty("linked_account_count")
        private final int linkedAccountCount;

        @JsonProperty("linked_accounts")
        private final List<LinkedAccountResponse> linkedAccounts;

        private static GoalResponse from(FinancialGoalListItemResult result) {
            List<LinkedAccountResponse> accounts = result.getLinkedAccounts()
                    .stream()
                    .map(LinkedAccountResponse::from)
                    .toList();
            return new GoalResponse(
                    result.getFinancialGoalId(),
                    result.getFinancialGoalTemplateId(),
                    result.getTitle(),
                    result.getIconKey(),
                    result.getTargetAmount(),
                    result.getCurrentAmount(),
                    result.getRemainingAmount(),
                    result.getAchievementRate(),
                    result.getTargetDate(),
                    result.getStatus(),
                    accounts.size(),
                    accounts
            );
        }
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class LinkedAccountResponse {

        @JsonProperty("account_id")
        private final long accountId;

        @JsonProperty("account_name")
        private final String accountName;

        @JsonProperty("bank_name")
        private final String bankName;

        @JsonProperty("account_number")
        private final String accountNumber;

        private final BigDecimal balance;

        private static LinkedAccountResponse from(
                FinancialGoalListAccountResult result
        ) {
            return new LinkedAccountResponse(
                    result.getAccountId(),
                    result.getAccountName(),
                    result.getBankName(),
                    result.getAccountNumber(),
                    result.getBalance()
            );
        }
    }
}
