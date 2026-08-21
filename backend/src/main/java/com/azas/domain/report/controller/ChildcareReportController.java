package com.azas.domain.report.controller;

import com.azas.domain.report.dto.ChildcareReportDetailResponse;
import com.azas.domain.report.service.ChildcareReportService;
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
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "리포트")
@RestController
@RequestMapping("/api/v1/children")
@RequiredArgsConstructor
public class ChildcareReportController {

    private final AccessTokenMemberResolver memberResolver;

    private final ChildcareReportService childcareReportService;

    @ApiOperation(
            value = "월간 양육비 리포트 상세 조회",
            notes = "자녀 입출금계좌의 외부 출금 거래를 기준으로 "
                    + "최근 12개월 월별 지출과 연간 합계를 조회합니다. "
                    + "자녀 계좌 사이의 내부 이체는 제외합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "월간 양육비 리포트 조회 성공",
                    response =
                            ChildcareReportDetailResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "잘못된 자녀 ID, 연도 또는 월",
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
    @GetMapping(
            "/{child_id}/childcare-reports/{year}/{month}"
    )
    public ResponseEntity<ChildcareReportDetailResponse>
    getReport(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorization,
            @PathVariable("child_id") Long childId,
            @PathVariable("year") Integer year,
            @PathVariable("month") Integer month
    ) {
        Long memberId =
                memberResolver.resolveMemberId(
                        authorization
                );

        return ResponseEntity.ok(
                childcareReportService.getReport(
                        memberId,
                        childId,
                        year,
                        month
                )
        );
    }
}