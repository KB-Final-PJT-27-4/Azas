package com.azas.domain.dashboard.dto;

import com.azas.domain.finance.account.entity.ChildUsageMode;
import com.azas.domain.mission.entity.MissionStatus;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@ApiModel(description = "자녀 본인 홈 대시보드")
@Getter
@RequiredArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ChildDashboardResponse {

    private final ChildInfo child;
    private final SpendingSummary spendingSummary;
    private final ActivitySummary activitySummary;
    private final MissionSummary missionSummary;
    private final NotificationSummary notificationSummary;

    @Getter
    @RequiredArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @ApiModel(value = "ChildDashboardChildInfoResponse")
    public static class ChildInfo {
        @ApiModelProperty(example = "6")
        private final Long childId;
        @ApiModelProperty(example = "깨비")
        private final String name;
        private final String profileImageUrl;
    }

    @Getter
    @RequiredArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class SpendingSummary {
        @ApiModelProperty(example = "40002")
        private final Long accountId;
        @ApiModelProperty(
                allowableValues = "CO_MANAGED,UNRESTRICTED",
                example = "CO_MANAGED"
        )
        private final ChildUsageMode childUsageMode;
        @ApiModelProperty(
                value = "화면 상단에 표시할 사용 가능 금액",
                example = "6000"
        )
        private final BigDecimal displayAvailableAmount;
        @ApiModelProperty(
                value = "자녀 대표 입출금계좌의 실제 잔액",
                example = "96000"
        )
        private final BigDecimal accountBalance;
        @ApiModelProperty(
                value = "자녀 화면에서 실제 계좌 잔액을 숨기는지 여부",
                example = "true"
        )
        private final boolean accountBalanceHidden;
        @ApiModelProperty(example = "20000")
        private final BigDecimal monthlyBudgetAmount;
        @ApiModelProperty(example = "14000")
        private final BigDecimal currentMonthSpentAmount;
        @ApiModelProperty(example = "6000")
        private final BigDecimal remainingMonthlyBudgetAmount;
        @ApiModelProperty(
                value = "프로그레스바 표시용 0~100 사용률",
                example = "70"
        )
        private final Integer usageRate;
        private final Boolean budgetExceeded;
        @ApiModelProperty(example = "2026-08")
        private final String period;
    }

    @Getter
    @RequiredArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ActivitySummary {
        private final int pendingAllowanceRequestCount;
        private final int currentMonthTransactionCount;
    }

    @Getter
    @RequiredArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class MissionSummary {
        @ApiModelProperty(
                value = "ASSIGNED, SUBMITTED, REJECTED 상태 미션 수"
        )
        private final int activeCount;
        private final List<MissionItem> items;
    }

    @Getter
    @RequiredArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class MissionItem {
        private final Long missionId;
        private final String title;
        private final String description;
        private final BigDecimal rewardAmount;
        private final MissionStatus status;

        public static MissionItem from(
                ChildDashboardMissionRow row
        ) {
            return new MissionItem(
                    row.getMissionId(),
                    row.getTitle(),
                    row.getDescription(),
                    row.getRewardAmount(),
                    row.getStatus()
            );
        }
    }

    @Getter
    @RequiredArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @ApiModel(value = "ChildDashboardNotificationSummaryResponse")
    public static class NotificationSummary {
        private final long unreadCount;
    }
}
