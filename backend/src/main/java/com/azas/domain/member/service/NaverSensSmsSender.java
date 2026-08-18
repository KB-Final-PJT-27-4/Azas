package com.azas.domain.member.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Clock;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class NaverSensSmsSender implements SmsSender {

    private static final String API_BASE_URL =
            "https://sens.apigw.ntruss.com";

    private static final String HTTP_METHOD = "POST";

    private final RestTemplate restTemplate;
    private final Clock clock;
    private final String accessKey;
    private final String secretKey;
    private final String serviceId;
    private final String senderNumber;

    public NaverSensSmsSender(
            String accessKey,
            String secretKey,
            String serviceId,
            String senderNumber
    ) {
        this(
                new RestTemplate(),
                Clock.systemUTC(),
                accessKey,
                secretKey,
                serviceId,
                senderNumber
        );
    }

    NaverSensSmsSender(
            RestTemplate restTemplate,
            Clock clock,
            String accessKey,
            String secretKey,
            String serviceId,
            String senderNumber
    ) {
        this.restTemplate =
                Objects.requireNonNull(restTemplate);
        this.clock =
                Objects.requireNonNull(clock);
        this.accessKey =
                requireText(accessKey, "accessKey");
        this.secretKey =
                requireText(secretKey, "secretKey");
        this.serviceId =
                requireText(serviceId, "serviceId");
        this.senderNumber =
                requireText(senderNumber, "senderNumber")
                        .replaceAll("[^0-9]", "");
    }

    @Override
    public void sendVerificationCode(
            String phoneNumber,
            String verificationCode
    ) {
        String requestPath =
                "/sms/v2/services/"
                        + serviceId
                        + "/messages";

        long timestamp = clock.millis();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(
                MediaType.APPLICATION_JSON
        );
        headers.set(
                "x-ncp-apigw-timestamp",
                String.valueOf(timestamp)
        );
        headers.set(
                "x-ncp-iam-access-key",
                accessKey
        );
        headers.set(
                "x-ncp-apigw-signature-v2",
                createSignature(
                        requestPath,
                        timestamp
                )
        );

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(
                        createRequestBody(
                                phoneNumber,
                                verificationCode
                        ),
                        headers
                );

        ResponseEntity<String> response;

        try {
            response = restTemplate.exchange(
                    API_BASE_URL + requestPath,
                    HttpMethod.POST,
                    request,
                    String.class
            );
        } catch (RestClientException exception) {
            throw new IllegalStateException(
                    "SENS SMS 발송 요청에 실패했습니다.",
                    exception
            );
        }

        if (response.getStatusCode()
                != HttpStatus.ACCEPTED) {
            throw new IllegalStateException(
                    "SENS가 SMS 발송 요청을 수락하지 않았습니다."
            );
        }
    }

    private Map<String, Object> createRequestBody(
            String phoneNumber,
            String verificationCode
    ) {
        Map<String, Object> body =
                new LinkedHashMap<>();

        body.put("type", "SMS");
        body.put("contentType", "COMM");
        body.put("countryCode", "82");
        body.put("from", senderNumber);
        body.put(
                "content",
                "[Azas] 인증번호는 ["
                        + verificationCode
                        + "]입니다."
        );
        body.put(
                "messages",
                List.of(
                        Map.of(
                                "to",
                                phoneNumber
                        )
                )
        );

        return body;
    }

    private String createSignature(
            String requestPath,
            long timestamp
    ) {
        String message =
                HTTP_METHOD
                        + " "
                        + requestPath
                        + "\n"
                        + timestamp
                        + "\n"
                        + accessKey;

        try {
            Mac mac = Mac.getInstance(
                    "HmacSHA256"
            );

            mac.init(
                    new SecretKeySpec(
                            secretKey.getBytes(
                                    StandardCharsets.UTF_8
                            ),
                            "HmacSHA256"
                    )
            );

            byte[] signature =
                    mac.doFinal(
                            message.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            return Base64.getEncoder()
                    .encodeToString(signature);
        } catch (
                GeneralSecurityException exception
        ) {
            throw new IllegalStateException(
                    "SENS 요청 서명을 생성할 수 없습니다.",
                    exception
            );
        }
    }

    private String requireText(
            String value,
            String fieldName
    ) {
        if (
                value == null
                        || value.isBlank()
        ) {
            throw new IllegalArgumentException(
                    fieldName + "은(는) 필수입니다."
            );
        }

        return value;
    }
}