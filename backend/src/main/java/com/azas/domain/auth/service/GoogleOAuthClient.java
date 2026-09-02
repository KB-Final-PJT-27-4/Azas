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
public class GoogleOAuthClient implements OAuthClient {

    private static final String TOKEN_URI =
            "https://oauth2.googleapis.com/token";

    private static final String USER_INFO_URI =
            "https://openidconnect.googleapis.com/v1/userinfo";

    private final OAuthHttpClient oauthHttpClient;
    private final String clientId;
    private final String clientSecret;

    public GoogleOAuthClient(
            OAuthHttpClient oauthHttpClient,
            @Value("${GOOGLE_CLIENT_ID}") String clientId,
            @Value("${GOOGLE_CLIENT_SECRET}") String clientSecret
    ) {
        this.oauthHttpClient = oauthHttpClient;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Override
    public OAuthProvider getProvider() {
        return OAuthProvider.GOOGLE;
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
        form.add("client_secret", clientSecret);
        form.add("redirect_uri", redirectUri);
        form.add("code", authorizationCode);

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

        return new OAuthProfile(
                OAuthProvider.GOOGLE,
                requiredText(userInfo.path("sub").asText(null)),
                requiredText(userInfo.path("email").asText(null)),
                requiredText(userInfo.path("name").asText(null)),
                userInfo.path("picture").asText(null)
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
