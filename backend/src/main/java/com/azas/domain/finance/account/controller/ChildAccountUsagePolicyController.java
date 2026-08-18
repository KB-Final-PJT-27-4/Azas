package com.azas.domain.finance.account.controller;

import com.azas.domain.finance.account.dto.ChildAccountUsagePolicyRequest;
import com.azas.domain.finance.account.dto.ChildAccountUsagePolicyResponse;
import com.azas.domain.finance.account.entity.FinancialAccountUsagePolicy;
import com.azas.domain.finance.account.service.ChildAccountUsagePolicyService;
import com.azas.global.response.ApiErrorResponse;
import com.azas.global.security.AccessTokenMemberResolver;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Api(tags = "계좌")
@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class ChildAccountUsagePolicyController {

    private final ChildAccountUsagePolicyService
            childAccountUsagePolicyService;

    private final AccessTokenMemberResolver
            accessTokenMemberResolver;

    @ApiOperation(
            value = "ACCOUNT-11 입출금 계좌 자녀 사용 관리 정책 설정",
            notes = "부모가 자녀 명의의 활성 입출금 계좌에 "
                    + "월간 사용 관리 기준을 설정합니다. "
                    + "이 정책은 실제 금융기관의 결제나 이체를 차단하지 않습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "자녀 계좌 사용 관리 정책 설정 성공",
                    response =
                            ChildAccountUsagePolicyResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "사용 관리 모드 또는 관리 기준 금액이 올바르지 않음",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 401,
                    message = "Access Token 누락·만료·유효하지 않음 또는 탈퇴 회원",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 403,
                    message = "해당 자녀에 대한 부모 권한이 없음",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 404,
                    message = "금융 계좌를 찾을 수 없음",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 422,
                    message = "자녀 명의의 활성 입출금 계좌가 아님",
                    response = ApiErrorResponse.class
            )
    })
    @PatchMapping(
            "/{account_id}/child-usage-policy"
    )
    public ResponseEntity<ChildAccountUsagePolicyResponse>
    updateUsagePolicy(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authorizationHeader,
            @PathVariable("account_id")
            long accountId,
            @Valid
            @RequestBody
            ChildAccountUsagePolicyRequest request
    ) {
        long memberId =
                accessTokenMemberResolver.resolveMemberId(
                        authorizationHeader
                );

        FinancialAccountUsagePolicy policy =
                childAccountUsagePolicyService
                        .updateUsagePolicy(
                                memberId,
                                accountId,
                                request.getChildUsageMode(),
                                request.getChildMonthlyBudgetAmount()
                        );

        return ResponseEntity.ok(
                ChildAccountUsagePolicyResponse.from(policy)
        );
    }

    @ApiOperation(
            value = "ACCOUNT-12 입출금 계좌 자녀 사용 관리 정책 조회",
            notes = "해당 자녀의 부모 또는 계좌에 연결된 자녀 회원이 "
                    + "현재 사용 관리 정책을 조회합니다. "
                    + "정책은 실제 금융기관 거래를 차단하지 않습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "자녀 계좌 사용 관리 정책 조회 성공",
                    response =
                            ChildAccountUsagePolicyResponse.class
            ),
            @ApiResponse(
                    code = 401,
                    message = "Access Token 누락·만료·유효하지 않음 또는 탈퇴 회원",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 403,
                    message = "해당 자녀 정보에 접근할 권한이 없음",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 404,
                    message = "금융 계좌를 찾을 수 없음",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 422,
                    message = "자녀 명의의 활성 입출금 계좌가 아님",
                    response = ApiErrorResponse.class
            )
    })
    @GetMapping(
            "/{account_id}/child-usage-policy"
    )
    public ResponseEntity<ChildAccountUsagePolicyResponse>
    getUsagePolicy(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authorizationHeader,
            @PathVariable("account_id")
            long accountId
    ) {
        long memberId =
                accessTokenMemberResolver.resolveMemberId(
                        authorizationHeader
                );

        FinancialAccountUsagePolicy policy =
                childAccountUsagePolicyService
                        .getUsagePolicy(
                                memberId,
                                accountId
                        );

        return ResponseEntity.ok(
                ChildAccountUsagePolicyResponse.from(policy)
        );
    }
}