package com.azas.domain.report.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AssetReportGoalSnapshot {

    private Long financialGoalId;

    private String title;

    private BigDecimal currentAmount;

    private BigDecimal targetAmount;

    private BigDecimal achievementRate;

    private BigDecimal monthlySavedAmount;

    private BigDecimal monthlySavingTargetAmount;

    private List<AssetReportAccountSnapshot> linkedAccounts =
            new ArrayList<>();
}