package com.azas.domain.report.service;

import com.azas.domain.report.dto.AssetReportDetailResponse;
import com.azas.domain.report.dto.AssetReportDetailRow;
import com.azas.domain.report.mapper.AssetReportMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssetReportDetailServiceTest {

    private static final Long MEMBER_ID = 4L;

    private static final Long CHILD_ID = 6L;

    @Mock
    private AssetReportMapper assetReportMapper;

    @Mock
    private AssetReportSnapshotService snapshotService;

    private AssetReportService assetReportService;

    @BeforeEach
    void setUp() {
        assetReportService =
                new AssetReportService(
                        assetReportMapper,
                        new ObjectMapper(),
                        snapshotService,
                        Clock.fixed(
                                Instant.parse("2026-08-20T00:00:00Z"),
                                ZoneOffset.UTC
                        )
                );
    }

    @Test
    void 월간_자산_리포트_상세를_조회한다() {
        mockAccess();

        when(assetReportMapper.findAssetReportDetail(
                CHILD_ID,
                LocalDate.of(2026, 7, 1)
        )).thenReturn(detailRow());

        AssetReportDetailResponse response =
                assetReportService.getAssetReportDetail(
                        MEMBER_ID,
                        CHILD_ID,
                        2026,
                        7
                );

        assertEquals(
                10001L,
                response.getAssetReportId()
        );

        assertEquals(
                CHILD_ID,
                response.getChildId()
        );

        assertEquals(2026, response.getReportYear());
        assertEquals(7, response.getReportMonth());

        assertEquals(
                LocalDate.of(2026, 7, 1),
                response.getPeriod().getStartDate()
        );

        assertEquals(
                LocalDate.of(2026, 7, 31),
                response.getPeriod().getEndDate()
        );

        assertEquals(
                new BigDecimal("20750000"),
                response.getSummary()
                        .getTotalAssetAmount()
        );

        assertEquals(
                new BigDecimal("1250000"),
                response.getSummary()
                        .getMonthlySavedAmount()
        );

        assertEquals(
                new BigDecimal("5000000"),
                response.getSummary()
                        .getMonthlySavingTargetAmount()
        );

        assertEquals(
                new BigDecimal("25.0"),
                response.getSummary()
                        .getMonthlySavingAchievementRate()
        );

        assertEquals(
                1,
                response.getGoalSummary().size()
        );

        AssetReportDetailResponse.GoalSummary goal =
                response.getGoalSummary().get(0);

        assertEquals("대학자금", goal.getTitle());
        assertEquals(2, goal.getLinkedAccountCount());

        assertEquals(
                "952-****-**43",
                goal.getLinkedAccounts()
                        .get(0)
                        .getAccountNumberMasked()
        );

        assertEquals(
                1,
                response.getInsightItems().size()
        );

        assertEquals(
                "MONTHLY_SAVING_COMPARISON",
                response.getInsightItems()
                        .get(0)
                        .getType()
        );
    }

    @Test
    void 해당_월_리포트가_없으면_404를_반환한다() {
        mockAccess();

        when(assetReportMapper.findAssetReportDetail(
                CHILD_ID,
                LocalDate.of(2026, 7, 1)
        )).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> assetReportService.getAssetReportDetail(
                        MEMBER_ID,
                        CHILD_ID,
                        2026,
                        7
                )
        );

        assertEquals(
                ErrorCode.ASSET_REPORT_NOT_FOUND,
                exception.getErrorCode()
        );

        verifyNoInteractions(snapshotService);
    }

    @Test
    void 이번_달_리포트는_최신_잔액으로_다시_생성한_뒤_반환한다() {
        mockAccess();

        AssetReportDetailRow august = detailRow();
        august.setReportMonth(LocalDate.of(2026, 8, 1));

        when(assetReportMapper.findAssetReportDetail(
                CHILD_ID,
                LocalDate.of(2026, 8, 1)
        )).thenReturn(august);

        AssetReportDetailResponse response =
                assetReportService.getAssetReportDetail(
                        MEMBER_ID,
                        CHILD_ID,
                        2026,
                        8
                );

        assertEquals(8, response.getReportMonth());

        verify(snapshotService).generateForChild(
                CHILD_ID,
                java.time.YearMonth.of(2026, 8)
        );

        verify(assetReportMapper)
                .findAssetReportDetail(
                        CHILD_ID,
                        LocalDate.of(2026, 8, 1)
                );
    }

    @Test
    void 존재하지_않는_자녀는_404를_반환한다() {
        when(assetReportMapper.findActiveChildId(CHILD_ID))
                .thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> assetReportService.getAssetReportDetail(
                        MEMBER_ID,
                        CHILD_ID,
                        2026,
                        7
                )
        );

        assertEquals(
                ErrorCode.CHILD_NOT_FOUND,
                exception.getErrorCode()
        );

        verify(assetReportMapper, never())
                .findAssetReportDetail(
                        anyLong(),
                        any(LocalDate.class)
                );
    }

    @Test
    void 연결되지_않은_부모는_조회할_수_없다() {
        when(assetReportMapper.findActiveChildId(CHILD_ID))
                .thenReturn(CHILD_ID);

        when(assetReportMapper.countParentAccess(
                MEMBER_ID,
                CHILD_ID
        )).thenReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> assetReportService.getAssetReportDetail(
                        MEMBER_ID,
                        CHILD_ID,
                        2026,
                        7
                )
        );

        assertEquals(
                ErrorCode.PARENT_ACCESS_REQUIRED,
                exception.getErrorCode()
        );

        verify(assetReportMapper, never())
                .findAssetReportDetail(
                        anyLong(),
                        any(LocalDate.class)
                );
    }

    @Test
    void 월이_1부터_12_범위가_아니면_400을_반환한다() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> assetReportService.getAssetReportDetail(
                        MEMBER_ID,
                        CHILD_ID,
                        2026,
                        13
                )
        );

        assertEquals(
                ErrorCode.INVALID_QUERY_PARAMETER,
                exception.getErrorCode()
        );

        verifyNoInteractions(assetReportMapper);
    }

    @Test
    void 잘못된_목표_JSON이면_500을_반환한다() {
        mockAccess();

        AssetReportDetailRow row = detailRow();

        row.setSavingsGoalSummaryJson(
                "{invalid-json"
        );

        when(assetReportMapper.findAssetReportDetail(
                CHILD_ID,
                LocalDate.of(2026, 7, 1)
        )).thenReturn(row);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> assetReportService.getAssetReportDetail(
                        MEMBER_ID,
                        CHILD_ID,
                        2026,
                        7
                )
        );

        assertEquals(
                ErrorCode.INTERNAL_SERVER_ERROR,
                exception.getErrorCode()
        );
    }

    @Test
    void 기존_형식의_리포트_JSON도_상세_조회할_수_있다() {
        mockAccess();

        AssetReportDetailRow row = detailRow();

        row.setSavingsGoalSummaryJson("""
                [
                  {
                    "account_id": 3,
                    "goal_name": "대학자금 마련",
                    "current_amount": 14600000,
                    "target_amount": 30000000,
                    "achievement_rate": 48.7,
                    "monthly_change_amount": 150000
                  }
                ]
                """);

        row.setInsightItemsJson("""
                [
                  {
                    "type": "MONTH_COMPARISON",
                    "message": "지난달보다 90,000원을 더 저축했어요."
                  }
                ]
                """);

        when(assetReportMapper.findAssetReportDetail(
                CHILD_ID,
                LocalDate.of(2026, 7, 1)
        )).thenReturn(row);

        AssetReportDetailResponse response =
                assetReportService.getAssetReportDetail(
                        MEMBER_ID,
                        CHILD_ID,
                        2026,
                        7
                );

        assertEquals(
                "대학자금 마련",
                response.getGoalSummary().get(0).getTitle()
        );
        assertEquals(
                new BigDecimal("150000"),
                response.getGoalSummary().get(0)
                        .getMonthlySavedAmount()
        );
        assertTrue(
                response.getGoalSummary().get(0)
                        .getLinkedAccounts().isEmpty()
        );
        assertEquals(
                "지난달보다 90,000원을 더 저축했어요.",
                response.getInsightItems().get(0).getTitle()
        );
    }

    private void mockAccess() {
        when(assetReportMapper.findActiveChildId(CHILD_ID))
                .thenReturn(CHILD_ID);

        when(assetReportMapper.countParentAccess(
                MEMBER_ID,
                CHILD_ID
        )).thenReturn(1);
    }

    private AssetReportDetailRow detailRow() {
        AssetReportDetailRow row =
                new AssetReportDetailRow();

        row.setAssetReportId(10001L);
        row.setChildId(CHILD_ID);
        row.setReportMonth(
                LocalDate.of(2026, 7, 1)
        );
        row.setTotalAssetAmount(
                new BigDecimal("20750000")
        );
        row.setTotalAssetChangeAmount(
                new BigDecimal("350000")
        );
        row.setMonthlySavedAmount(
                new BigDecimal("1250000")
        );
        row.setTotalGoalTargetAmount(
                new BigDecimal("50000000")
        );
        row.setTotalGoalSavedAmount(
                new BigDecimal("20750000")
        );
        row.setGoalAchievementRate(
                new BigDecimal("41.5")
        );

        row.setSavingsGoalSummaryJson("""
                [
                  {
                    "financial_goal_id": 100,
                    "title": "대학자금",
                    "current_amount": 14600000,
                    "target_amount": 30000000,
                    "achievement_rate": 48.67,
                    "monthly_saved_amount": 1250000,
                    "monthly_saving_target_amount": 5000000,
                    "linked_accounts": [
                      {
                        "account_id": 3,
                        "account_name": "KB 아이사랑적금 1",
                        "bank_name": "KB국민은행",
                        "account_number_masked": "952-****-**43",
                        "balance": 9600000
                      },
                      {
                        "account_id": 4,
                        "account_name": "KB 아이사랑적금 2",
                        "bank_name": "KB국민은행",
                        "account_number_masked": "952-****-**57",
                        "balance": 5000000
                      }
                    ]
                  }
                ]
                """);

        row.setInsightItemsJson("""
                [
                  {
                    "type": "MONTHLY_SAVING_COMPARISON",
                    "title": "지난달보다 90,000원을 더 저축했어요.",
                    "description": "꾸준한 저축 흐름이 아주 좋아요."
                  }
                ]
                """);

        row.setCreatedAt(
                LocalDateTime.of(
                        2026,
                        8,
                        1,
                        0,
                        5
                )
        );

        row.setUpdatedAt(row.getCreatedAt());

        return row;
    }
}
