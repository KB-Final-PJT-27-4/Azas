package com.azas.domain.auth.service;

import com.azas.domain.auth.dto.OAuthProfile;
import com.azas.domain.auth.entity.OAuthProvider;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

@Component
public class KakaoOAuthClient implements OAuthClient {

    private static final String TOKEN_URI =
            "https://kauth.kakao.com/oauth/token";

    private static final String USER_INFO_URI =
            "https://kapi.kakao.com/v2/user/me";

    private final OAuthHttpClient oauthHttpClient;
    private final String clientId;
    private final String clientSecret;

    public KakaoOAuthClient(
            OAuthHttpClient oauthHttpClient,
            @Value("${KAKAO_CLIENT_ID}") String clientId,
            @Value("${KAKAO_CLIENT_SECRET:}") String clientSecret
    ) {
        this.oauthHttpClient = oauthHttpClient;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Override
    public OAuthProvider getProvider() {
        return OAuthProvider.KAKAO;
    }

    @Override
    public OAuthProfile fetchProfile(
            String authorizationCode,
            String redirectUri
    ) {
        MultiValueMap<String, String> form =
                new LinkedMultiValueMap<>();

        form.add("grant_type", "authorization_code");
        form.add("client_id", clientId);
        form.add("redirect_uri", redirectUri);
        form.add("code", authorizationCode);

        if (StringUtils.hasText(clientSecret)) {
            form.add("client_secret", clientSecret);
        }

        String accessToken =
                oauthHttpClient.exchangeAccessToken(
                        TOKEN_URI,
                        form
                );

        JsonNode userInfo =
                oauthHttpClient.requestUserInfo(
                        USER_INFO_URI,
                        accessToken
                );

        JsonNode kakaoAccount =
                userInfo.path("kakao_account");
        JsonNode profile =
                kakaoAccount.path("profile");

        return new OAuthProfile(
                OAuthProvider.KAKAO,
                requiredText(userInfo.path("id").asText(null)),
                requiredText(kakaoAccount.path("email").asText(null)),
                requiredText(profile.path("nickname").asText(null)),
                profile.path("profile_image_url").asText(null)
        );
    }

    private String requiredText(String value) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(
                    ErrorCode.OAUTH_PROVIDER_ERROR
            );
        }

        return value;
    }
}
