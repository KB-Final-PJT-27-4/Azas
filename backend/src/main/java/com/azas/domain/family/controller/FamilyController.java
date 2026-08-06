package com.azas.domain.family.controller;

import com.azas.domain.family.dto.AllowanceRequestResponse;
import com.azas.domain.family.dto.ChildMemberLinkResponse;
import com.azas.domain.family.dto.FamilyGuardianListResponse;
import com.azas.domain.family.service.FamilyService;
import com.azas.global.security.AccessTokenMemberResolver;
import com.azas.global.response.ApiErrorResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Api(tags = "자녀·가족")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FamilyController {

    private final FamilyService familyService;
    private final AccessTokenMemberResolver accessTokenMemberResolver;

    @ApiOperation(
            value = "함께 관리하는 보호자 목록 조회",
            notes = "특정 자녀를 함께 관리하는 부모/보호자 목록을 조회합니다. 자녀 회원 계정은 포함하지 않습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "가족 목록 조회 성공",
                    response = FamilyGuardianListResponse.class
            ),
            @ApiResponse(
                    code = 401,
                    message = "Access Token 누락 또는 오류",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 404,
                    message = "자녀 정보를 찾을 수 없음",
                    response = ApiErrorResponse.class
            )
    })
    @GetMapping("/children/{child_id}/family-members")
    public ResponseEntity<FamilyGuardianListResponse> getFamilyMembers(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authorizationHeader,
            @ApiParam(value = "자녀 ID", required = true, example = "1")
            @PathVariable("child_id")
            Long childId
    ) {
        long memberId = accessTokenMemberResolver.resolveMemberId(
                authorizationHeader
        );

        FamilyGuardianListResponse response = familyService.getFamilyMembers(
                memberId,
                childId
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/children/{child_id}/member-link")
    public ResponseEntity<ChildMemberLinkResponse> getChildMemberLink(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader,
            @ApiParam(value = "자녀 ID", required = true, example = "1")
            @PathVariable("child_id")
            Long childId
    ) {
        long memberId = accessTokenMemberResolver.resolveMemberId(authorizationHeader);

        ChildMemberLinkResponse response =
                familyService.getChildMemberLink(memberId, childId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/children/{child_id}/allowance-requests")
    public ResponseEntity<AllowanceRequestResponse> requestAllowance(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader,
            @ApiParam(value = "자녀 ID", required = true, example = "1")
            @PathVariable("child_id")
            Long childId
    ) {
        long memberId = accessTokenMemberResolver.resolveMemberId(authorizationHeader);

        AllowanceRequestResponse response =
                familyService.requestAllowance(memberId, childId);

        return ResponseEntity.ok(response);
    }
}