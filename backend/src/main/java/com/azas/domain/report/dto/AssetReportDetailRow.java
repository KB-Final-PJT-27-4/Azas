package com.azas.domain.report.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class AssetReportDetailRow {

    private Long assetReportId;

    private Long childId;

    private LocalDate reportMonth;

    private BigDecimal totalAssetAmount;

    private BigDecimal totalAssetChangeAmount;

    private BigDecimal monthlySavedAmount;

    private BigDecimal totalGoalTargetAmount;

    private BigDecimal totalGoalSavedAmount;

    private BigDecimal goalAchievementRate;

    private String savingsGoalSummaryJson;

    private String insightItemsJson;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}