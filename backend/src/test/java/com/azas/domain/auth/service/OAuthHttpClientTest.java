package com.azas.domain.auth.service;

import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OAuthHttpClientTest {

    private RestTemplate restTemplate;
    private MockRestServiceServer mockServer;
    private OAuthHttpClient oauthHttpClient;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.bindTo(restTemplate).build();
        oauthHttpClient = new OAuthHttpClient(
                restTemplate,
                new ObjectMapper()
        );
    }

    @Test
    void exchangesAuthorizationCodeForAccessToken() {
        String tokenUri = "https://oauth.example.com/token";

        MultiValueMap<String, String> form =
                new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("code", "test-authorization-code");
        form.add("redirect_uri", "http://localhost/callback");

        mockServer.expect(once(), requestTo(tokenUri))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().formData(form))
                .andRespond(withSuccess(
                        """
                                {
                                "access_token": "provider-access-token"
                                }
                                """,
                        MediaType.APPLICATION_JSON
                ));

        String accessToken =
                oauthHttpClient.exchangeAccessToken(tokenUri, form);

        assertEquals("provider-access-token", accessToken);
        mockServer.verify();
    }

    @Test
    void throwsExceptionWhenAuthorizationCodeIsInvalid() {
        String tokenUri = "https://oauth.example.com/token";

        MultiValueMap<String, String> form =
                new LinkedMultiValueMap<>();
        form.add("code", "invalid-authorization-code");

        mockServer.expect(once(), requestTo(tokenUri))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> oauthHttpClient.exchangeAccessToken(tokenUri, form)
        );

        assertEquals(
                ErrorCode.INVALID_AUTHORIZATION_CODE,
                exception.getErrorCode()
        );
        mockServer.verify();
    }

    @Test
    void throwsExceptionWhenAccessTokenIsMissing() {
        String tokenUri = "https://oauth.example.com/token";

        MultiValueMap<String, String> form =
                new LinkedMultiValueMap<>();
        form.add("code", "test-authorization-code");

        mockServer.expect(once(), requestTo(tokenUri))
                .andRespond(withSuccess(
                        """
                                {
                                  "token_type": "Bearer"
                                }
                                """,
                        MediaType.APPLICATION_JSON
                ));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> oauthHttpClient.exchangeAccessToken(tokenUri, form)
        );

        assertEquals(
                ErrorCode.OAUTH_PROVIDER_ERROR,
                exception.getErrorCode()
        );
        mockServer.verify();
    }

    @Test
    void requestsUserInfoWithBearerAccessToken() {
        String userInfoUri = "https://oauth.example.com/userinfo";
        String accessToken = "provider-access-token";

        mockServer.expect(once(), requestTo(userInfoUri))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer provider-access-token"
                ))
                .andRespond(withSuccess(
                        """
                                {
                                  "sub": "provider-user-id",
                                  "email": "parent@example.com",
                                  "name": "김하나"
                                }
                                """,
                        MediaType.APPLICATION_JSON
                ));

        JsonNode userInfo =
                oauthHttpClient.requestUserInfo(
                        userInfoUri,
                        accessToken
                );

        assertEquals(
                "provider-user-id",
                userInfo.path("sub").asText()
        );
        assertEquals(
                "parent@example.com",
                userInfo.path("email").asText()
        );

        mockServer.verify();
    }
}
