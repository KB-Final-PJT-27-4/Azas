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

class GoogleOAuthClientTest {

    private MockRestServiceServer mockServer;
    private GoogleOAuthClient googleOAuthClient;

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

        googleOAuthClient = new GoogleOAuthClient(
                oauthHttpClient,
                "test-google-client-id",
                "test-google-client-secret"
        );
    }

    @Test
    void fetchesGoogleProfile() {
        String authorizationCode =
                "test-authorization-code";
        String redirectUri =
                "http://localhost/auth/google/callback";

        MultiValueMap<String, String> expectedForm =
                new LinkedMultiValueMap<>();
        expectedForm.add(
                "grant_type",
                "authorization_code"
        );
        expectedForm.add(
                "client_id",
                "test-google-client-id"
        );
        expectedForm.add(
                "client_secret",
                "test-google-client-secret"
        );
        expectedForm.add(
                "redirect_uri",
                redirectUri
        );
        expectedForm.add(
                "code",
                authorizationCode
        );

        mockServer.expect(
                        once(),
                        requestTo(
                                "https://oauth2.googleapis.com/token"
                        )
                )
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().formData(expectedForm))
                .andRespond(withSuccess(
                        """
                        {
                          "access_token": "google-access-token"
                        }
                        """,
                        MediaType.APPLICATION_JSON
                ));

        mockServer.expect(
                        once(),
                        requestTo(
                                "https://openidconnect.googleapis.com/v1/userinfo"
                        )
                )
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer google-access-token"
                ))
                .andRespond(withSuccess(
                        """
                        {
                          "sub": "google-user-id",
                          "email": "parent@example.com",
                          "name": "김하나",
                          "picture": "https://example.com/profile.jpg"
                        }
                        """,
                        MediaType.APPLICATION_JSON
                ));

        OAuthProfile profile =
                googleOAuthClient.fetchProfile(
                        authorizationCode,
                        redirectUri
                );

        assertEquals(
                OAuthProvider.GOOGLE,
                profile.getProvider()
        );
        assertEquals(
                "google-user-id",
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