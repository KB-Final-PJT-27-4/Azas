package com.azas.domain.finance.goal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@ApiModel(description = "자녀 금융 목표 상세 조회 응답")
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class FinancialGoalDetailResponse {

    @JsonProperty("financial_goal_id")
    private final long financialGoalId;

    @JsonProperty("child_id")
    private final long childId;

    @JsonProperty("financial_goal_template_id")
    private final Long financialGoalTemplateId;

    private final String title;

    @JsonProperty("icon_key")
    private final String iconKey;

    @JsonProperty("target_amount")
    private final BigDecimal targetAmount;

    @JsonProperty("target_date")
    private final LocalDate targetDate;

    @JsonProperty("monthly_saving_amount")
    private final BigDecimal monthlySavingAmount;

    @JsonProperty("current_amount")
    private final BigDecimal currentAmount;

    @JsonProperty("remaining_amount")
    private final BigDecimal remainingAmount;

    @JsonProperty("achievement_rate")
    private final BigDecimal achievementRate;

    private final String status;

    @JsonProperty("linked_account_count")
    private final int linkedAccountCount;

    @JsonProperty("linked_accounts")
    private final List<LinkedAccountResponse> linkedAccounts;

    private final List<CheckpointResponse> checkpoints;

    public static FinancialGoalDetailResponse from(
            FinancialGoalDetailResult result
    ) {
        List<LinkedAccountResponse> accounts = result.getLinkedAccounts()
                .stream()
                .map(LinkedAccountResponse::from)
                .toList();
        List<CheckpointResponse> checkpoints = result.getCheckpoints()
                .stream()
                .map(CheckpointResponse::from)
                .toList();
        return new FinancialGoalDetailResponse(
                result.getFinancialGoalId(),
                result.getChildId(),
                result.getFinancialGoalTemplateId(),
                result.getTitle(),
                result.getIconKey(),
                result.getTargetAmount(),
                result.getTargetDate(),
                result.getMonthlySavingAmount(),
                result.getCurrentAmount(),
                result.getRemainingAmount(),
                result.getAchievementRate(),
                result.getStatus(),
                accounts.size(),
                accounts,
                checkpoints
        );
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

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class CheckpointResponse {

        @JsonProperty("financial_goal_checkpoint_id")
        private final long financialGoalCheckpointId;

        private final int percentage;

        @JsonProperty("target_amount")
        private final BigDecimal targetAmount;

        private final boolean reached;

        @JsonProperty("reached_at")
        private final Instant reachedAt;

        private static CheckpointResponse from(
                FinancialGoalCheckpointResult result
        ) {
            return new CheckpointResponse(
                    result.getFinancialGoalCheckpointId(),
                    result.getPercentage(),
                    result.getTargetAmount(),
                    result.isReached(),
                    result.getReachedAt()
            );
        }
    }
}
