package com.azas.domain.report.service;

import com.azas.domain.report.dto.ChildcareMonthlyExpenseRow;
import com.azas.domain.report.dto.ChildcareReportDetailResponse;
import com.azas.domain.report.mapper.ChildcareReportMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChildcareReportService {

    private static final int FLOW_MONTH_COUNT = 12;

    /**
     * 부모 연령을 수집하지 않는 현재 서비스 정책에 따라,
     * 30대 부모 가구 양육비 평균을 모든 비교에 고정 적용합니다.
     * KICCE 소비실태조사 2024의 2023년 영유아 가구 통계에서
     * 모·부 30~39세 양육비용을 단순 평균한 값입니다.
     */
    private static final BigDecimal SAME_AGE_MONTHLY_AVERAGE_AMOUNT =
            new BigDecimal("1407000");

    private static final String COMPARISON_LABEL =
            "30대 부모 가구 월평균 양육비";

    private static final String COMPARISON_SOURCE_NAME =
            "육아정책연구소(KICCE) 소비실태조사 2024";

    private static final String COMPARISON_SOURCE_URL =
            "https://repo.kicce.re.kr/bitstream/2019.oak/5822/2/GR2404.pdf";

    private static final int COMPARISON_SOURCE_YEAR = 2023;

    private static final String COMPARISON_CALCULATION_BASIS =
            "KICCE 소비실태조사 2024의 2023년 영유아 가구 통계에서 "
                    + "모 30~39세 양육비 1,443,000원과 "
                    + "부 30~39세 양육비 1,370,000원을 단순 평균한 "
                    + "1,406,500원을 1,407,000원으로 반올림한 고정 기준입니다.";

    private static final int MIN_YEAR = 1900;

    private static final int MAX_YEAR = 9999;

    private static final ZoneId SERVICE_ZONE =
            ZoneId.of("Asia/Seoul");

    private final ChildcareReportMapper childcareReportMapper;

    private final Clock clock;

    @Autowired
    public ChildcareReportService(
            ChildcareReportMapper childcareReportMapper
    ) {
        this(
                childcareReportMapper,
                Clock.systemUTC()
        );
    }

    ChildcareReportService(
            ChildcareReportMapper childcareReportMapper,
            Clock clock
    ) {
        this.childcareReportMapper =
                childcareReportMapper;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public ChildcareReportDetailResponse getReport(
            Long memberId,
            Long childId,
            Integer year,
            Integer month
    ) {
        YearMonth reportPeriod =
                validateAndCreatePeriod(
                        childId,
                        year,
                        month
                );

        validateNotFuturePeriod(reportPeriod);

        if (childcareReportMapper.findActiveChildId(
                childId
        ) == null) {
            throw new BusinessException(
                    ErrorCode.CHILD_NOT_FOUND
            );
        }

        if (childcareReportMapper.countParentAccess(
                memberId,
                childId
        ) <= 0) {
            throw new BusinessException(
                    ErrorCode.PARENT_ACCESS_REQUIRED
            );
        }

        YearMonth flowStartPeriod =
                reportPeriod.minusMonths(
                        FLOW_MONTH_COUNT - 1L
                );

        LocalDateTime startOccurredAt =
                toUtcStart(flowStartPeriod);

        LocalDateTime endOccurredAtExclusive =
                toUtcStart(
                        reportPeriod.plusMonths(1)
                );

        List<ChildcareMonthlyExpenseRow> rows =
                childcareReportMapper.findMonthlyExpenses(
                        childId,
                        startOccurredAt,
                        endOccurredAtExclusive
                );

        if (rows == null) {
            rows = Collections.emptyList();
        }

        Map<YearMonth, BigDecimal> amountByMonth =
                createAmountMap(
                        flowStartPeriod,
                        rows
                );

        BigDecimal currentMonthAmount =
                amountByMonth.get(reportPeriod);

        BigDecimal previousMonthAmount =
                amountByMonth.get(
                        reportPeriod.minusMonths(1)
                );

        BigDecimal changeAmount =
                currentMonthAmount.subtract(
                        previousMonthAmount
                );

        BigDecimal changeRate =
                calculateChangeRate(
                        currentMonthAmount,
                        previousMonthAmount
                );

        BigDecimal annualExpenseAmount =
                amountByMonth.values()
                        .stream()
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal sameAgeDifferenceAmount =
                currentMonthAmount.subtract(
                        SAME_AGE_MONTHLY_AVERAGE_AMOUNT
                );

        BigDecimal sameAgeDifferenceRate =
                calculateComparisonRate(
                        currentMonthAmount,
                        SAME_AGE_MONTHLY_AVERAGE_AMOUNT
                );

        List<ChildcareReportDetailResponse.MonthlyFlowItem>
                monthlyFlow = amountByMonth
                .entrySet()
                .stream()
                .map(entry ->
                        new ChildcareReportDetailResponse
                                .MonthlyFlowItem(
                                entry.getKey().getYear(),
                                entry.getKey().getMonthValue(),
                                entry.getValue(),
                                SAME_AGE_MONTHLY_AVERAGE_AMOUNT
                        )
                )
                .toList();

        LocalDate periodStart =
                reportPeriod.atDay(1);

        LocalDate periodEnd =
                reportPeriod.atEndOfMonth();

        Instant calculatedAt = clock.instant();

        return new ChildcareReportDetailResponse(
                childId,
                reportPeriod.getYear(),
                reportPeriod.getMonthValue(),
                new ChildcareReportDetailResponse.Period(
                        periodStart,
                        periodEnd
                ),
                new ChildcareReportDetailResponse.Summary(
                        currentMonthAmount,
                        previousMonthAmount,
                        changeAmount,
                        changeRate,
                        annualExpenseAmount,
                        SAME_AGE_MONTHLY_AVERAGE_AMOUNT,
                        sameAgeDifferenceAmount,
                        sameAgeDifferenceRate
                ),
                monthlyFlow,
                new ChildcareReportDetailResponse.ComparisonBenchmark(
                        COMPARISON_LABEL,
                        "30대",
                        SAME_AGE_MONTHLY_AVERAGE_AMOUNT,
                        COMPARISON_SOURCE_NAME,
                        COMPARISON_SOURCE_URL,
                        COMPARISON_SOURCE_YEAR,
                        COMPARISON_CALCULATION_BASIS
                ),
                calculatedAt,
                calculatedAt,
                calculatedAt
        );
    }

    private YearMonth validateAndCreatePeriod(
            Long childId,
            Integer year,
            Integer month
    ) {
        if (childId == null
                || childId <= 0
                || year == null
                || year < MIN_YEAR
                || year > MAX_YEAR
                || month == null
                || month < 1
                || month > 12) {
            throw new BusinessException(
                    ErrorCode.INVALID_QUERY_PARAMETER
            );
        }

        return YearMonth.of(year, month);
    }

    private void validateNotFuturePeriod(
            YearMonth reportPeriod
    ) {
        YearMonth currentPeriod =
                YearMonth.from(
                        ZonedDateTime.now(clock)
                                .withZoneSameInstant(
                                        SERVICE_ZONE
                                )
                );

        if (reportPeriod.isAfter(currentPeriod)) {
            throw new BusinessException(
                    ErrorCode.INVALID_QUERY_PARAMETER
            );
        }
    }

    private LocalDateTime toUtcStart(
            YearMonth period
    ) {
        return period
                .atDay(1)
                .atStartOfDay(SERVICE_ZONE)
                .withZoneSameInstant(ZoneOffset.UTC)
                .toLocalDateTime();
    }

    private Map<YearMonth, BigDecimal> createAmountMap(
            YearMonth startPeriod,
            List<ChildcareMonthlyExpenseRow> rows
    ) {
        Map<YearMonth, BigDecimal> amountByMonth =
                new LinkedHashMap<>();

        for (int index = 0;
             index < FLOW_MONTH_COUNT;
             index++) {
            amountByMonth.put(
                    startPeriod.plusMonths(index),
                    BigDecimal.ZERO
            );
        }

        for (ChildcareMonthlyExpenseRow row : rows) {
            if (row == null
                    || row.getReportMonth() == null) {
                throw new BusinessException(
                        ErrorCode.INTERNAL_SERVER_ERROR
                );
            }

            YearMonth period =
                    YearMonth.from(
                            row.getReportMonth()
                    );

            if (amountByMonth.containsKey(period)) {
                amountByMonth.put(
                        period,
                        zeroIfNull(
                                row.getExpenseAmount()
                        )
                );
            }
        }

        return amountByMonth;
    }

    private BigDecimal calculateChangeRate(
            BigDecimal currentAmount,
            BigDecimal previousAmount
    ) {
        if (previousAmount.signum() == 0) {
            if (currentAmount.signum() == 0) {
                return BigDecimal.ZERO.setScale(1);
            }

            // 0원에서 지출이 발생하면 증가율은 계산 불가
            return null;
        }

        return currentAmount
                .subtract(previousAmount)
                .multiply(BigDecimal.valueOf(100))
                .divide(
                        previousAmount,
                        1,
                        RoundingMode.HALF_UP
                );
    }

    private BigDecimal calculateComparisonRate(
            BigDecimal expenseAmount,
            BigDecimal benchmarkAmount
    ) {
        return expenseAmount.subtract(benchmarkAmount)
                .multiply(BigDecimal.valueOf(100))
                .divide(
                        benchmarkAmount,
                        1,
                        RoundingMode.HALF_UP
                );
    }

    private BigDecimal zeroIfNull(
            BigDecimal amount
    ) {
        return amount == null
                ? BigDecimal.ZERO
                : amount;
    }
}
