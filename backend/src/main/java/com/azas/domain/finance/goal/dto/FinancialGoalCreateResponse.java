package com.azas.domain.finance.goal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Getter
public class FinancialGoalCreateResponse {

    @JsonProperty("financial_goal_id")
    private final long financialGoalId;

    @JsonProperty("child_id")
    private final long childId;

    @JsonProperty("financial_goal_template_id")
    private final Long financialGoalTemplateId;

    private final String title;

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

    @JsonProperty("created_at")
    private final Instant createdAt;

    private FinancialGoalCreateResponse(FinancialGoalCreateResult result) {
        this.financialGoalId = result.getFinancialGoalId();
        this.childId = result.getChildId();
        this.financialGoalTemplateId = result.getFinancialGoalTemplateId();
        this.title = result.getTitle();
        this.targetAmount = result.getTargetAmount();
        this.targetDate = result.getTargetDate();
        this.monthlySavingAmount = result.getMonthlySavingAmount();
        this.currentAmount = result.getCurrentAmount();
        this.remainingAmount = result.getRemainingAmount();
        this.achievementRate = result.getAchievementRate();
        this.status = result.getStatus();
        this.linkedAccounts = result.getLinkedAccounts().stream()
                .map(LinkedAccountResponse::new)
                .toList();
        this.linkedAccountCount = linkedAccounts.size();
        this.createdAt = result.getCreatedAt();
    }

    public static FinancialGoalCreateResponse from(FinancialGoalCreateResult result) {
        return new FinancialGoalCreateResponse(result);
    }

    @Getter
    public static class LinkedAccountResponse {

        @JsonProperty("account_id")
        private final long accountId;

        @JsonProperty("account_name")
        private final String accountName;

        @JsonProperty("bank_name")
        private final String bankName;

        private final BigDecimal balance;

        private LinkedAccountResponse(FinancialGoalLinkedAccountResult account) {
            this.accountId = account.getAccountId();
            this.accountName = account.getAccountName();
            this.bankName = account.getBankName();
            this.balance = account.getBalance();
        }
    }
}
