package com.azas.domain.dashboard.service;

import com.azas.domain.dashboard.dto.ChildDashboardAccountRow;
import com.azas.domain.dashboard.dto.ChildDashboardActivityRow;
import com.azas.domain.dashboard.dto.ChildDashboardChildRow;
import com.azas.domain.dashboard.dto.ChildDashboardMissionRow;
import com.azas.domain.dashboard.dto.ChildDashboardResponse;
import com.azas.domain.dashboard.mapper.ChildDashboardMapper;
import com.azas.domain.finance.account.entity.ChildUsageMode;
import com.azas.domain.member.entity.Member;
import com.azas.domain.member.entity.MemberType;
import com.azas.domain.member.mapper.MemberMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChildDashboardService {

    private static final int MISSION_PREVIEW_SIZE = 2;
    private static final ZoneId SERVICE_ZONE =
            ZoneId.of("Asia/Seoul");

    private final ChildDashboardMapper childDashboardMapper;
    private final MemberMapper memberMapper;
    private final Clock clock;

    @Autowired
    public ChildDashboardService(
            ChildDashboardMapper childDashboardMapper,
            MemberMapper memberMapper
    ) {
        this(
                childDashboardMapper,
                memberMapper,
                Clock.systemUTC()
        );
    }

    ChildDashboardService(
            ChildDashboardMapper childDashboardMapper,
            MemberMapper memberMapper,
            Clock clock
    ) {
        this.childDashboardMapper = childDashboardMapper;
        this.memberMapper = memberMapper;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public ChildDashboardResponse getDashboard(
            long memberId
    ) {
        validateChildMember(memberId);

        ChildDashboardChildRow child = childDashboardMapper
                .findActiveChildByMemberId(memberId);

        if (child == null) {
            throw new BusinessException(
                    ErrorCode.CHILD_NOT_FOUND
            );
        }

        MonthRange monthRange = currentMonthRange();

        ChildDashboardAccountRow account = childDashboardMapper
                .findPreferredAccountUsage(
                        child.getChildId(),
                        monthRange.startOccurredAt,
                        monthRange.endOccurredAtExclusive
                );

        ChildDashboardActivityRow activity = childDashboardMapper
                .findActivitySummary(
                        child.getChildId(),
                        account == null
                                ? null
                                : account.getAccountId(),
                        monthRange.startOccurredAt,
                        monthRange.endOccurredAtExclusive
                );

        if (activity == null) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR
            );
        }

        int activeMissionCount = childDashboardMapper
                .countActiveMissions(child.getChildId());

        List<ChildDashboardMissionRow> missionRows =
                childDashboardMapper.findMissionPreview(
                        child.getChildId(),
                        MISSION_PREVIEW_SIZE
                );

        long unreadNotificationCount = childDashboardMapper
                .countUnreadNotifications(memberId);

        return new ChildDashboardResponse(
                new ChildDashboardResponse.ChildInfo(
                        child.getChildId(),
                        child.getName(),
                        child.getProfileImageUrl()
                ),
                createSpendingSummary(
                        account,
                        monthRange.period
                ),
                new ChildDashboardResponse.ActivitySummary(
                        activity.getPendingAllowanceRequestCount(),
                        activity.getCurrentMonthTransactionCount()
                ),
                new ChildDashboardResponse.MissionSummary(
                        activeMissionCount,
                        missionRows.stream()
                                .map(
                                        ChildDashboardResponse.MissionItem
                                                ::from
                                )
                                .collect(Collectors.toList())
                ),
                new ChildDashboardResponse.NotificationSummary(
                        unreadNotificationCount
                )
        );
    }

    private ChildDashboardResponse.SpendingSummary
    createSpendingSummary(
            ChildDashboardAccountRow account,
            String period
    ) {
        if (account == null) {
            return null;
        }

        ChildUsageMode usageMode = account.getChildUsageMode();

        if (usageMode == null) {
            throw new BusinessException(
                    ErrorCode.CHILD_USAGE_POLICY_NOT_CONFIGURED
            );
        }

        BigDecimal spentAmount =
                requireNonNegative(
                        account.getCurrentMonthSpentAmount()
                );
        BigDecimal accountBalance =
                requireNonNegative(account.getAccountBalance());

        if (usageMode == ChildUsageMode.UNRESTRICTED) {
            return new ChildDashboardResponse.SpendingSummary(
                    account.getAccountId(),
                    usageMode,
                    accountBalance,
                    accountBalance,
                    false,
                    null,
                    spentAmount,
                    null,
                    null,
                    null,
                    period
            );
        }

        BigDecimal budgetAmount =
                requireNonNegative(
                        account.getMonthlyBudgetAmount()
                );
        BigDecimal remainingAmount = budgetAmount
                .subtract(spentAmount)
                .max(BigDecimal.ZERO);
        boolean budgetExceeded =
                spentAmount.compareTo(budgetAmount) > 0;

        return new ChildDashboardResponse.SpendingSummary(
                account.getAccountId(),
                usageMode,
                remainingAmount,
                accountBalance,
                true,
                budgetAmount,
                spentAmount,
                remainingAmount,
                calculateUsageRate(
                        spentAmount,
                        budgetAmount
                ),
                budgetExceeded,
                period
        );
    }

    private void validateChildMember(long memberId) {
        Member member = memberMapper.findById(memberId);

        if (member == null
                || member.getMemberType() != MemberType.CHILD) {
            throw new BusinessException(
                    ErrorCode.CHILD_MEMBER_ACCESS_REQUIRED
            );
        }
    }

    private BigDecimal requireNonNegative(BigDecimal amount) {
        if (amount == null || amount.signum() < 0) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR
            );
        }
        return amount;
    }

    private int calculateUsageRate(
            BigDecimal spentAmount,
            BigDecimal budgetAmount
    ) {
        if (budgetAmount.signum() == 0) {
            return spentAmount.signum() > 0 ? 100 : 0;
        }

        int rate = spentAmount
                .multiply(BigDecimal.valueOf(100))
                .divide(
                        budgetAmount,
                        0,
                        RoundingMode.HALF_UP
                )
                .intValue();

        return Math.min(Math.max(rate, 0), 100);
    }

    private MonthRange currentMonthRange() {
        ZonedDateTime now = ZonedDateTime.now(clock)
                .withZoneSameInstant(SERVICE_ZONE);
        YearMonth period = YearMonth.from(now);

        LocalDateTime startOccurredAt = period
                .atDay(1)
                .atStartOfDay(SERVICE_ZONE)
                .withZoneSameInstant(ZoneOffset.UTC)
                .toLocalDateTime();

        LocalDateTime endOccurredAtExclusive = period
                .plusMonths(1)
                .atDay(1)
                .atStartOfDay(SERVICE_ZONE)
                .withZoneSameInstant(ZoneOffset.UTC)
                .toLocalDateTime();

        return new MonthRange(
                period.toString(),
                startOccurredAt,
                endOccurredAtExclusive
        );
    }

    private static final class MonthRange {
        private final String period;
        private final LocalDateTime startOccurredAt;
        private final LocalDateTime endOccurredAtExclusive;

        private MonthRange(
                String period,
                LocalDateTime startOccurredAt,
                LocalDateTime endOccurredAtExclusive
        ) {
            this.period = period;
            this.startOccurredAt = startOccurredAt;
            this.endOccurredAtExclusive =
                    endOccurredAtExclusive;
        }
    }
}
