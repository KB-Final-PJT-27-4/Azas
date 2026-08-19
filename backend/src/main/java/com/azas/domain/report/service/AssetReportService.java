package com.azas.domain.report.service;

import com.azas.domain.report.dto.AssetReportListItemResponse;
import com.azas.domain.report.dto.AssetReportListQuery;
import com.azas.domain.report.dto.AssetReportListResponse;
import com.azas.domain.report.dto.AssetReportListRow;
import com.azas.domain.report.mapper.AssetReportMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
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
}