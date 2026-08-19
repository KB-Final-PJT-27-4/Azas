package com.azas.domain.dashboard.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@Getter
@RequiredArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ParentDashboardResponse {

    private final ChildInfo child;
    private final AssetSummary assetSummary;
    private final PrimaryGoal primaryGoal;
    private final QuickSummary quickSummary;
    private final ChecklistSummary checklistSummary;
    private final NotificationSummary notifications;


    public static ParentDashboardResponse from(
            ParentDashboardSummaryRow summary,
            List<ParentDashboardFlowRow> flowRows,
            List<ParentDashboardChecklistRow> checklistRows
    ) {
        PrimaryGoal primaryGoal = null;

        if (summary.getPrimaryGoalId() != null) {
            primaryGoal = new PrimaryGoal(
                    summary.getPrimaryGoalId(),
                    summary.getPrimaryGoalTitle(),
                    zero(summary.getPrimaryGoalSavedAmount()),
                    zero(summary.getPrimaryGoalTargetAmount()),
                    calculateRate(
                            summary.getPrimaryGoalSavedAmount(),
                            summary.getPrimaryGoalTargetAmount()
                    )
            );
        }

        TimeCapsuleSummary timeCapsule = null;

        if (summary.getNearestTimeCapsuleId() != null) {
            timeCapsule = new TimeCapsuleSummary(
                    summary.getNearestTimeCapsuleId(),
                    summary.getNearestTimeCapsuleTitle(),
                    summary.getNearestTimeCapsuleDDay()
            );
        }

        List<FlowItem> flows = flowRows.stream()
                .map(row -> new FlowItem(
                        row.getReportMonth(),
                        zero(row.getTotalAssetAmount())
                ))
                .collect(Collectors.toList());

        List<ChecklistItem> previewItems = checklistRows.stream()
                .map(row -> new ChecklistItem(
                        row.getChecklistItemId(),
                        row.getTitle(),
                        row.getStatus()
                ))
                .collect(Collectors.toList());

        int totalCount = summary.getChecklistTotalCount();
        int completedCount = summary.getChecklistCompletedCount();

        return new ParentDashboardResponse(
                new ChildInfo(
                        summary.getChildId(),
                        summary.getChildName(),
                        summary.getProfileImageUrl()
                ),
                new AssetSummary(
                        zero(summary.getTotalAssetAmount()),
                        zero(summary.getTotalAssetChangeAmount()),
                        flows
                ),
                primaryGoal,
                new QuickSummary(
                        summary.getActiveGoalCount(),
                        timeCapsule
                ),
                new ChecklistSummary(
                        totalCount,
                        completedCount,
                        calculateRate(
                                BigDecimal.valueOf(completedCount),
                                BigDecimal.valueOf(totalCount)
                        ),
                        Math.max(totalCount - completedCount, 0),
                        previewItems
                ),
                new NotificationSummary(
                        summary.getUnreadNotificationCount()
                )
        );
    }

    private static BigDecimal calculateRate(
            BigDecimal current,
            BigDecimal target
    ) {
        if (current == null
                || target == null
                || target.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2);
        }

        return current
                .multiply(BigDecimal.valueOf(100))
                .divide(target, 2, RoundingMode.HALF_UP);
    }

    private static BigDecimal zero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    @Getter
    @RequiredArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ChildInfo {
        private final Long childId;
        private final String name;
        private final String profileImageUrl;
    }

    @Getter
    @RequiredArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class AssetSummary {
        private final BigDecimal totalAssetAmount;
        private final BigDecimal totalAssetChangeAmount;
        private final List<FlowItem> sixMonthFlow;
    }

    @Getter
    @RequiredArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class FlowItem {
        private final String reportMonth;
        private final BigDecimal totalAssetAmount;
    }

    @Getter
    @RequiredArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class PrimaryGoal {
        private final Long financialGoalId;
        private final String title;
        private final BigDecimal savedAmount;
        private final BigDecimal targetAmount;
        private final BigDecimal achievementRate;
    }

    @Getter
    @RequiredArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class QuickSummary {
        private final int activeGoalCount;
        private final TimeCapsuleSummary nearestTimeCapsule;
    }

    @Getter
    @RequiredArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class TimeCapsuleSummary {
        private final Long timeCapsuleId;
        private final String title;
        private final Integer dDay;
    }

    @Getter
    @RequiredArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ChecklistSummary {
        private final int totalCount;
        private final int completedCount;
        private final BigDecimal achievementRate;
        private final int remainingCount;
        private final List<ChecklistItem> previewItems;
    }

    @Getter
    @RequiredArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ChecklistItem {
        private final Long checklistItemId;
        private final String title;
        private final String status;
    }

    @Getter
    @RequiredArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class NotificationSummary {
        private final int unreadCount;
    }
}