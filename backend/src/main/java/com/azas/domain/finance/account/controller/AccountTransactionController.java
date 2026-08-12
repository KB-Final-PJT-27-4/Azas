package com.azas.domain.finance.account.controller;

import com.azas.domain.finance.account.dto.AccountTransactionListResponse;
import com.azas.domain.finance.account.dto.AccountTransactionListResult;
import com.azas.domain.finance.account.service.AccountTransactionService;
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
public class AccountTransactionController {

    private final AccountTransactionService accountTransactionService;
    private final AccessTokenMemberResolver accessTokenMemberResolver;

    @ApiOperation(
            value = "ACCOUNT-22 계좌 거래내역 조회",
            notes = "계좌별 거래내역을 거래 발생 시각과 거래 ID의 역순으로 "
                    + "커서 페이지네이션 조회합니다. 부모 명의 계좌는 연결한 "
                    + "부모 본인만, 자녀 명의 계좌는 연결된 부모 또는 자녀 "
                    + "본인이 조회할 수 있습니다. 거래 금액은 항상 양수이며 "
                    + "direction(CREDIT, DEBIT)으로 입출금을 구분합니다. "
                    + "응답에는 거래 목록과 이체 상세 화면에 필요한 입금·출금 "
                    + "계좌 정보가 포함됩니다. 거래가 없으면 빈 목록을 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "계좌 거래내역 조회 성공",
                    response = AccountTransactionListResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "올바르지 않은 계좌 ID, 커서 또는 조회 개수",
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
                    message = "계좌가 없거나 활성 연결 대상이 아님",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 500,
                    message = "계좌번호 복호화 실패, 잘못 저장된 거래 또는 서버 오류",
                    response = ApiErrorResponse.class
            )
    })
    @GetMapping("/{account_id}/transactions")
    public ResponseEntity<AccountTransactionListResponse> getTransactions(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader,
            @PathVariable("account_id")
            long accountId,
            @ApiParam(value = "이전 응답의 next_cursor")
            @RequestParam(value = "cursor", required = false)
            String cursor,
            @ApiParam(value = "페이지당 조회 개수(1~100, 기본 20)",
                    example = "20")
            @RequestParam(value = "size", required = false)
            Integer size
    ) {
        long memberId = accessTokenMemberResolver.resolveMemberId(
                authorizationHeader
        );

        AccountTransactionListResult result = accountTransactionService
                .getTransactions(memberId, accountId, cursor, size);

        return ResponseEntity.ok(
                AccountTransactionListResponse.from(result)
        );
    }
}
