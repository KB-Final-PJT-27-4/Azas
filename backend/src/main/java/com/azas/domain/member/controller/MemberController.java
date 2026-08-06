package com.azas.domain.member.controller;

import com.azas.domain.member.dto.MemberProfileResponse;
import com.azas.domain.member.dto.MemberProfileResult;
import com.azas.domain.member.service.MemberProfileService;
import com.azas.global.security.AccessTokenMemberResolver;
import com.azas.global.response.ApiErrorResponse;
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

@Api(tags = "회원")
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberProfileService memberProfileService;
    private final AccessTokenMemberResolver accessTokenMemberResolver;

    @ApiOperation(
            value = "내 회원 정보 조회",
            notes = "Access Token을 기준으로 현재 로그인 회원의 기본 정보와 "
                    + "연결된 소셜 계정 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "내 회원 정보 조회 성공",
                    response = MemberProfileResponse.class
            ),
            @ApiResponse(
                    code = 401,
                    message = "Access Token 누락·만료·유효하지 않음 또는 탈퇴 회원",
                    response = ApiErrorResponse.class
            )
    })
    @GetMapping("/me")
    public ResponseEntity<MemberProfileResponse> getMyProfile(
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

        MemberProfileResult result =
                memberProfileService.getMyProfile(
                        memberId
                );

        return ResponseEntity.ok(
                MemberProfileResponse.from(result)
        );
    }
}