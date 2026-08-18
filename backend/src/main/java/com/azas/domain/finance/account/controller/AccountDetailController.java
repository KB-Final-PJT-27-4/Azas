package com.azas.domain.finance.account.controller;

import com.azas.domain.finance.account.dto.AccountDetailResponse;
import com.azas.domain.finance.account.dto.AccountDetailResult;
import com.azas.domain.finance.account.service.AccountDetailService;
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
public class AccountDetailController {

    private final AccountDetailService accountDetailService;
    private final AccessTokenMemberResolver accessTokenMemberResolver;

    @ApiOperation(
            value = "ACCOUNT-15 계좌 상세 조회",
            notes = "부모 명의 활성 Mock 계좌는 소유 부모 본인만 조회할 수 "
                    + "있습니다. 자녀 명의 활성 Mock 계좌는 해당 자녀와 연결된 "
                    + "부모 또는 자녀 본인이 조회할 수 있습니다. 계좌 상세 "
                    + "화면에 필요한 은행명, 계좌명, 복호화된 전체 계좌번호, "
                    + "예금주명, 상품 유형과 현재 잔액을 반환합니다. 최근 "
                    + "거래내역은 ACCOUNT-22를 별도로 호출합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "계좌 상세 조회 성공",
                    response = AccountDetailResponse.class
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
                    message = "계좌가 없거나 서비스 연결이 해제됨",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 500,
                    message = "계좌번호 복호화 실패 등 서버 내부 오류",
                    response = ApiErrorResponse.class
            )
    })
    @GetMapping("/{account_id}")
    public ResponseEntity<AccountDetailResponse> getAccountDetail(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader,
            @PathVariable("account_id")
            long accountId
    ) {
        long memberId = accessTokenMemberResolver.resolveMemberId(
                authorizationHeader
        );

        AccountDetailResult result = accountDetailService.getAccountDetail(
                memberId,
                accountId
        );

        return ResponseEntity.ok(AccountDetailResponse.from(result));
    }
}
