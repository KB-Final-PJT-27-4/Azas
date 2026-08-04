package com.azas.domain.auth.controller;

import com.azas.domain.auth.dto.AuthTokenPair;
import com.azas.domain.auth.dto.TokenRefreshRequest;
import com.azas.domain.auth.dto.TokenRefreshResponse;
import com.azas.domain.auth.service.TokenRefreshService;
import com.azas.global.response.ApiErrorResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Api(tags = "인증")
@RestController
@RequestMapping("/api/v1/auth/token")
@RequiredArgsConstructor
public class AuthTokenController {

    private final TokenRefreshService tokenRefreshService;

    @ApiOperation(
            value = "Access Token 재발급",
            notes = "유효한 Refresh Token을 새 Access Token과 Refresh Token으로 교체합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "토큰 재발급 성공",
                    response = TokenRefreshResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "Refresh Token 누락 또는 빈 값",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 401,
                    message = "유효하지 않거나 만료·폐기된 Refresh Token",
                    response = ApiErrorResponse.class
            )
    })
    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> refresh(
            @Valid
            @RequestBody
            TokenRefreshRequest request
    ) {
        AuthTokenPair tokenPair =
                tokenRefreshService.refresh(
                        request.getRefreshToken()
                );

        return ResponseEntity.ok(
                TokenRefreshResponse.from(tokenPair)
        );
    }
}
