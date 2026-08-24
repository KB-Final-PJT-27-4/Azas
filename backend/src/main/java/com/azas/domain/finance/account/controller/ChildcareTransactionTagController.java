package com.azas.domain.finance.account.controller;

import com.azas.domain.finance.account.dto.ChildcareTransactionTagRequest;
import com.azas.domain.finance.account.dto.ChildcareTransactionTagResponse;
import com.azas.domain.finance.account.service.ChildcareTransactionTagService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "계좌")
@RestController
@RequestMapping("/api/v1/account-transactions")
@RequiredArgsConstructor
public class ChildcareTransactionTagController {

    private final ChildcareTransactionTagService childcareTransactionTagService;
    private final AccessTokenMemberResolver accessTokenMemberResolver;

    @ApiOperation(
            value = "ACCOUNT-25 외부 출금 거래 양육비 포함 설정",
            notes = "부모 계좌의 외부 출금 거래만 양육비에 포함하거나 해제합니다. "
                    + "서비스 내부 계좌 간 이체와 적금 이체는 설정할 수 없습니다."
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "양육비 포함 설정 성공", response = ChildcareTransactionTagResponse.class),
            @ApiResponse(code = 400, message = "거래 ID 또는 자녀 ID가 올바르지 않음", response = ApiErrorResponse.class),
            @ApiResponse(code = 401, message = "Access Token 누락·만료·위조 또는 탈퇴 회원", response = ApiErrorResponse.class),
            @ApiResponse(code = 403, message = "부모 계좌 또는 대상 자녀 접근 권한 없음", response = ApiErrorResponse.class),
            @ApiResponse(code = 404, message = "거래내역 또는 자녀를 찾을 수 없음", response = ApiErrorResponse.class),
            @ApiResponse(code = 422, message = "부모 계좌의 외부 출금 거래가 아님", response = ApiErrorResponse.class)
    })
    @PatchMapping("/{account_transaction_id}/childcare")
    public ResponseEntity<ChildcareTransactionTagResponse> updateTag(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader,
            @PathVariable("account_transaction_id") long accountTransactionId,
            @RequestBody ChildcareTransactionTagRequest request
    ) {
        long memberId = accessTokenMemberResolver.resolveMemberId(authorizationHeader);
        return ResponseEntity.ok(childcareTransactionTagService.updateTag(
                memberId,
                accountTransactionId,
                request == null ? null : request.getChildId()
        ));
    }
}
