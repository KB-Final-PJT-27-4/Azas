package com.azas.domain.auth.controller;

import com.azas.domain.auth.dto.OAuthLoginRequest;
import com.azas.domain.auth.dto.OAuthLoginResponse;
import com.azas.domain.auth.dto.OAuthLoginResult;
import com.azas.domain.auth.service.OAuthLoginService;
import com.azas.global.response.ApiErrorResponse;
import io.swagger.annotations.*;
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
}
