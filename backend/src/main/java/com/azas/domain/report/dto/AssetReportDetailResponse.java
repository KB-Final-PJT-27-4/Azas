package com.azas.domain.report.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@ApiModel(description = "월간 자산 리포트 상세 응답")
@Getter
@RequiredArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AssetReportDetailResponse {

    @ApiModelProperty(example = "10001")
    private final Long assetReportId;

    @ApiModelProperty(example = "10")
    private final Long childId;

    @ApiModelProperty(example = "2026")
    private final int reportYear;

    @ApiModelProperty(example = "7")
    private final int reportMonth;

    private final Period period;

    private final Summary summary;

    private final List<GoalSummary> goalSummary;

    private final List<InsightItem> insightItems;

    private final Instant createdAt;

    private final Instant updatedAt;

    @Getter
    @RequiredArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @ApiModel(value = "AssetReportPeriodResponse")
    public static class Period {

        private final LocalDate startDate;

        private final LocalDate endDate;
    }

    @Getter
    @RequiredArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @ApiModel(value = "AssetReportSummaryResponse")
    public static class Summary {

        private final BigDecimal totalAssetAmount;

        private final BigDecimal totalAssetChangeAmount;

        private final BigDecimal totalGoalTargetAmount;

        private final BigDecimal totalGoalSavedAmount;

        private final BigDecimal goalAchievementRate;

        private final BigDecimal monthlySavedAmount;

        private final BigDecimal monthlySavingTargetAmount;

        private final BigDecimal monthlySavingAchievementRate;
    }

    @Getter
    @RequiredArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class GoalSummary {

        private final Long financialGoalId;

        private final String title;

        private final BigDecimal currentAmount;

        private final BigDecimal targetAmount;

        private final BigDecimal achievementRate;

        private final BigDecimal monthlySavedAmount;

        private final int linkedAccountCount;

        private final List<LinkedAccount> linkedAccounts;
    }

    @Getter
    @RequiredArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class LinkedAccount {

        private final Long accountId;

        private final String accountName;

        private final String bankName;

        private final String accountNumberMasked;

        private final BigDecimal balance;
    }

    @Getter
    @RequiredArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class InsightItem {

        private final String type;

        private final String title;

        private final String description;
    }
}
