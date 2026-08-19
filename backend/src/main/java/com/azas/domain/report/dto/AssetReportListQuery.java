package com.azas.domain.report.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@Getter
@RequiredArgsConstructor
public class AssetReportListQuery {

    private final Long childId;

    private final LocalDate yearStart;

    private final LocalDate yearEndExclusive;

    private final LocalDate cursorReportMonth;

    private final Long cursorAssetReportId;

    private final int limit;
}