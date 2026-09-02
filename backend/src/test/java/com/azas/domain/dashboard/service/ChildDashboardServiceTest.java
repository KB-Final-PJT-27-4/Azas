package com.azas.domain.dashboard.service;

import com.azas.domain.dashboard.dto.ChildDashboardAccountRow;
import com.azas.domain.dashboard.dto.ChildDashboardActivityRow;
import com.azas.domain.dashboard.dto.ChildDashboardChildRow;
import com.azas.domain.dashboard.dto.ChildDashboardMissionRow;
import com.azas.domain.dashboard.dto.ChildDashboardResponse;
import com.azas.domain.dashboard.mapper.ChildDashboardMapper;
import com.azas.domain.finance.account.entity.ChildUsageMode;
import com.azas.domain.member.entity.Member;
import com.azas.domain.member.mapper.MemberMapper;
import com.azas.domain.mission.entity.MissionStatus;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChildDashboardServiceTest {

    private static final long MEMBER_ID = 9L;
    private static final long CHILD_ID = 6L;
    private static final long ACCOUNT_ID = 40002L;
    private static final LocalDateTime MONTH_START_UTC =
            LocalDateTime.of(2026, 7, 31, 15, 0);
    private static final LocalDateTime NEXT_MONTH_START_UTC =
            LocalDateTime.of(2026, 8, 31, 15, 0);

    @Mock
    private ChildDashboardMapper childDashboardMapper;

    @Mock
    private MemberMapper memberMapper;

    private ChildDashboardService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(
                Instant.parse("2026-08-11T10:00:00Z"),
                ZoneOffset.UTC
        );

        service = new ChildDashboardService(
                childDashboardMapper,
                memberMapper,
                clock
        );
    }

    @Test
    void returnsCoManagedChildDashboard() {
        mockBaseDashboard(
                account(
                        ChildUsageMode.CO_MANAGED,
                        "96000.00",
                        "20000.00",
                        "14000.00"
                )
        );

        ChildDashboardResponse response =
                service.getDashboard(MEMBER_ID);

        assertEquals(CHILD_ID, response.getChild().getChildId());
        assertEquals("깨비", response.getChild().getName());
        assertEquals(
                new BigDecimal("6000.00"),
                response.getSpendingSummary()
                        .getDisplayAvailableAmount()
        );
        assertEquals(
                new BigDecimal("96000.00"),
                response.getSpendingSummary().getAccountBalance()
        );
        assertTrue(
                response.getSpendingSummary()
                        .isAccountBalanceHidden()
        );
        assertEquals(
                70,
                response.getSpendingSummary().getUsageRate()
        );
        assertFalse(
                response.getSpendingSummary().getBudgetExceeded()
        );
        assertEquals(
                "2026-08",
                response.getSpendingSummary().getPeriod()
        );
        assertEquals(
                1,
                response.getActivitySummary()
                        .getPendingAllowanceRequestCount()
        );
        assertEquals(
                2,
                response.getActivitySummary()
                        .getCurrentMonthTransactionCount()
        );
        assertEquals(1, response.getMissionSummary().getActiveCount());
        assertEquals(2, response.getMissionSummary().getItems().size());
        assertEquals(
                MissionStatus.APPROVED,
                response.getMissionSummary().getItems().get(0).getStatus()
        );
        assertEquals(
                2L,
                response.getNotificationSummary().getUnreadCount()
        );

        verify(childDashboardMapper).findPreferredAccountUsage(
                CHILD_ID,
                MONTH_START_UTC,
                NEXT_MONTH_START_UTC
        );
    }

    @Test
    void returnsDashboardWithoutSpendingWhenAccountIsMissing() {
        mockBaseDashboard(null);

        ChildDashboardResponse response =
                service.getDashboard(MEMBER_ID);

        assertNull(response.getSpendingSummary());
        assertEquals(
                0,
                response.getActivitySummary()
                        .getCurrentMonthTransactionCount()
        );
        verify(childDashboardMapper).findActivitySummary(
                CHILD_ID,
                null,
                MONTH_START_UTC,
                NEXT_MONTH_START_UTC
        );
    }

    @Test
    void unrestrictedModeDisplaysAccountBalance() {
        mockBaseDashboard(
                account(
                        ChildUsageMode.UNRESTRICTED,
                        "96000.00",
                        null,
                        "14000.00"
                )
        );

        ChildDashboardResponse response =
                service.getDashboard(MEMBER_ID);

        assertEquals(
                new BigDecimal("96000.00"),
                response.getSpendingSummary()
                        .getDisplayAvailableAmount()
        );
        assertEquals(
                new BigDecimal("96000.00"),
                response.getSpendingSummary().getAccountBalance()
        );
        assertFalse(
                response.getSpendingSummary()
                        .isAccountBalanceHidden()
        );
        assertNull(
                response.getSpendingSummary()
                        .getMonthlyBudgetAmount()
        );
        assertNull(response.getSpendingSummary().getUsageRate());
        assertNull(response.getSpendingSummary().getBudgetExceeded());
    }

    @Test
    void capsUsageRateAndReturnsZeroWhenBudgetIsExceeded() {
        mockBaseDashboard(
                account(
                        ChildUsageMode.CO_MANAGED,
                        "96000.00",
                        "20000.00",
                        "25000.00"
                )
        );

        ChildDashboardResponse response =
                service.getDashboard(MEMBER_ID);

        assertEquals(
                BigDecimal.ZERO,
                response.getSpendingSummary()
                        .getRemainingMonthlyBudgetAmount()
        );
        assertEquals(100, response.getSpendingSummary().getUsageRate());
        assertTrue(response.getSpendingSummary().getBudgetExceeded());
    }

    @Test
    void rejectsParentMember() {
        when(memberMapper.findById(MEMBER_ID)).thenReturn(
                Member.createParent(
                        "parent@example.com",
                        "부모",
                        null
                )
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.getDashboard(MEMBER_ID)
        );

        assertEquals(
                ErrorCode.CHILD_MEMBER_ACCESS_REQUIRED,
                exception.getErrorCode()
        );
        verify(childDashboardMapper, never())
                .findActiveChildByMemberId(MEMBER_ID);
    }

    @Test
    void returnsNotFoundWhenChildProfileIsMissing() {
        when(memberMapper.findById(MEMBER_ID))
                .thenReturn(childMember());
        when(childDashboardMapper.findActiveChildByMemberId(
                MEMBER_ID
        )).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.getDashboard(MEMBER_ID)
        );

        assertEquals(
                ErrorCode.CHILD_NOT_FOUND,
                exception.getErrorCode()
        );
        verify(childDashboardMapper, never())
                .findPreferredAccountUsage(
                        any(Long.class),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class)
                );
    }

    @Test
    void returnsConflictWhenUsagePolicyIsMissing() {
        ChildDashboardAccountRow account = account(
                null,
                "96000.00",
                null,
                "14000.00"
        );
        mockBaseDashboard(account);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.getDashboard(MEMBER_ID)
        );

        assertEquals(
                ErrorCode.CHILD_USAGE_POLICY_NOT_CONFIGURED,
                exception.getErrorCode()
        );
    }

    private void mockBaseDashboard(
            ChildDashboardAccountRow account
    ) {
        when(memberMapper.findById(MEMBER_ID))
                .thenReturn(childMember());
        when(childDashboardMapper.findActiveChildByMemberId(
                MEMBER_ID
        )).thenReturn(child());
        when(childDashboardMapper.findPreferredAccountUsage(
                CHILD_ID,
                MONTH_START_UTC,
                NEXT_MONTH_START_UTC
        )).thenReturn(account);
        when(childDashboardMapper.findActivitySummary(
                CHILD_ID,
                account == null ? null : ACCOUNT_ID,
                MONTH_START_UTC,
                NEXT_MONTH_START_UTC
        )).thenReturn(activity(account == null ? 0 : 2));
        when(childDashboardMapper.countActiveMissions(CHILD_ID))
                .thenReturn(1);
        when(childDashboardMapper.findMissionPreview(CHILD_ID, 2))
                .thenReturn(List.of(
                        mission(
                                1L,
                                "용돈기입장 작성하기",
                                MissionStatus.APPROVED
                        ),
                        mission(
                                2L,
                                "소비 계획 지키기",
                                MissionStatus.ASSIGNED
                        )
                ));
        when(childDashboardMapper.countUnreadNotifications(MEMBER_ID))
                .thenReturn(2L);
    }

    private Member childMember() {
        return Member.createChild(
                "child@example.com",
                "깨비",
                null
        );
    }

    private ChildDashboardChildRow child() {
        ChildDashboardChildRow row = new ChildDashboardChildRow();
        row.setChildId(CHILD_ID);
        row.setName("깨비");
        return row;
    }

    private ChildDashboardAccountRow account(
            ChildUsageMode usageMode,
            String balance,
            String budget,
            String spent
    ) {
        ChildDashboardAccountRow row =
                new ChildDashboardAccountRow();
        row.setAccountId(ACCOUNT_ID);
        row.setChildUsageMode(usageMode);
        row.setAccountBalance(
                balance == null ? null : new BigDecimal(balance)
        );
        row.setMonthlyBudgetAmount(
                budget == null ? null : new BigDecimal(budget)
        );
        row.setCurrentMonthSpentAmount(
                spent == null ? null : new BigDecimal(spent)
        );
        return row;
    }

    private ChildDashboardActivityRow activity(
            int transactionCount
    ) {
        ChildDashboardActivityRow row =
                new ChildDashboardActivityRow();
        row.setPendingAllowanceRequestCount(1);
        row.setCurrentMonthTransactionCount(transactionCount);
        return row;
    }

    private ChildDashboardMissionRow mission(
            Long missionId,
            String title,
            MissionStatus status
    ) {
        ChildDashboardMissionRow row =
                new ChildDashboardMissionRow();
        row.setMissionId(missionId);
        row.setTitle(title);
        row.setDescription("이번 주 미션 내용");
        row.setRewardAmount(new BigDecimal("1000.00"));
        row.setStatus(status);
        return row;
    }
}
