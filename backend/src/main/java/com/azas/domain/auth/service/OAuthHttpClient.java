package com.azas.domain.auth.service;

import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OAuthHttpClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public String exchangeAccessToken(
            String tokenUri,
            MultiValueMap<String, String> form
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(
                MediaType.APPLICATION_FORM_URLENCODED
        );
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        try {
            ResponseEntity<String> response =
                    restTemplate.postForEntity(
                            tokenUri,
                            new HttpEntity<>(form, headers),
                            String.class
                    );

            JsonNode responseBody = readJson(response.getBody());
            String accessToken = responseBody.path("access_token")
                    .asText(null);

            if (!StringUtils.hasText(accessToken)) {
                throw new BusinessException(
                        ErrorCode.OAUTH_PROVIDER_ERROR
                );
            }

            return accessToken;
        } catch (HttpClientErrorException exception) {
            // 두 제공자의 토큰 API는 유효하지 않은 인가 코드를 400으로 응답한다.
            if (exception.getStatusCode()
                    == HttpStatus.BAD_REQUEST) {
                throw new BusinessException(
                        ErrorCode.INVALID_AUTHORIZATION_CODE,
                        exception
                );
            }

            throw new BusinessException(
                    ErrorCode.OAUTH_PROVIDER_ERROR,
                    exception
            );
        } catch (RestClientException exception) {
            throw new BusinessException(
                    ErrorCode.OAUTH_PROVIDER_ERROR,
                    exception
            );
        }
    }

    public JsonNode requestUserInfo(
            String userInfoUri,
            String accessToken
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        try {
            ResponseEntity<String> response =
                    restTemplate.exchange(
                            userInfoUri,
                            HttpMethod.GET,
                            new HttpEntity<Void>(headers),
                            String.class
                    );

            return readJson(response.getBody());
        } catch (RestClientException exception) {
            throw new BusinessException(
                    ErrorCode.OAUTH_PROVIDER_ERROR,
                    exception
            );
        }
    }

    private JsonNode readJson(String body) {
        if (!StringUtils.hasText(body)) {
            throw new BusinessException(
                    ErrorCode.OAUTH_PROVIDER_ERROR
            );
        }

        try {
            return objectMapper.readTree(body);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(
                    ErrorCode.OAUTH_PROVIDER_ERROR,
                    exception
            );
        }
    }
}
