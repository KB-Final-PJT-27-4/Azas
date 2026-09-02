package com.azas.domain.member.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

class NaverSensSmsSenderTest {

    private static final String REQUEST_URL =
            "https://sens.apigw.ntruss.com"
                    + "/sms/v2/services/"
                    + "ncp:sms:kr:1234567890:test"
                    + "/messages";

    private RestTemplate restTemplate;
    private MockRestServiceServer mockServer;
    private NaverSensSmsSender smsSender;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();

        mockServer =
                MockRestServiceServer
                        .bindTo(restTemplate)
                        .build();

        Clock clock = Clock.fixed(
                Instant.ofEpochMilli(
                        1700000000000L
                ),
                ZoneOffset.UTC
        );

        smsSender = new NaverSensSmsSender(
                restTemplate,
                clock,
                "access-key",
                "secret-key",
                "ncp:sms:kr:1234567890:test",
                "010-9876-5432"
        );
    }

    @Test
    void sendsVerificationCodeThroughSens() {
        mockServer.expect(
                        once(),
                        requestTo(REQUEST_URL)
                )
                .andExpect(
                        method(HttpMethod.POST)
                )
                .andExpect(
                        header(
                                "x-ncp-apigw-timestamp",
                                "1700000000000"
                        )
                )
                .andExpect(
                        header(
                                "x-ncp-iam-access-key",
                                "access-key"
                        )
                )
                .andExpect(
                        header(
                                "x-ncp-apigw-signature-v2",
                                "znee/kTQJKBv0NKiX6sXeaB"
                                        + "+UE+2693snizX4WkCNco="
                        )
                )
                .andExpect(
                        jsonPath("$.type")
                                .value("SMS")
                )
                .andExpect(
                        jsonPath("$.contentType")
                                .value("COMM")
                )
                .andExpect(
                        jsonPath("$.countryCode")
                                .value("82")
                )
                .andExpect(
                        jsonPath("$.from")
                                .value("01098765432")
                )
                .andExpect(
                        jsonPath("$.messages[0].to")
                                .value("01012345678")
                )
                .andExpect(
                        jsonPath("$.content")
                                .value(
                                        "[Azas] 인증번호는 "
                                                + "[123456]입니다."
                                )
                )
                .andRespond(
                        withStatus(HttpStatus.ACCEPTED)
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .body("""
                                        {
                                          "requestId": "request-id",
                                          "requestTime": "2026-08-06T12:00:00.000",
                                          "statusCode": "202",
                                          "statusName": "success"
                                        }
                                        """)
                );

        smsSender.sendVerificationCode(
                "01012345678",
                "123456"
        );

        mockServer.verify();
    }

    @Test
    void throwsExceptionWhenSensRequestFails() {
        mockServer.expect(
                        once(),
                        requestTo(REQUEST_URL)
                )
                .andRespond(withServerError());

        assertThrows(
                IllegalStateException.class,
                () -> smsSender.sendVerificationCode(
                        "01012345678",
                        "123456"
                )
        );

        mockServer.verify();
    }
}