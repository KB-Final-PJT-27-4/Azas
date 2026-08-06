package com.azas.domain.auth.service;

import com.azas.domain.auth.dto.OAuthProfile;
import com.azas.domain.auth.entity.OAuthProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class KakaoOAuthClientTest {

    private MockRestServiceServer mockServer;
    private KakaoOAuthClient kakaoOAuthClient;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();

        mockServer = MockRestServiceServer
                .bindTo(restTemplate)
                .build();

        OAuthHttpClient oauthHttpClient =
                new OAuthHttpClient(
                        restTemplate,
                        new ObjectMapper()
                );

        kakaoOAuthClient = new KakaoOAuthClient(
                oauthHttpClient,
                "test-kakao-client-id",
                "test-kakao-client-secret"
        );
    }

    @Test
    void fetchesKakaoProfile() {
        String authorizationCode =
                "test-authorization-code";
        String redirectUri =
                "http://localhost/auth/kakao/callback";

        MultiValueMap<String, String> expectedForm =
                new LinkedMultiValueMap<>();
        expectedForm.add(
                "grant_type",
                "authorization_code"
        );
        expectedForm.add(
                "client_id",
                "test-kakao-client-id"
        );
        expectedForm.add(
                "redirect_uri",
                redirectUri
        );
        expectedForm.add(
                "code",
                authorizationCode
        );
        expectedForm.add(
                "client_secret",
                "test-kakao-client-secret"
        );

        mockServer.expect(
                        once(),
                        requestTo(
                                "https://kauth.kakao.com/oauth/token"
                        )
                )
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().formData(expectedForm))
                .andRespond(withSuccess(
                        """
                                {
                                  "access_token": "kakao-access-token"
                                }
                                """,
                        MediaType.APPLICATION_JSON
                ));

        mockServer.expect(
                        once(),
                        requestTo(
                                "https://kapi.kakao.com/v2/user/me"
                        )
                )
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer kakao-access-token"
                ))
                .andRespond(withSuccess(
                        """
                        {
                          "id": 123456789,
                          "kakao_account": {
                            "email": "parent@example.com",
                            "profile": {
                              "nickname": "김하나",
                              "profile_image_url": "https://example.com/profile.jpg"
                            }
                          }
                        }
                        """,
                        MediaType.APPLICATION_JSON
                ));

        OAuthProfile profile =
                kakaoOAuthClient.fetchProfile(
                        authorizationCode,
                        redirectUri
                );

        assertEquals(
                OAuthProvider.KAKAO,
                profile.getProvider()
        );
        assertEquals(
                "123456789",
                profile.getProviderSubject()
        );
        assertEquals(
                "parent@example.com",
                profile.getEmail()
        );
        assertEquals(
                "김하나",
                profile.getName()
        );
        assertEquals(
                "https://example.com/profile.jpg",
                profile.getProfileImageUrl()
        );

        mockServer.verify();
    }
}
