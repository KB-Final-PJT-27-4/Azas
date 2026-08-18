package com.azas.domain.finance.account.controller;

import com.azas.domain.finance.account.dto.AccountLinkRequest;
import com.azas.domain.finance.account.dto.AccountLinkResponse;
import com.azas.domain.finance.account.service.AccountLinkService;
import com.azas.global.security.AccessTokenMemberResolver;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Api(tags = "계좌")
@RestController
@RequiredArgsConstructor
public class AccountLinkController {
    private final AccountLinkService accountLinkService;
    private final AccessTokenMemberResolver accessTokenMemberResolver;

    @ApiOperation(value = "ACCOUNT-3 선택한 Mock 계좌 연결")
    @PostMapping("/api/v1/accounts/link")
    public ResponseEntity<AccountLinkResponse> link(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader,
            @RequestBody(required = false) AccountLinkRequest request
    ) {
        long memberId = accessTokenMemberResolver
                .resolveMemberId(authorizationHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                AccountLinkResponse.from(
                        accountLinkService.link(memberId, request)
                )
        );
    }
}
