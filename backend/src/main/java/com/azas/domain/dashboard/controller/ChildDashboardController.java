package com.azas.domain.dashboard.controller;

import com.azas.domain.dashboard.dto.ChildDashboardResponse;
import com.azas.domain.dashboard.service.ChildDashboardService;
import com.azas.global.response.ApiErrorResponse;
import com.azas.global.security.AccessTokenMemberResolver;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Api(tags = "대시보드")
@RestController
@RequestMapping("/api/v1/children/me")
@RequiredArgsConstructor
public class ChildDashboardController {

    private final AccessTokenMemberResolver accessTokenMemberResolver;
    private final ChildDashboardService childDashboardService;

    @ApiOperation(
            value = "자녀 본인 홈 대시보드 조회",
            notes = "자녀 본인의 월간 사용 현황, 용돈 요청, "
                    + "최근 거래, 미션 및 읽지 않은 알림 수를 조회합니다. "
                    + "대표 입출금 계좌가 없으면 spending_summary는 null입니다."
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "자녀 대시보드 조회 성공",
                    response = ChildDashboardResponse.class
            ),
            @ApiResponse(
                    code = 401,
                    message = "Access Token 누락·만료·위조",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 403,
                    message = "자녀 회원 계정이 아님",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 404,
                    message = "로그인 회원과 연결된 활성 자녀 프로필 없음",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 409,
                    message = "대표 계좌의 자녀 사용 관리 정책이 설정되지 않음",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 500,
                    message = "저장된 대시보드 집계 데이터가 올바르지 않음",
                    response = ApiErrorResponse.class
            )
    })
    @GetMapping("/dashboard")
    public ResponseEntity<ChildDashboardResponse> getDashboard(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authorization
    ) {
        long memberId =
                accessTokenMemberResolver.resolveMemberId(
                        authorization
                );

        return ResponseEntity.ok(
                childDashboardService.getDashboard(memberId)
        );
    }
}
