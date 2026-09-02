package com.azas.domain.report.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;

@ApiModel(description = "자산 리포트 월 목록 아이템")
@Getter
@RequiredArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AssetReportListItemResponse {

    @ApiModelProperty(example = "10001")
    private final Long assetReportId;

    @ApiModelProperty(example = "2026")
    private final int reportYear;

    @ApiModelProperty(example = "7")
    private final int reportMonth;

    @ApiModelProperty(example = "2026-07")
    private final String period;

    @ApiModelProperty(example = "20750000")
    private final BigDecimal totalAssetAmount;

    @ApiModelProperty(example = "350000")
    private final BigDecimal totalAssetChangeAmount;

    @ApiModelProperty(example = "1250000")
    private final BigDecimal monthlySavedAmount;

    @ApiModelProperty(example = "41.5")
    private final BigDecimal goalAchievementRate;

    private final Instant createdAt;

    private final Instant updatedAt;

    public static AssetReportListItemResponse from(
            AssetReportListRow row
    ) {
        return new AssetReportListItemResponse(
                row.getAssetReportId(),
                row.getReportMonth().getYear(),
                row.getReportMonth().getMonthValue(),
                row.getReportMonth().toString().substring(0, 7),
                row.getTotalAssetAmount(),
                row.getTotalAssetChangeAmount(),
                row.getMonthlySavedAmount(),
                row.getGoalAchievementRate(),
                row.getCreatedAt().toInstant(ZoneOffset.UTC),
                row.getUpdatedAt().toInstant(ZoneOffset.UTC)
        );
    }
}