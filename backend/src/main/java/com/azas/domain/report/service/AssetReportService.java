package com.azas.domain.report.service;

import com.azas.domain.report.dto.AssetReportAccountSnapshot;
import com.azas.domain.report.dto.AssetReportDetailResponse;
import com.azas.domain.report.dto.AssetReportDetailRow;
import com.azas.domain.report.dto.AssetReportGoalSnapshot;
import com.azas.domain.report.dto.AssetReportInsightSnapshot;
import com.azas.domain.report.dto.AssetReportListItemResponse;
import com.azas.domain.report.dto.AssetReportListQuery;
import com.azas.domain.report.dto.AssetReportListResponse;
import com.azas.domain.report.dto.AssetReportListRow;
import com.azas.domain.report.mapper.AssetReportMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetReportService {

    private static final int DEFAULT_SIZE = 12;

    private static final int MAX_SIZE = 12;

    private static final int MIN_YEAR = 1900;

    private static final int MAX_YEAR = 9999;

    private final AssetReportMapper assetReportMapper;

    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public AssetReportListResponse getAssetReports(
            Long memberId,
            Long childId,
            Integer year,
            String cursorValue,
            Integer sizeValue
    ) {
        validateChildId(childId);

        int pageSize = normalizeSize(sizeValue);

        YearRange yearRange = createYearRange(year);

        ReportCursor cursor = parseCursor(cursorValue);

        validateCursorYear(year, cursor);

        if (assetReportMapper.findActiveChildId(childId) == null) {
            throw new BusinessException(
                    ErrorCode.CHILD_NOT_FOUND
            );
        }

        if (assetReportMapper.countParentAccess(
                memberId,
                childId
        ) <= 0) {
            throw new BusinessException(
                    ErrorCode.PARENT_ACCESS_REQUIRED
            );
        }

        AssetReportListQuery query =
                new AssetReportListQuery(
                        childId,
                        yearRange == null
                                ? null
                                : yearRange.start,
                        yearRange == null
                                ? null
                                : yearRange.endExclusive,
                        cursor == null
                                ? null
                                : cursor.reportMonth,
                        cursor == null
                                ? null
                                : cursor.assetReportId,
                        pageSize + 1
                );

        List<AssetReportListRow> rows =
                assetReportMapper.findAssetReports(query);

        if (rows == null) {
            rows = Collections.emptyList();
        }

        boolean hasNext = rows.size() > pageSize;

        List<AssetReportListRow> pageRows =
                hasNext
                        ? new ArrayList<>(
                        rows.subList(0, pageSize)
                )
                        : new ArrayList<>(rows);

        List<AssetReportListItemResponse> items =
                pageRows.stream()
                        .map(AssetReportListItemResponse::from)
                        .toList();

        String nextCursor = null;

        if (hasNext && !pageRows.isEmpty()) {
            AssetReportListRow lastRow =
                    pageRows.get(pageRows.size() - 1);

            nextCursor = encodeCursor(
                    lastRow.getReportMonth(),
                    lastRow.getAssetReportId()
            );
        }

        return new AssetReportListResponse(
                items,
                nextCursor,
                hasNext
        );
    }

    private void validateChildId(Long childId) {
        if (childId == null || childId <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_QUERY_PARAMETER
            );
        }
    }

    private int normalizeSize(Integer sizeValue) {
        if (sizeValue == null) {
            return DEFAULT_SIZE;
        }

        if (sizeValue < 1 || sizeValue > MAX_SIZE) {
            throw new BusinessException(
                    ErrorCode.INVALID_QUERY_PARAMETER
            );
        }

        return sizeValue;
    }

    private YearRange createYearRange(Integer year) {
        if (year == null) {
            return null;
        }

        if (year < MIN_YEAR || year > MAX_YEAR) {
            throw new BusinessException(
                    ErrorCode.INVALID_QUERY_PARAMETER
            );
        }

        LocalDate start = LocalDate.of(year, 1, 1);

        return new YearRange(
                start,
                start.plusYears(1)
        );
    }

    private ReportCursor parseCursor(String cursorValue) {
        if (cursorValue == null || cursorValue.isBlank()) {
            return null;
        }

        try {
            String decoded = new String(
                    Base64.getUrlDecoder().decode(cursorValue),
                    StandardCharsets.UTF_8
            );

            String[] parts = decoded.split("\\|", -1);

            if (parts.length != 2) {
                throw new IllegalArgumentException();
            }

            LocalDate reportMonth =
                    LocalDate.parse(parts[0]);

            long assetReportId =
                    Long.parseLong(parts[1]);

            if (reportMonth.getDayOfMonth() != 1
                    || assetReportId <= 0) {
                throw new IllegalArgumentException();
            }

            return new ReportCursor(
                    reportMonth,
                    assetReportId
            );
        } catch (RuntimeException exception) {
            throw new BusinessException(
                    ErrorCode.INVALID_QUERY_PARAMETER
            );
        }
    }

    private void validateCursorYear(
            Integer year,
            ReportCursor cursor
    ) {
        if (year != null
                && cursor != null
                && cursor.reportMonth.getYear() != year) {
            throw new BusinessException(
                    ErrorCode.INVALID_QUERY_PARAMETER
            );
        }
    }

    private String encodeCursor(
            LocalDate reportMonth,
            Long assetReportId
    ) {
        String value =
                reportMonth + "|" + assetReportId;

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(
                        value.getBytes(StandardCharsets.UTF_8)
                );
    }

    private static final class YearRange {

        private final LocalDate start;

        private final LocalDate endExclusive;

        private YearRange(
                LocalDate start,
                LocalDate endExclusive
        ) {
            this.start = start;
            this.endExclusive = endExclusive;
        }
    }

    private static final class ReportCursor {

        private final LocalDate reportMonth;

        private final Long assetReportId;

        private ReportCursor(
                LocalDate reportMonth,
                Long assetReportId
        ) {
            this.reportMonth = reportMonth;
            this.assetReportId = assetReportId;
        }
    }

    @Transactional(readOnly = true)
    public AssetReportDetailResponse getAssetReportDetail(
            Long memberId,
            Long childId,
            Integer year,
            Integer month
    ) {
        validateDetailRequest(
                childId,
                year,
                month
        );

        if (assetReportMapper.findActiveChildId(childId) == null) {
            throw new BusinessException(
                    ErrorCode.CHILD_NOT_FOUND
            );
        }

        if (assetReportMapper.countParentAccess(
                memberId,
                childId
        ) <= 0) {
            throw new BusinessException(
                    ErrorCode.PARENT_ACCESS_REQUIRED
            );
        }

        LocalDate reportMonth =
                LocalDate.of(year, month, 1);

        AssetReportDetailRow row =
                assetReportMapper.findAssetReportDetail(
                        childId,
                        reportMonth
                );

        if (row == null) {
            throw new BusinessException(
                    ErrorCode.ASSET_REPORT_NOT_FOUND
            );
        }

        List<AssetReportGoalSnapshot> goalSnapshots =
                parseGoalSnapshots(
                        row.getSavingsGoalSummaryJson()
                );

        List<AssetReportInsightSnapshot> insightSnapshots =
                parseInsightSnapshots(
                        row.getInsightItemsJson()
                );

        BigDecimal monthlySavingTargetAmount =
                goalSnapshots.stream()
                        .map(
                                AssetReportGoalSnapshot
                                        ::getMonthlySavingTargetAmount
                        )
                        .map(this::zeroIfNull)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal monthlySavingAchievementRate =
                calculateAchievementRate(
                        row.getMonthlySavedAmount(),
                        monthlySavingTargetAmount
                );

        List<AssetReportDetailResponse.GoalSummary>
                goalSummary = goalSnapshots.stream()
                .map(this::toGoalSummary)
                .toList();

        List<AssetReportDetailResponse.InsightItem>
                insightItems = insightSnapshots.stream()
                .map(this::toInsightItem)
                .toList();

        LocalDate periodStart = row.getReportMonth();

        LocalDate periodEnd = periodStart
                .plusMonths(1)
                .minusDays(1);

        return new AssetReportDetailResponse(
                row.getAssetReportId(),
                row.getChildId(),
                row.getReportMonth().getYear(),
                row.getReportMonth().getMonthValue(),
                new AssetReportDetailResponse.Period(
                        periodStart,
                        periodEnd
                ),
                new AssetReportDetailResponse.Summary(
                        zeroIfNull(
                                row.getTotalAssetAmount()
                        ),
                        zeroIfNull(
                                row.getTotalAssetChangeAmount()
                        ),
                        zeroIfNull(
                                row.getTotalGoalTargetAmount()
                        ),
                        zeroIfNull(
                                row.getTotalGoalSavedAmount()
                        ),
                        zeroIfNull(
                                row.getGoalAchievementRate()
                        ),
                        zeroIfNull(
                                row.getMonthlySavedAmount()
                        ),
                        monthlySavingTargetAmount,
                        monthlySavingAchievementRate
                ),
                goalSummary,
                insightItems,
                row.getCreatedAt().toInstant(
                        ZoneOffset.UTC
                ),
                row.getUpdatedAt().toInstant(
                        ZoneOffset.UTC
                )
        );
    }

    private void validateDetailRequest(
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
    }

    private List<AssetReportGoalSnapshot>
    parseGoalSnapshots(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }

        try {
            List<AssetReportGoalSnapshot> snapshots =
                    objectMapper.readValue(
                            json,
                            new TypeReference<
                                    List<AssetReportGoalSnapshot>
                                    >() {
                            }
                    );

            return snapshots == null
                    ? List.of()
                    : snapshots;
        } catch (Exception exception) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR
            );
        }
    }

    private List<AssetReportInsightSnapshot>
    parseInsightSnapshots(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }

        try {
            List<AssetReportInsightSnapshot> snapshots =
                    objectMapper.readValue(
                            json,
                            new TypeReference<
                                    List<AssetReportInsightSnapshot>
                                    >() {
                            }
                    );

            return snapshots == null
                    ? List.of()
                    : snapshots;
        } catch (Exception exception) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR
            );
        }
    }

    private AssetReportDetailResponse.GoalSummary
    toGoalSummary(
            AssetReportGoalSnapshot snapshot
    ) {
        List<AssetReportAccountSnapshot> accountSnapshots =
                snapshot.getLinkedAccounts() == null
                        ? List.of()
                        : snapshot.getLinkedAccounts();

        List<AssetReportDetailResponse.LinkedAccount>
                linkedAccounts = accountSnapshots.stream()
                .map(this::toLinkedAccount)
                .toList();

        return new AssetReportDetailResponse.GoalSummary(
                snapshot.getFinancialGoalId(),
                snapshot.getTitle(),
                zeroIfNull(snapshot.getCurrentAmount()),
                zeroIfNull(snapshot.getTargetAmount()),
                zeroIfNull(snapshot.getAchievementRate()),
                zeroIfNull(snapshot.getMonthlySavedAmount()),
                linkedAccounts.size(),
                linkedAccounts
        );
    }

    private AssetReportDetailResponse.LinkedAccount
    toLinkedAccount(
            AssetReportAccountSnapshot snapshot
    ) {
        return new AssetReportDetailResponse.LinkedAccount(
                snapshot.getAccountId(),
                snapshot.getAccountName(),
                snapshot.getBankName(),
                snapshot.getAccountNumberMasked(),
                zeroIfNull(snapshot.getBalance())
        );
    }

    private AssetReportDetailResponse.InsightItem
    toInsightItem(
            AssetReportInsightSnapshot snapshot
    ) {
        return new AssetReportDetailResponse.InsightItem(
                snapshot.getType(),
                snapshot.getTitle(),
                snapshot.getDescription()
        );
    }

    private BigDecimal calculateAchievementRate(
            BigDecimal currentAmount,
            BigDecimal targetAmount
    ) {
        BigDecimal current = zeroIfNull(currentAmount);
        BigDecimal target = zeroIfNull(targetAmount);

        if (target.signum() <= 0) {
            return BigDecimal.ZERO.setScale(1);
        }

        return current
                .multiply(BigDecimal.valueOf(100))
                .divide(
                        target,
                        1,
                        RoundingMode.HALF_UP
                )
                .min(
                        BigDecimal.valueOf(100)
                                .setScale(1)
                );
    }

    private BigDecimal zeroIfNull(BigDecimal amount) {
        return amount == null
                ? BigDecimal.ZERO
                : amount;
    }
}
