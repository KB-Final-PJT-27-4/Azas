package com.azas.domain.finance.account.controller;

import com.azas.domain.finance.account.service.AccountUnlinkService;
import com.azas.global.response.ApiErrorResponse;
import com.azas.global.security.AccessTokenMemberResolver;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "계좌")
@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountUnlinkController {

    private final AccountUnlinkService accountUnlinkService;
    private final AccessTokenMemberResolver accessTokenMemberResolver;

    @ApiOperation(
            value = "ACCOUNT-18 계좌 서비스 연결 해제",
            notes = "금융기관 계좌를 해지하거나 CODEF 금융 연결 전체를 철회하지 않고 "
                    + "선택한 계좌의 Azas 서비스 연결만 해제합니다. "
                    + "해당 금융 연결을 생성한 회원만 요청할 수 있으며, "
                    + "이미 연결 해제된 계좌에 다시 요청해도 성공으로 처리합니다. "
                    + "계좌·거래·잔액 이력과 기존 관리 설정은 삭제하지 않습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    code = 204,
                    message = "계좌 서비스 연결 해제 성공"
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
                    message = "해당 금융 연결을 생성한 회원이 아님",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 404,
                    message = "금융 계좌를 찾을 수 없음",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 500,
                    message = "서버 내부 오류",
                    response = ApiErrorResponse.class
            )
    })
    @DeleteMapping("/{account_id}")
    public ResponseEntity<Void> unlinkAccount(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader,
            @PathVariable("account_id")
            long accountId
    ) {
        long memberId = accessTokenMemberResolver.resolveMemberId(
                authorizationHeader
        );

        accountUnlinkService.unlinkAccount(memberId, accountId);

        return ResponseEntity.noContent().build();
    }
}
