package com.azas.domain.finance.account.controller;

import com.azas.domain.finance.account.dto.AccountBalanceResponse;
import com.azas.domain.finance.account.dto.AccountBalanceResult;
import com.azas.domain.finance.account.service.AccountBalanceService;
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

@Api(tags = "계좌")
@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountBalanceController {

    private final AccountBalanceService accountBalanceService;
    private final AccessTokenMemberResolver accessTokenMemberResolver;

    @ApiOperation(
            value = "ACCOUNT-16 계좌 최신 잔액 조회",
            notes = "마지막 금융정보 동기화로 저장된 계좌 잔액과 기준 시각을 조회합니다. "
                    + "이 API에서는 CODEF 등 외부 금융기관 API를 직접 호출하지 않습니다. "
                    + "부모 명의 계좌는 금융 연결 회원 본인, 자녀 명의 계좌는 "
                    + "해당 자녀와 연결된 부모 또는 자녀 본인만 조회할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "계좌 최신 잔액 조회 성공",
                    response = AccountBalanceResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "올바르지 않은 계좌 ID",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 401,
                    message = "Access Token 누락·만료·위조 또는 탈퇴 회원",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 403,
                    message = "해당 금융 계좌에 접근할 권한이 없음",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 404,
                    message = "계좌가 없거나 유효한 금융 연결 대상이 아님",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 409,
                    message = "아직 동기화된 계좌 잔액이 없음",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 500,
                    message = "서버 내부 오류",
                    response = ApiErrorResponse.class
            )
    })
    @GetMapping("/{account_id}/balance")
    public ResponseEntity<AccountBalanceResponse> getLatestBalance(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader,
            @PathVariable("account_id")
            long accountId
    ) {
        long memberId = accessTokenMemberResolver.resolveMemberId(
                authorizationHeader
        );

        AccountBalanceResult result = accountBalanceService
                .getLatestBalance(memberId, accountId);

        return ResponseEntity.ok(AccountBalanceResponse.from(result));
    }
}
