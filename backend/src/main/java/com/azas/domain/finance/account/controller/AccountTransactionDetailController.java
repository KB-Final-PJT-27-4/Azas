package com.azas.domain.finance.account.controller;

import com.azas.domain.finance.account.dto.AccountTransactionDetailResponse;
import com.azas.domain.finance.account.dto.AccountTransactionDetailResult;
import com.azas.domain.finance.account.service.AccountTransactionDetailService;
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
@RequestMapping("/api/v1/account-transactions")
@RequiredArgsConstructor
public class AccountTransactionDetailController {

    private final AccountTransactionDetailService transactionDetailService;
    private final AccessTokenMemberResolver accessTokenMemberResolver;

    @ApiOperation(
            value = "ACCOUNT-24 거래내역 상세 조회",
            notes = "거래 ID로 거래 금액, 메모, 입금처, 출금처, 거래 시각, "
                    + "거래 후 잔액을 조회합니다. 거래가 속한 계좌를 기준으로 "
                    + "부모 본인 또는 연결 부모·자녀 회원의 접근 권한을 검증합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "거래내역 상세 조회 성공",
                    response = AccountTransactionDetailResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "올바르지 않은 거래 ID",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 401,
                    message = "Access Token 누락·만료·위조 또는 탈퇴 회원",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 403,
                    message = "거래 원장 계좌 접근 권한 없음",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 404,
                    message = "거래내역을 찾을 수 없음",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 500,
                    message = "계좌번호 복호화 실패, 저장 데이터 불일치 또는 서버 오류",
                    response = ApiErrorResponse.class
            )
    })
    @GetMapping("/{account_transaction_id}")
    public ResponseEntity<AccountTransactionDetailResponse>
    getTransactionDetail(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader,
            @PathVariable("account_transaction_id")
            long accountTransactionId
    ) {
        long memberId = accessTokenMemberResolver.resolveMemberId(
                authorizationHeader
        );
        AccountTransactionDetailResult result = transactionDetailService
                .getTransactionDetail(
                        memberId,
                        accountTransactionId
                );

        return ResponseEntity.ok(
                AccountTransactionDetailResponse.from(result)
        );
    }
}
