package com.azas.domain.report.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class AssetReportUpsertCommand {

    private final Long childId;
    private final LocalDate reportMonth;

    private final BigDecimal totalAssetAmount;
    private final BigDecimal totalAssetChangeAmount;
    private final BigDecimal monthlySavedAmount;

    private final BigDecimal totalGoalTargetAmount;
    private final BigDecimal totalGoalSavedAmount;
    private final BigDecimal goalAchievementRate;

    private final String sixMonthFlowJson;
    private final String savingsGoalSummaryJson;
    private final String insightItemsJson;
}