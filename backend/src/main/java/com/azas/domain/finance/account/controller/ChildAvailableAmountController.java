package com.azas.domain.finance.account.controller;

import com.azas.domain.finance.account.dto.ChildAvailableAmountResponse;
import com.azas.domain.finance.account.dto.ChildAvailableAmountResult;
import com.azas.domain.finance.account.service.ChildAvailableAmountService;
import com.azas.global.response.ApiErrorResponse;
import com.azas.global.security.AccessTokenMemberResolver;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "계좌")
@RestController
@RequestMapping("/api/v1/children/me")
@RequiredArgsConstructor
public class ChildAvailableAmountController {

    private final ChildAvailableAmountService childAvailableAmountService;
    private final AccessTokenMemberResolver accessTokenMemberResolver;

    @ApiOperation(
            value = "ACCOUNT-19 자녀 본인 월간 사용 현황 조회",
            notes = "자녀 회원이 본인의 대표 입출금 계좌에 설정된 월간 사용 관리 기준액과 "
                    + "현재 달 출금 합계, 남은 참고 금액을 조회합니다. "
                    + "이 응답은 실제 금융기관의 결제·출금 가능 금액이나 거래 차단을 "
                    + "의미하지 않습니다. UNRESTRICTED 정책에서는 기준액·남은 금액·"
                    + "초과 여부를 null로 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "자녀 본인 월간 사용 현황 조회 성공",
                    response = ChildAvailableAmountResponse.class
            ),
            @ApiResponse(
                    code = 401,
                    message = "Access Token 누락·만료·위조 또는 탈퇴 회원",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 403,
                    message = "자녀 회원 계정이 아님",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 404,
                    message = "활성 자녀 프로필 또는 대표 입출금 계좌를 찾을 수 없음",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 409,
                    message = "대표 계좌의 사용 관리 정책이 설정되지 않음",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 500,
                    message = "저장된 사용 관리 데이터가 올바르지 않음",
                    response = ApiErrorResponse.class
            )
    })
    @GetMapping("/available-amount")
    public ResponseEntity<ChildAvailableAmountResponse>
    getCurrentMonthUsage(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authorizationHeader
    ) {
        long memberId = accessTokenMemberResolver.resolveMemberId(
                authorizationHeader
        );

        ChildAvailableAmountResult result =
                childAvailableAmountService
                        .getCurrentMonthUsage(memberId);

        return ResponseEntity.ok(
                ChildAvailableAmountResponse.from(result)
        );
    }
}
