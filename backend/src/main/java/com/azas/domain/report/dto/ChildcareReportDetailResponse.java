package com.azas.domain.report.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@ApiModel(description = "월간 양육비 리포트 상세 응답")
@Getter
@RequiredArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ChildcareReportDetailResponse {

    @ApiModelProperty(example = "6")
    private final Long childId;

    @ApiModelProperty(example = "2026")
    private final int reportYear;

    @ApiModelProperty(example = "7")
    private final int reportMonth;

    private final Period period;

    private final Summary summary;

    private final List<MonthlyFlowItem> monthlyFlow;

    private final ComparisonBenchmark comparisonBenchmark;

    /**
     * 자산 리포트의 created_at, updated_at과 공통으로 사용하는 조회 시각입니다.
     * 양육비 리포트는 거래내역을 실시간 집계하므로 둘 다 같은 값입니다.
     */
    private final Instant createdAt;

    private final Instant updatedAt;

    /**
     * 기존 클라이언트 호환용 필드입니다. created_at과 같은 값입니다.
     */
    private final Instant calculatedAt;

    public ChildcareReportDetailResponse(
            Long childId,
            int reportYear,
            int reportMonth,
            Period period,
            Summary summary,
            List<MonthlyFlowItem> monthlyFlow,
            Instant calculatedAt
    ) {
        this(
                childId,
                reportYear,
                reportMonth,
                period,
                summary,
                monthlyFlow,
                new ComparisonBenchmark(
                        "30대 부모 가구 월평균 양육비",
                        "30대",
                        new BigDecimal("1407000"),
                        "",
                        "",
                        0,
                        ""
                ),
                calculatedAt,
                calculatedAt,
                calculatedAt
        );
    }

    @Getter
    @RequiredArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @ApiModel(value = "ChildcareReportPeriodResponse")
    public static class Period {

        private final LocalDate startDate;

        private final LocalDate endDate;
    }

    @Getter
    @RequiredArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @ApiModel(value = "ChildcareReportSummaryResponse")
    public static class Summary {

        private final BigDecimal totalExpenseAmount;

        private final BigDecimal previousMonthExpenseAmount;

        private final BigDecimal previousMonthChangeAmount;

        /**
         * 전월 지출이 0이고 이번 달 지출이 있는 경우에는
         * 백분율을 계산할 수 없으므로 null입니다.
         */
        private final BigDecimal previousMonthChangeRate;

        private final BigDecimal annualExpenseAmount;

        private final BigDecimal sameAgeMonthlyAverageAmount;

        private final BigDecimal sameAgeDifferenceAmount;

        private final BigDecimal sameAgeDifferenceRate;

        public Summary(
                BigDecimal totalExpenseAmount,
                BigDecimal previousMonthExpenseAmount,
                BigDecimal previousMonthChangeAmount,
                BigDecimal previousMonthChangeRate,
                BigDecimal annualExpenseAmount
        ) {
            this(
                    totalExpenseAmount,
                    previousMonthExpenseAmount,
                    previousMonthChangeAmount,
                    previousMonthChangeRate,
                    annualExpenseAmount,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO
            );
        }
    }

    @Getter
    @RequiredArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class MonthlyFlowItem {

        private final int year;

        private final int month;

        private final BigDecimal expenseAmount;

        private final BigDecimal sameAgeAverageAmount;

        public MonthlyFlowItem(
                int year,
                int month,
                BigDecimal expenseAmount
        ) {
            this(year, month, expenseAmount, BigDecimal.ZERO);
        }
    }

    @Getter
    @RequiredArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ComparisonBenchmark {

        private final String label;

        private final String ageGroup;

        private final BigDecimal monthlyAverageAmount;

        private final String sourceName;

        private final String sourceUrl;

        private final int sourceYear;

        private final String calculationBasis;
    }
}
