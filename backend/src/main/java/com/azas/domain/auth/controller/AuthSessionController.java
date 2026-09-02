package com.azas.domain.auth.controller;

import com.azas.domain.auth.dto.TokenLogoutRequest;
import com.azas.domain.auth.service.TokenLogoutService;
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
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthSessionController {

    private final TokenLogoutService tokenLogoutService;

    @ApiOperation(
            value = "로그아웃",
            notes = "현재 기기에서 사용 중인 Refresh Token을 폐기합니다. "
                    + "이미 폐기됐거나 존재하지 않는 토큰 요청도 성공으로 처리합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    code = 204,
                    message = "로그아웃 성공"
            ),
            @ApiResponse(
                    code = 400,
                    message = "Refresh Token 누락 또는 빈 값",
                    response = ApiErrorResponse.class
            )
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Valid
            @RequestBody
            TokenLogoutRequest request
    ) {
        tokenLogoutService.logout(
                request.getRefreshToken()
        );

        return ResponseEntity.noContent().build();
    }
}