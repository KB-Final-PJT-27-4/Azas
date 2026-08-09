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
            notes = "로그인한 부모가 연결한 본인 명의의 활성 계좌 목록을 조회합니다. "
                    + "잔액은 마지막 금융정보 동기화 기준이며 실시간 금융기관 조회를 수행하지 않습니다. "
                    + "계좌번호는 DB에 암호화하여 저장하고 본인 권한 검증 후 전체 값을 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "부모 계좌 목록 조회 성공. 연결 계좌가 없으면 빈 목록 반환",
                    response = ParentAccountListResponse.class
            ),
            @ApiResponse(
                    code = 401,
                    message = "Access Token 누락·만료·유효하지 않음 또는 탈퇴 회원",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 403,
                    message = "부모 회원 권한이 없음",
                    response = ApiErrorResponse.class
            )
    })
    @GetMapping
    public ResponseEntity<ParentAccountListResponse>
    getMyAccounts(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authorizationHeader
    ) {
        long memberId =
                accessTokenMemberResolver.resolveMemberId(
                        authorizationHeader
                );

        ParentAccountListResult result =
                parentAccountListService.getMyAccounts(
                        memberId
                );

        return ResponseEntity.ok(
                ParentAccountListResponse.from(result)
        );
    }
}
