package com.azas.domain.report.service;

import com.azas.domain.report.dto.ChildcareMonthlyExpenseRow;
import com.azas.domain.report.dto.ChildcareReportDetailResponse;
import com.azas.domain.report.mapper.ChildcareReportMapper;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChildcareReportServiceTest {

    private static final Long MEMBER_ID = 4L;

    private static final Long CHILD_ID = 6L;

    private static final Clock FIXED_CLOCK =
            Clock.fixed(
                    Instant.parse(
                            "2026-08-20T01:30:00Z"
                    ),
                    ZoneOffset.UTC
            );

    @Mock
    private ChildcareReportMapper childcareReportMapper;

    private ChildcareReportService childcareReportService;

    @BeforeEach
    void setUp() {
        childcareReportService =
                new ChildcareReportService(
                        childcareReportMapper,
                        FIXED_CLOCK
                );
    }

    @Test
    void 최근_12개월_양육비_리포트를_조회한다() {
        mockAccess();

        when(childcareReportMapper.findMonthlyExpenses(
                eq(CHILD_ID),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(
                List.of(
                        row("2026-06-01", "1740000"),
                        row("2026-07-01", "1860000")
                )
        );

        ChildcareReportDetailResponse response =
                childcareReportService.getReport(
                        MEMBER_ID,
                        CHILD_ID,
                        2026,
                        7
                );

        assertEquals(CHILD_ID, response.getChildId());
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
                new BigDecimal("1860000"),
                response.getSummary()
                        .getTotalExpenseAmount()
        );

        assertEquals(
                new BigDecimal("1740000"),
                response.getSummary()
                        .getPreviousMonthExpenseAmount()
        );

        assertEquals(
                new BigDecimal("120000"),
                response.getSummary()
                        .getPreviousMonthChangeAmount()
        );

        assertEquals(
                new BigDecimal("6.9"),
                response.getSummary()
                        .getPreviousMonthChangeRate()
        );

        assertEquals(
                new BigDecimal("3600000"),
                response.getSummary()
                        .getAnnualExpenseAmount()
        );

        assertEquals(
                new BigDecimal("1407000"),
                response.getSummary()
                        .getSameAgeMonthlyAverageAmount()
        );

        assertEquals(
                new BigDecimal("453000"),
                response.getSummary()
                        .getSameAgeDifferenceAmount()
        );

        assertEquals(
                new BigDecimal("32.2"),
                response.getSummary()
                        .getSameAgeDifferenceRate()
        );

        assertEquals(
                "30대 부모 가구 월평균 양육비",
                response.getComparisonBenchmark().getLabel()
        );

        assertEquals(
                "30대",
                response.getComparisonBenchmark().getAgeGroup()
        );

        assertEquals(
                2023,
                response.getComparisonBenchmark().getSourceYear()
        );

        assertEquals(12, response.getMonthlyFlow().size());

        assertEquals(
                2025,
                response.getMonthlyFlow().get(0).getYear()
        );

        assertEquals(
                8,
                response.getMonthlyFlow().get(0).getMonth()
        );

        assertEquals(
                BigDecimal.ZERO,
                response.getMonthlyFlow()
                        .get(0)
                        .getExpenseAmount()
        );

        assertEquals(
                new BigDecimal("1860000"),
                response.getMonthlyFlow()
                        .get(11)
                        .getExpenseAmount()
        );

        assertEquals(
                new BigDecimal("1407000"),
                response.getMonthlyFlow()
                        .get(0)
                        .getSameAgeAverageAmount()
        );

        verify(childcareReportMapper)
                .findMonthlyExpenses(
                        CHILD_ID,
                        LocalDateTime.of(
                                2025,
                                7,
                                31,
                                15,
                                0
                        ),
                        LocalDateTime.of(
                                2026,
                                7,
                                31,
                                15,
                                0
                        )
                );
    }

    @Test
    void 거래가_없으면_금액이_0인_리포트를_반환한다() {
        mockAccess();

        when(childcareReportMapper.findMonthlyExpenses(
                eq(CHILD_ID),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(Collections.emptyList());

        ChildcareReportDetailResponse response =
                childcareReportService.getReport(
                        MEMBER_ID,
                        CHILD_ID,
                        2026,
                        7
                );

        assertEquals(
                BigDecimal.ZERO,
                response.getSummary()
                        .getTotalExpenseAmount()
        );

        assertEquals(
                BigDecimal.ZERO,
                response.getSummary()
                        .getAnnualExpenseAmount()
        );

        assertEquals(
                new BigDecimal("0.0"),
                response.getSummary()
                        .getPreviousMonthChangeRate()
        );

        assertEquals(12, response.getMonthlyFlow().size());
    }

    @Test
    void 전월이_0원이고_이번달에_지출이_있으면_증가율은_null이다() {
        mockAccess();

        when(childcareReportMapper.findMonthlyExpenses(
                eq(CHILD_ID),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(
                List.of(
                        row("2026-07-01", "100000")
                )
        );

        ChildcareReportDetailResponse response =
                childcareReportService.getReport(
                        MEMBER_ID,
                        CHILD_ID,
                        2026,
                        7
                );

        assertNull(
                response.getSummary()
                        .getPreviousMonthChangeRate()
        );
    }

    @Test
    void 미래_월은_조회할_수_없다() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> childcareReportService.getReport(
                        MEMBER_ID,
                        CHILD_ID,
                        2026,
                        9
                )
        );

        assertEquals(
                ErrorCode.INVALID_QUERY_PARAMETER,
                exception.getErrorCode()
        );

        verifyNoInteractions(childcareReportMapper);
    }

    @Test
    void 잘못된_월은_400을_반환한다() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> childcareReportService.getReport(
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

        verifyNoInteractions(childcareReportMapper);
    }

    @Test
    void 존재하지_않는_자녀는_404를_반환한다() {
        when(childcareReportMapper.findActiveChildId(
                CHILD_ID
        )).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> childcareReportService.getReport(
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
    }

    @Test
    void 연결되지_않은_부모는_조회할_수_없다() {
        when(childcareReportMapper.findActiveChildId(
                CHILD_ID
        )).thenReturn(CHILD_ID);

        when(childcareReportMapper.countParentAccess(
                MEMBER_ID,
                CHILD_ID
        )).thenReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> childcareReportService.getReport(
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

        verify(childcareReportMapper, never())
                .findMonthlyExpenses(
                        anyLong(),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class)
                );
    }

    private void mockAccess() {
        when(childcareReportMapper.findActiveChildId(
                CHILD_ID
        )).thenReturn(CHILD_ID);

        when(childcareReportMapper.countParentAccess(
                MEMBER_ID,
                CHILD_ID
        )).thenReturn(1);

    }

    private ChildcareMonthlyExpenseRow row(
            String reportMonth,
            String expenseAmount
    ) {
        ChildcareMonthlyExpenseRow row =
                new ChildcareMonthlyExpenseRow();

        row.setReportMonth(
                LocalDate.parse(reportMonth)
        );

        row.setExpenseAmount(
                new BigDecimal(expenseAmount)
        );

        return row;
    }
}
