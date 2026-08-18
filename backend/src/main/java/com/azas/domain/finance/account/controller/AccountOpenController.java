package com.azas.domain.finance.account.controller;

import com.azas.domain.finance.account.dto.*;
import com.azas.domain.finance.account.service.AccountOpenService;
import com.azas.global.security.AccessTokenMemberResolver;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@Api(tags = "계좌")
@RestController
@RequiredArgsConstructor
public class AccountOpenController {
    private final AccountOpenService service;
    private final AccessTokenMemberResolver resolver;

    @ApiOperation(
            value = "ACCOUNT-4 KB 금융상품 기반 Mock 계좌 개설",
            notes = "실제 금융기관 호출 없이 KB 금융상품 정보로 Mock 계좌를 개설합니다. "
                    + "자녀 적금 목표는 계좌 개설 후 GOAL-2에서 별도로 생성하고 연결합니다."
    )
    @PostMapping("/api/v1/accounts/open")
    public ResponseEntity<AccountOpenResponse> open(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) AccountOpenRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                AccountOpenResponse.from(service.open(
                        resolver.resolveMemberId(authorization), request)));
    }
}
