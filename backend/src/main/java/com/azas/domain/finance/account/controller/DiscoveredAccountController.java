package com.azas.domain.finance.account.controller;

import com.azas.domain.finance.account.dto.DiscoveredAccountListResponse;
import com.azas.domain.finance.account.service.DiscoveredAccountService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "계좌")
@RestController
@RequiredArgsConstructor
public class DiscoveredAccountController {

    private final DiscoveredAccountService discoveredAccountService;
    private final AccessTokenMemberResolver accessTokenMemberResolver;

    @ApiOperation(
            value = "ACCOUNT-2 연결 가능한 Mock 계좌 목록 조회",
            notes = "부모가 본인 또는 연결 자녀의 아직 연결하지 않은 Mock 계좌 후보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "조회 성공. 후보가 없으면 빈 배열", response = DiscoveredAccountListResponse.class),
            @ApiResponse(code = 400, message = "조회 조건 오류", response = ApiErrorResponse.class),
            @ApiResponse(code = 401, message = "Access Token 오류", response = ApiErrorResponse.class),
            @ApiResponse(code = 403, message = "부모 또는 자녀 접근 권한 없음", response = ApiErrorResponse.class),
            @ApiResponse(code = 404, message = "자녀 없음", response = ApiErrorResponse.class),
            @ApiResponse(code = 409, message = "부모 입출금계좌 온보딩 필요", response = ApiErrorResponse.class)
    })
    @GetMapping("/api/v1/accounts/discovered")
    public ResponseEntity<DiscoveredAccountListResponse> getDiscoveredAccounts(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader,
            @RequestParam("owner_type")
            String ownerType,
            @RequestParam(value = "child_id", required = false)
            Long childId
    ) {
        long memberId = accessTokenMemberResolver.resolveMemberId(
                authorizationHeader
        );
        return ResponseEntity.ok(
                DiscoveredAccountListResponse.from(
                        discoveredAccountService.getDiscoveredAccounts(
                                memberId,
                                ownerType,
                                childId
                        )
                )
        );
    }
}
