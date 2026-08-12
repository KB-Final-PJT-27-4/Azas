package com.azas.domain.finance.account.controller;

import com.azas.domain.finance.account.service.AccountPrimaryService;
import com.azas.global.response.ApiErrorResponse;
import com.azas.global.security.AccessTokenMemberResolver;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "계좌")
@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountPrimaryController {

    private final AccountPrimaryService accountPrimaryService;
    private final AccessTokenMemberResolver accessTokenMemberResolver;

    @ApiOperation(
            value = "ACCOUNT-23 대표 계좌 설정",
            notes = "로그인 회원이 접근할 수 있는 활성 계좌를 대표 계좌로 설정합니다. "
                    + "부모 명의 계좌는 금융 연결을 생성한 부모 본인만, "
                    + "자녀 명의 계좌는 해당 자녀와 연결된 부모 또는 자녀 본인이 "
                    + "설정할 수 있습니다. 같은 소유 범위의 기존 대표 계좌는 "
                    + "자동으로 해제되며, 이미 대표인 계좌에 다시 요청해도 "
                    + "성공으로 처리합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    code = 204,
                    message = "대표 계좌 설정 성공"
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
                    message = "계좌가 없거나 대표 계좌로 설정할 수 없는 상태",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 500,
                    message = "서버 내부 오류",
                    response = ApiErrorResponse.class
            )
    })
    @PatchMapping("/{account_id}/primary")
    public ResponseEntity<Void> setPrimaryAccount(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader,
            @PathVariable("account_id")
            long accountId
    ) {
        long memberId = accessTokenMemberResolver.resolveMemberId(
                authorizationHeader
        );

        accountPrimaryService.setPrimaryAccount(memberId, accountId);

        return ResponseEntity.noContent().build();
    }
}
