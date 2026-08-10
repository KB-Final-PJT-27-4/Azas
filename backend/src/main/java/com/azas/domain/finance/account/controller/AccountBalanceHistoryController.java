package com.azas.domain.finance.account.controller;

import com.azas.domain.finance.account.dto.AccountBalanceHistoryResponse;
import com.azas.domain.finance.account.dto.AccountBalanceHistoryResult;
import com.azas.domain.finance.account.service.AccountBalanceHistoryService;
import com.azas.global.response.ApiErrorResponse;
import com.azas.global.security.AccessTokenMemberResolver;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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

@Api(tags = "계좌")
@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountBalanceHistoryController {

    private final AccountBalanceHistoryService accountBalanceHistoryService;
    private final AccessTokenMemberResolver accessTokenMemberResolver;

    @ApiOperation(
            value = "ACCOUNT-17 계좌 월별 잔액 변화 조회",
            notes = "최근 N개월(기본 6개월, 최대 12개월)의 월별 마지막 잔액과 "
                    + "직전 달 대비 순변화액을 조회합니다. 월 경계는 Asia/Seoul 기준이며, "
                    + "응답 시각은 UTC입니다. change_amount는 실제 저축액이 아니라 "
                    + "월말 잔액의 순변화액입니다. 스냅샷이 없는 월은 null로 반환하며 "
                    + "이전 잔액을 이월하지 않습니다. 이 API는 저장된 잔액 스냅샷만 "
                    + "조회하고 CODEF API를 직접 호출하지 않습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "계좌 월별 잔액 변화 조회 성공",
                    response = AccountBalanceHistoryResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "올바르지 않은 계좌 ID 또는 조회 개월 수",
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
                    code = 500,
                    message = "저장된 잔액 스냅샷이 올바르지 않음 또는 서버 오류",
                    response = ApiErrorResponse.class
            )
    })
    @GetMapping("/{account_id}/balance-history")
    public ResponseEntity<AccountBalanceHistoryResponse> getBalanceHistory(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader,
            @PathVariable("account_id")
            long accountId,
            @ApiParam(
                    value = "현재 월을 포함한 조회 개월 수(1~12)",
                    example = "6"
            )
            @RequestParam(value = "months", defaultValue = "6")
            int months
    ) {
        long memberId = accessTokenMemberResolver.resolveMemberId(
                authorizationHeader
        );

        AccountBalanceHistoryResult result = accountBalanceHistoryService
                .getBalanceHistory(memberId, accountId, months);

        return ResponseEntity.ok(
                AccountBalanceHistoryResponse.from(result)
        );
    }
}
