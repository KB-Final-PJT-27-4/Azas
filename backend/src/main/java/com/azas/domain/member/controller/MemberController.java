package com.azas.domain.member.controller;

import com.azas.domain.member.dto.MemberProfileResponse;
import com.azas.domain.member.dto.MemberProfileResult;
import com.azas.domain.member.dto.MemberProfileUpdateRequest;
import com.azas.domain.member.service.MemberProfileService;
import com.azas.domain.member.service.MemberProfileUpdateService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "회원")
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberProfileService memberProfileService;
    private final MemberProfileUpdateService
            memberProfileUpdateService;
    private final AccessTokenMemberResolver
            accessTokenMemberResolver;

    @ApiOperation(
            value = "내 회원 정보 조회",
            notes = "Access Token을 기준으로 현재 로그인 회원의 "
                    + "기본 정보와 연결된 소셜 계정 목록을 조회합니다."
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
                memberProfileService.getMyProfile(memberId);

        return ResponseEntity.ok(
                MemberProfileResponse.from(result)
        );
    }

    @ApiOperation(
            value = "내 회원 정보 수정",
            notes = "생년월일, 프로필 이미지 URL, 인증 완료된 휴대폰 번호를 수정합니다. "
                    + "휴대폰 번호는 인증번호 확인 API에서 발급받은 토큰으로 변경합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "내 회원 정보 수정 성공",
                    response = MemberProfileResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "수정 항목 또는 휴대폰 인증 토큰이 올바르지 않음",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 401,
                    message = "Access Token 누락·만료·유효하지 않음 또는 탈퇴 회원",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 409,
                    message = "이미 다른 회원이 사용 중인 휴대폰 번호",
                    response = ApiErrorResponse.class
            )
    })
    @PatchMapping("/me")
    public ResponseEntity<MemberProfileResponse> updateMyProfile(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authorizationHeader,
            @RequestBody
            MemberProfileUpdateRequest request
    ) {
        long memberId =
                accessTokenMemberResolver.resolveMemberId(
                        authorizationHeader
                );

        memberProfileUpdateService.updateMyProfile(
                memberId,
                request.toCommand()
        );

        MemberProfileResult result =
                memberProfileService.getMyProfile(memberId);

        return ResponseEntity.ok(
                MemberProfileResponse.from(result)
        );
    }
}