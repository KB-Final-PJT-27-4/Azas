package com.azas.domain.auth.controller;

import com.azas.domain.auth.dto.OAuthLoginRequest;
import com.azas.domain.auth.dto.OAuthLoginResponse;
import com.azas.domain.auth.dto.OAuthLoginResult;
import com.azas.domain.auth.service.OAuthLoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class OAuthController {

    private final OAuthLoginService oauthLoginService;

    @PostMapping("/oauth/{provider}")
    public ResponseEntity<OAuthLoginResponse> login(
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
