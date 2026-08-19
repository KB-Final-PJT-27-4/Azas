package com.azas.domain.dashboard.service;

import com.azas.domain.dashboard.dto.ParentDashboardChecklistRow;
import com.azas.domain.dashboard.dto.ParentDashboardFlowRow;
import com.azas.domain.dashboard.dto.ParentDashboardResponse;
import com.azas.domain.dashboard.dto.ParentDashboardSummaryRow;
import com.azas.domain.dashboard.mapper.ParentDashboardMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ParentDashboardServiceTest {

    private static final Long MEMBER_ID = 7L;
    private static final Long CHILD_ID = 6L;

    @Mock
    private ParentDashboardMapper parentDashboardMapper;

    private ParentDashboardService parentDashboardService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        parentDashboardService =
                new ParentDashboardService(parentDashboardMapper);
    }

    @Test
    void 연결된_부모는_자녀_대시보드를_조회한다() {
        ParentDashboardSummaryRow summary = summary();

        ParentDashboardFlowRow august = flow(
                "2026-08",
                "9600000"
        );
        ParentDashboardFlowRow july = flow(
                "2026-07",
                "9250000"
        );

        ParentDashboardChecklistRow checklist =
                checklist(101L, "가족 금융 계획 점검하기", "PENDING");

        when(parentDashboardMapper.countActiveChild(CHILD_ID))
                .thenReturn(1);
        when(parentDashboardMapper.countActiveParentAccess(
                MEMBER_ID,
                CHILD_ID
        )).thenReturn(1);
        when(parentDashboardMapper.findDashboardSummary(
                MEMBER_ID,
                CHILD_ID
        )).thenReturn(summary);
        when(parentDashboardMapper.findSixMonthFlow(CHILD_ID))
                .thenReturn(Arrays.asList(august, july));
        when(parentDashboardMapper.findChecklistPreview(
                CHILD_ID,
                3
        )).thenReturn(List.of(checklist));

        ParentDashboardResponse response =
                parentDashboardService.getDashboard(
                        MEMBER_ID,
                        CHILD_ID
                );

        assertEquals(CHILD_ID, response.getChild().getChildId());
        assertEquals(
                new BigDecimal("9600000"),
                response.getAssetSummary().getTotalAssetAmount()
        );
        assertEquals(
                "2026-07",
                response.getAssetSummary()
                        .getSixMonthFlow()
                        .get(0)
                        .getReportMonth()
        );
        assertEquals(
                new BigDecimal("32.00"),
                response.getPrimaryGoal().getAchievementRate()
        );
        assertEquals(
                new BigDecimal("50.00"),
                response.getChecklistSummary().getAchievementRate()
        );
        assertEquals(
                3,
                response.getChecklistSummary().getRemainingCount()
        );
    }

    @Test
    void 연결되지_않은_회원은_대시보드를_조회할_수_없다() {
        when(parentDashboardMapper.countActiveChild(CHILD_ID))
                .thenReturn(1);
        when(parentDashboardMapper.countActiveParentAccess(
                MEMBER_ID,
                CHILD_ID
        )).thenReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> parentDashboardService.getDashboard(
                        MEMBER_ID,
                        CHILD_ID
                )
        );

        assertEquals(
                ErrorCode.CHILD_ACCESS_DENIED,
                exception.getErrorCode()
        );

        verify(
                parentDashboardMapper,
                never()
        ).findDashboardSummary(anyLong(), anyLong());
    }

    @Test
    void 존재하지_않는_자녀는_조회할_수_없다() {
        when(parentDashboardMapper.countActiveChild(CHILD_ID))
                .thenReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> parentDashboardService.getDashboard(
                        MEMBER_ID,
                        CHILD_ID
                )
        );

        assertEquals(
                ErrorCode.CHILD_NOT_FOUND,
                exception.getErrorCode()
        );
    }

    private ParentDashboardSummaryRow summary() {
        ParentDashboardSummaryRow row =
                new ParentDashboardSummaryRow();

        row.setChildId(CHILD_ID);
        row.setChildName("깨비");
        row.setTotalAssetAmount(new BigDecimal("9600000"));
        row.setTotalAssetChangeAmount(new BigDecimal("350000"));

        row.setPrimaryGoalId(31L);
        row.setPrimaryGoalTitle("대학자금");
        row.setPrimaryGoalSavedAmount(new BigDecimal("9600000"));
        row.setPrimaryGoalTargetAmount(new BigDecimal("30000000"));

        row.setActiveGoalCount(2);
        row.setNearestTimeCapsuleId(10L);
        row.setNearestTimeCapsuleTitle("깨비의 첫 타임캡슐");
        row.setNearestTimeCapsuleDDay(23);

        row.setChecklistTotalCount(6);
        row.setChecklistCompletedCount(3);
        row.setUnreadNotificationCount(2);

        return row;
    }

    private ParentDashboardFlowRow flow(
            String month,
            String amount
    ) {
        ParentDashboardFlowRow row =
                new ParentDashboardFlowRow();
        row.setReportMonth(month);
        row.setTotalAssetAmount(new BigDecimal(amount));
        return row;
    }

    private ParentDashboardChecklistRow checklist(
            Long id,
            String title,
            String status
    ) {
        ParentDashboardChecklistRow row =
                new ParentDashboardChecklistRow();
        row.setChecklistItemId(id);
        row.setTitle(title);
        row.setStatus(status);
        return row;
    }
}