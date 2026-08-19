package com.azas.domain.report.controller;

import com.azas.domain.report.dto.AssetReportListResponse;
import com.azas.domain.report.service.AssetReportService;
import com.azas.global.response.ApiErrorResponse;
import com.azas.global.security.AccessTokenMemberResolver;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "리포트")
@RestController
@RequestMapping("/api/v1/children")
@RequiredArgsConstructor
public class AssetReportController {

    private final AccessTokenMemberResolver memberResolver;

    private final AssetReportService assetReportService;

    @ApiOperation(
            value = "자산 리포트 월 목록 조회",
            notes = "연결된 부모가 자녀의 월별 자산 리포트를 "
                    + "최신 월순으로 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "자산 리포트 월 목록 조회 성공",
                    response = AssetReportListResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "잘못된 year, cursor 또는 size",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 401,
                    message = "Access Token 누락·만료·위조",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 403,
                    message = "해당 자녀의 부모 권한 없음",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 404,
                    message = "활성 자녀 없음",
                    response = ApiErrorResponse.class
            )
    })
    @GetMapping("/{child_id}/asset-reports")
    public ResponseEntity<AssetReportListResponse>
    getAssetReports(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorization,
            @PathVariable("child_id") Long childId,
            @RequestParam(
                    value = "year",
                    required = false
            ) Integer year,
            @RequestParam(
                    value = "cursor",
                    required = false
            ) String cursor,
            @RequestParam(
                    value = "size",
                    required = false
            ) Integer size
    ) {
        Long memberId =
                memberResolver.resolveMemberId(
                        authorization
                );

        return ResponseEntity.ok(
                assetReportService.getAssetReports(
                        memberId,
                        childId,
                        year,
                        cursor,
                        size
                )
        );
    }
}