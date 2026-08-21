package com.azas.domain.report.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@ApiModel(description = "자산 리포트 월 목록 응답")
@Getter
@RequiredArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AssetReportListResponse {

    private final List<AssetReportListItemResponse> items;

    private final String nextCursor;

    private final boolean hasNext;
}