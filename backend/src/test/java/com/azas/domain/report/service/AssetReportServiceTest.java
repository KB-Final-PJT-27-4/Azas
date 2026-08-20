package com.azas.domain.report.service;

import com.azas.domain.report.dto.AssetReportListQuery;
import com.azas.domain.report.dto.AssetReportListResponse;
import com.azas.domain.report.dto.AssetReportListRow;
import com.azas.domain.report.mapper.AssetReportMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssetReportServiceTest {

    private static final Long MEMBER_ID = 4L;

    private static final Long CHILD_ID = 6L;

    @Mock
    private AssetReportMapper assetReportMapper;

    private AssetReportService assetReportService;

    @BeforeEach
    void setUp() {
        assetReportService =
                new AssetReportService(
                        assetReportMapper,
                        new ObjectMapper()
                );
    }

    @Test
    void 자산_리포트를_최신_월순으로_조회한다() {
        mockAccess();

        AssetReportListRow july = row(
                10001L,
                LocalDate.of(2026, 7, 1),
                "20750000",
                "350000",
                "1250000",
                "41.5"
        );

        AssetReportListRow june = row(
                10000L,
                LocalDate.of(2026, 6, 1),
                "20400000",
                "260000",
                "1160000",
                "40.8"
        );

        AssetReportListRow may = row(
                9999L,
                LocalDate.of(2026, 5, 1),
                "20140000",
                "190000",
                "1100000",
                "40.28"
        );

        when(assetReportMapper.findAssetReports(any()))
                .thenReturn(List.of(july, june, may));

        AssetReportListResponse response =
                assetReportService.getAssetReports(
                        MEMBER_ID,
                        CHILD_ID,
                        2026,
                        null,
                        2
                );

        assertEquals(2, response.getItems().size());
        assertTrue(response.isHasNext());
        assertNotNull(response.getNextCursor());

        assertEquals(
                10001L,
                response.getItems().get(0).getAssetReportId()
        );

        assertEquals(
                "2026-07",
                response.getItems().get(0).getPeriod()
        );

        assertEquals(
                new BigDecimal("20750000"),
                response.getItems().get(0)
                        .getTotalAssetAmount()
        );

        String decodedCursor = new String(
                Base64.getUrlDecoder().decode(
                        response.getNextCursor()
                ),
                StandardCharsets.UTF_8
        );

        assertEquals(
                "2026-06-01|10000",
                decodedCursor
        );

        ArgumentCaptor<AssetReportListQuery> captor =
                ArgumentCaptor.forClass(
                        AssetReportListQuery.class
                );

        verify(assetReportMapper).findAssetReports(
                captor.capture()
        );

        AssetReportListQuery query = captor.getValue();

        assertEquals(CHILD_ID, query.getChildId());
        assertEquals(
                LocalDate.of(2026, 1, 1),
                query.getYearStart()
        );
        assertEquals(
                LocalDate.of(2027, 1, 1),
                query.getYearEndExclusive()
        );

        // size + 1로 조회해서 다음 페이지 존재 여부를 확인한다.
        assertEquals(3, query.getLimit());
    }

    @Test
    void 리포트가_없으면_빈_목록을_반환한다() {
        mockAccess();

        when(assetReportMapper.findAssetReports(any()))
                .thenReturn(Collections.emptyList());

        AssetReportListResponse response =
                assetReportService.getAssetReports(
                        MEMBER_ID,
                        CHILD_ID,
                        null,
                        null,
                        null
                );

        assertTrue(response.getItems().isEmpty());
        assertFalse(response.isHasNext());
        assertNull(response.getNextCursor());
    }

    @Test
    void 존재하지_않는_자녀는_404_예외를_발생시킨다() {
        when(assetReportMapper.findActiveChildId(CHILD_ID))
                .thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> assetReportService.getAssetReports(
                        MEMBER_ID,
                        CHILD_ID,
                        null,
                        null,
                        null
                )
        );

        assertEquals(
                ErrorCode.CHILD_NOT_FOUND,
                exception.getErrorCode()
        );

        verify(assetReportMapper, never())
                .findAssetReports(any());
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
                () -> assetReportService.getAssetReports(
                        MEMBER_ID,
                        CHILD_ID,
                        null,
                        null,
                        null
                )
        );

        assertEquals(
                ErrorCode.PARENT_ACCESS_REQUIRED,
                exception.getErrorCode()
        );

        verify(assetReportMapper, never())
                .findAssetReports(any());
    }

    @Test
    void 잘못된_size는_400_예외를_발생시킨다() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> assetReportService.getAssetReports(
                        MEMBER_ID,
                        CHILD_ID,
                        null,
                        null,
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
    void 잘못된_cursor는_400_예외를_발생시킨다() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> assetReportService.getAssetReports(
                        MEMBER_ID,
                        CHILD_ID,
                        null,
                        "not-valid-cursor",
                        12
                )
        );

        assertEquals(
                ErrorCode.INVALID_QUERY_PARAMETER,
                exception.getErrorCode()
        );

        verifyNoInteractions(assetReportMapper);
    }

    @Test
    void 연도와_cursor의_연도가_다르면_400을_반환한다() {
        String cursor = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(
                        "2025-12-01|99"
                                .getBytes(StandardCharsets.UTF_8)
                );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> assetReportService.getAssetReports(
                        MEMBER_ID,
                        CHILD_ID,
                        2026,
                        cursor,
                        12
                )
        );

        assertEquals(
                ErrorCode.INVALID_QUERY_PARAMETER,
                exception.getErrorCode()
        );

        verifyNoInteractions(assetReportMapper);
    }

    private void mockAccess() {
        when(assetReportMapper.findActiveChildId(CHILD_ID))
                .thenReturn(CHILD_ID);

        when(assetReportMapper.countParentAccess(
                MEMBER_ID,
                CHILD_ID
        )).thenReturn(1);
    }

    private AssetReportListRow row(
            Long assetReportId,
            LocalDate reportMonth,
            String totalAssetAmount,
            String totalAssetChangeAmount,
            String monthlySavedAmount,
            String goalAchievementRate
    ) {
        AssetReportListRow row =
                new AssetReportListRow();

        row.setAssetReportId(assetReportId);
        row.setReportMonth(reportMonth);
        row.setTotalAssetAmount(
                new BigDecimal(totalAssetAmount)
        );
        row.setTotalAssetChangeAmount(
                new BigDecimal(totalAssetChangeAmount)
        );
        row.setMonthlySavedAmount(
                new BigDecimal(monthlySavedAmount)
        );
        row.setGoalAchievementRate(
                new BigDecimal(goalAchievementRate)
        );
        row.setCreatedAt(
                LocalDateTime.of(
                        2026,
                        reportMonth.getMonthValue(),
                        1,
                        0,
                        5
                )
        );
        row.setUpdatedAt(row.getCreatedAt());

        return row;
    }
}
