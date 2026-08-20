package com.azas.domain.report.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class AssetReportListRow {

    private Long assetReportId;

    private LocalDate reportMonth;

    private BigDecimal totalAssetAmount;

    private BigDecimal totalAssetChangeAmount;

    private BigDecimal monthlySavedAmount;

    private BigDecimal goalAchievementRate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}