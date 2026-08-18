package com.azas.domain.auth.controller;

import com.azas.domain.auth.dto.*;
import com.azas.domain.auth.service.ChildInviteOAuthService;
import com.azas.domain.auth.service.OAuthLoginService;
import com.azas.domain.auth.service.ParentInviteOAuthService;
import com.azas.global.response.ApiErrorResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Api(tags = "인증")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class OAuthController {

    private final OAuthLoginService oauthLoginService;
    private final ChildInviteOAuthService childInviteOAuthService;
    private final ParentInviteOAuthService parentInviteOAuthService;

    @ApiOperation(
            value = "소셜 로그인/회원가입",
            notes = "Google 또는 Kakao의 인가 코드로 로그인하며, 최초 로그인 시 회원을 생성합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "로그인 또는 회원가입 성공",
                    response = OAuthLoginResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "요청값 오류 또는 지원하지 않는 제공자",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 401,
                    message = "만료되었거나 유효하지 않은 인가 코드",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 502,
                    message = "소셜 제공자 통신 실패",
                    response = ApiErrorResponse.class
            )
    })
    @PostMapping("/oauth/{provider}")
    public ResponseEntity<OAuthLoginResponse> login(
            @ApiParam(
                    value = "소셜 로그인 제공자",
                    allowableValues = "google,kakao",
                    required = true,
                    example = "kakao"
            )
            @PathVariable("provider")
            String provider,
            @Valid
            @RequestBody
            OAuthLoginRequest request
    ) {
        OAuthLoginResult result =
                oauthLoginService.login(
                        provider,
                        request
                );

        return ResponseEntity.ok(
                OAuthLoginResponse.from(result)
        );
    }

    @ApiOperation(
            value = "자녀 초대코드 기반 소셜 회원가입/로그인",
            notes = "자녀 초대 토큰과 소셜 인가 코드를 검증하고 "
                    + "CHILD 회원을 생성하거나 로그인합니다. "
                    + "이 단계에서는 자녀 정보와 회원 계정을 연결하지 않으며, "
                    + "가족 초대 상태를 PENDING으로 유지합니다. "
                    + "로그인 성공 후 별도의 가족 초대 수락 API를 호출해야 합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "로그인 및 수락 대기 초대 정보 조회 성공",
                    response = ChildInviteOAuthResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "요청값 오류, 지원하지 않는 제공자 또는 사용할 수 없는 자녀 초대",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 401,
                    message = "유효하지 않은 인가 코드 또는 탈퇴 회원",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 409,
                    message = "초대 유형과 기존 회원 유형 불일치",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 502,
                    message = "소셜 제공자 통신 실패",
                    response = ApiErrorResponse.class
            )
    })
    @PostMapping("/oauth/{provider}/child-invite")
    public ResponseEntity<ChildInviteOAuthResponse>
    loginWithChildInvite(
            @ApiParam(
                    value = "소셜 로그인 제공자",
                    allowableValues = "google,kakao",
                    required = true,
                    example = "kakao"
            )
            @PathVariable("provider")
            String provider,
            @Valid
            @RequestBody
            ChildInviteOAuthRequest request
    ) {
        ChildInviteOAuthResult result =
                childInviteOAuthService.login(
                        provider,
                        request.getAuthorizationCode(),
                        request.getRedirectUri(),
                        request.getInviteToken()
                );

        return ResponseEntity.ok(
                ChildInviteOAuthResponse.from(result)
        );
    }

    @ApiOperation(
            value = "부모 초대코드 기반 소셜 회원가입/로그인",
            notes = "부모 초대 토큰과 소셜 인가 코드를 검증하고 "
                    + "PARENT 회원을 생성하거나 로그인합니다. "
                    + "이 단계에서는 부모·자녀 관계를 등록하지 않으며, "
                    + "가족 초대 상태를 PENDING으로 유지합니다. "
                    + "부모 관계 유형은 별도의 가족 초대 수락 API에서 전달합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "로그인 및 수락 대기 초대 정보 조회 성공",
                    response = ParentInviteOAuthResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "요청값 오류, 지원하지 않는 제공자 또는 사용할 수 없는 부모 초대",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 401,
                    message = "유효하지 않은 인가 코드 또는 탈퇴 회원",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 409,
                    message = "초대 유형과 기존 회원 유형 불일치",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 502,
                    message = "소셜 제공자 통신 실패",
                    response = ApiErrorResponse.class
            )
    })
    @PostMapping("/oauth/{provider}/parent-invite")
    public ResponseEntity<ParentInviteOAuthResponse>
    loginWithParentInvite(
            @ApiParam(
                    value = "소셜 로그인 제공자",
                    allowableValues = "google,kakao",
                    required = true,
                    example = "kakao"
            )
            @PathVariable("provider")
            String provider,
            @Valid
            @RequestBody
            ParentInviteOAuthRequest request
    ) {
        ParentInviteOAuthResult result =
                parentInviteOAuthService.login(
                        provider,
                        request.getAuthorizationCode(),
                        request.getRedirectUri(),
                        request.getInviteToken()
                );

        return ResponseEntity.ok(
                ParentInviteOAuthResponse.from(result)
        );
    }
}