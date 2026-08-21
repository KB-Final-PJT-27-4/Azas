package com.azas.domain.dashboard.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ParentDashboardSummaryRow {

    private Long childId;
    private String childName;
    private String profileImageUrl;

    private BigDecimal totalAssetAmount;
    private BigDecimal totalAssetChangeAmount;

    private Long primaryGoalId;
    private String primaryGoalTitle;
    private BigDecimal primaryGoalSavedAmount;
    private BigDecimal primaryGoalTargetAmount;

    private int activeGoalCount;

    private Long nearestTimeCapsuleId;
    private String nearestTimeCapsuleTitle;
    private Integer nearestTimeCapsuleDDay;

    private int checklistTotalCount;
    private int checklistCompletedCount;

    private int unreadNotificationCount;
}