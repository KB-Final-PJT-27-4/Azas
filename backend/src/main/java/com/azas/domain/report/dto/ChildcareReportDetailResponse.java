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

    private final Instant calculatedAt;

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
    }

    @Getter
    @RequiredArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class MonthlyFlowItem {

        private final int year;

        private final int month;

        private final BigDecimal expenseAmount;
    }
}
