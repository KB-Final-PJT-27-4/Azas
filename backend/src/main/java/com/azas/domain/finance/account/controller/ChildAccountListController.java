package com.azas.domain.finance.account.controller;

import com.azas.domain.finance.account.dto.ChildAccountListResponse;
import com.azas.domain.finance.account.dto.ChildAccountListResult;
import com.azas.domain.finance.account.service.ChildAccountListService;
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
@RequestMapping("/api/v1/children/{child_id}/accounts")
@RequiredArgsConstructor
public class ChildAccountListController {

    private final ChildAccountListService childAccountListService;
    private final AccessTokenMemberResolver accessTokenMemberResolver;

    @ApiOperation(
            value = "ACCOUNT-14 자녀 계좌 목록 조회",
            notes = "해당 자녀와 연결된 부모 또는 자녀 본인이 자녀 명의의 "
                    + "활성 Mock 계좌 목록을 조회합니다. 자녀 계좌 카드에 표시할 "
                    + "잔액 합계와 연결 계좌 수를 함께 반환합니다. 계좌별로 상세 "
                    + "이동과 화면 표시에 필요한 계좌 ID, 계좌명, 전체 계좌번호, "
                    + "상품 유형, 현재 잔액만 반환합니다. 연결 계좌가 없으면 잔액 "
                    + "합계와 계좌 수가 0인 빈 목록을 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "자녀 계좌 목록 조회 성공",
                    response = ChildAccountListResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "올바르지 않은 자녀 ID",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 401,
                    message = "Access Token 누락·만료·위조 또는 탈퇴 회원",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 403,
                    message = "해당 자녀에 접근할 권한 없음",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 404,
                    message = "자녀가 없거나 삭제 상태",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 500,
                    message = "계좌번호 복호화 실패 또는 서버 오류",
                    response = ApiErrorResponse.class
            )
    })
    @GetMapping
    public ResponseEntity<ChildAccountListResponse> getChildAccounts(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader,
            @PathVariable("child_id") long childId
    ) {
        long memberId = accessTokenMemberResolver.resolveMemberId(
                authorizationHeader
        );
        ChildAccountListResult result = childAccountListService
                .getChildAccounts(memberId, childId);
        return ResponseEntity.ok(ChildAccountListResponse.from(result));
    }
}
