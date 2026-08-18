package com.azas.domain.finance.autotransfer.controller;

import com.azas.domain.finance.autotransfer.dto.AutoTransferRetryResponse;
import com.azas.domain.finance.autotransfer.service.AutoTransferRetryService;
import com.azas.domain.finance.transfer.entity.TransferStatus;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import com.azas.global.exception.GlobalExceptionHandler;
import com.azas.global.security.AccessTokenMemberResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AutoTransferRetryControllerTest {

    private MockMvc mockMvc;
    private AutoTransferRetryService retryService;
    private AccessTokenMemberResolver memberResolver;

    @BeforeEach
    void setUp() {
        retryService = mock(AutoTransferRetryService.class);
        memberResolver = mock(AccessTokenMemberResolver.class);

        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders.standaloneSetup(
                        new AutoTransferRetryController(
                                retryService,
                                memberResolver
                        )
                )
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(
                        new MappingJackson2HttpMessageConverter(
                                objectMapper
                        )
                )
                .build();
    }

    @Test
    void 실패한_자동이체를_재시도한다() throws Exception {
        String key =
                "93c7a60c-5664-4994-b8f5-b77aa573403d";

        given(memberResolver.resolveMemberId(
                "Bearer access-token"
        )).willReturn(7L);

        given(retryService.retry(7L, 21L, key))
                .willReturn(new AutoTransferRetryResponse(
                        502L,
                        21L,
                        501L,
                        TransferStatus.SUCCEEDED,
                        null,
                        null,
                        Instant.parse("2026-08-18T01:30:00Z"),
                        Instant.parse("2026-08-18T01:30:01Z")
                ));

        mockMvc.perform(
                        post(
                                "/api/v1/auto-transfer-schedules/{schedule_id}/retry",
                                21L
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer access-token"
                                )
                                .header("Idempotency-Key", key)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.financial_transfer_id")
                        .value(502))
                .andExpect(jsonPath("$.auto_transfer_schedule_id")
                        .value(21))
                .andExpect(jsonPath("$.retry_of_transfer_id")
                        .value(501))
                .andExpect(jsonPath("$.status")
                        .value("SUCCEEDED"))
                .andExpect(jsonPath("$.failure_code")
                        .doesNotExist());

        verify(retryService).retry(7L, 21L, key);
    }

    @Test
    void 재시도할_실패_회차가_없으면_409를_반환한다()
            throws Exception {
        String key =
                "93c7a60c-5664-4994-b8f5-b77aa573403d";

        given(memberResolver.resolveMemberId(
                "Bearer access-token"
        )).willReturn(7L);

        given(retryService.retry(7L, 21L, key))
                .willThrow(new BusinessException(
                        ErrorCode.AUTO_TRANSFER_RETRY_NOT_AVAILABLE
                ));

        mockMvc.perform(
                        post(
                                "/api/v1/auto-transfer-schedules/{schedule_id}/retry",
                                21L
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer access-token"
                                )
                                .header("Idempotency-Key", key)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code")
                        .value(
                                "AUTO_TRANSFER_RETRY_NOT_AVAILABLE"
                        ));
    }
}