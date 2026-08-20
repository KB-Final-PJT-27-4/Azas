package com.azas.domain.report.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AssetReportGoalSnapshot {

    private Long financialGoalId;

    @JsonAlias("goal_name")
    private String title;

    private BigDecimal currentAmount;

    private BigDecimal targetAmount;

    private BigDecimal achievementRate;

    @JsonAlias("monthly_change_amount")
    private BigDecimal monthlySavedAmount;

    private BigDecimal monthlySavingTargetAmount;

    private List<AssetReportAccountSnapshot> linkedAccounts =
            new ArrayList<>();
}
