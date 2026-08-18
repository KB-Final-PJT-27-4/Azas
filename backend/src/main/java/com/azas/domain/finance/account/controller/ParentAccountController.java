package com.azas.domain.finance.account.controller;

import com.azas.domain.finance.account.dto.ParentAccountListResponse;
import com.azas.domain.finance.account.dto.ParentAccountListResult;
import com.azas.domain.finance.account.service.ParentAccountListService;
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
@RequestMapping("/api/v1/members/me/accounts")
@RequiredArgsConstructor
public class ParentAccountController {

    private final ParentAccountListService parentAccountListService;
    private final AccessTokenMemberResolver accessTokenMemberResolver;

    @ApiOperation(
            value = "ACCOUNT-13 부모 계좌 목록 조회",
            notes = "로그인한 부모 본인 명의의 활성 Mock 계좌를 조회합니다. "
                    + "부모 계좌 카드에 표시할 잔액 합계와 연결 계좌 수를 함께 "
                    + "반환합니다. 계좌별로 상세 이동과 화면 표시에 필요한 계좌 ID, "
                    + "계좌명, 전체 계좌번호, 상품 유형, 현재 잔액만 반환합니다. "
                    + "연결 계좌가 없으면 잔액 합계와 계좌 수가 0인 빈 목록을 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "부모 계좌 목록 조회 성공",
                    response = ParentAccountListResponse.class
            ),
            @ApiResponse(
                    code = 401,
                    message = "Access Token 누락·만료·위조 또는 탈퇴 회원",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 403,
                    message = "부모 회원 권한 없음",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 500,
                    message = "계좌번호 복호화 실패 또는 서버 오류",
                    response = ApiErrorResponse.class
            )
    })
    @GetMapping
    public ResponseEntity<ParentAccountListResponse> getMyAccounts(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader
    ) {
        long memberId = accessTokenMemberResolver.resolveMemberId(
                authorizationHeader
        );
        ParentAccountListResult result = parentAccountListService
                .getMyAccounts(memberId);
        return ResponseEntity.ok(ParentAccountListResponse.from(result));
    }
}
