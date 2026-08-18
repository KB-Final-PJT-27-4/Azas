package com.azas.domain.allowance.controller;

import com.azas.domain.allowance.dto.AllowanceRequestDetailResponse;
import com.azas.domain.allowance.entity.AllowanceRequestStatus;
import com.azas.domain.allowance.service.AllowanceRequestService;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import com.azas.global.exception.GlobalExceptionHandler;
import com.azas.global.security.AccessTokenMemberResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AllowanceRequestDetailControllerTest {

    private static final long MEMBER_ID = 20L;
    private static final long CHILD_ID = 6L;
    private static final long ALLOWANCE_REQUEST_ID = 41L;

    @Mock
    private AllowanceRequestService allowanceRequestService;

    @Mock
    private AccessTokenMemberResolver accessTokenMemberResolver;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AllowanceRequestController controller =
                new AllowanceRequestController(
                        allowanceRequestService,
                        accessTokenMemberResolver
                );

        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(
                        SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
                );

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(
                        new MappingJackson2HttpMessageConverter(
                                objectMapper
                        )
                )
                .build();
    }

    @Test
    void getsAllowanceRequestDetail() throws Exception {
        AllowanceRequestDetailResponse response =
                new AllowanceRequestDetailResponse(
                        ALLOWANCE_REQUEST_ID,
                        CHILD_ID,
                        new BigDecimal("10000"),
                        "밥먹을래",
                        AllowanceRequestStatus.PENDING,
                        LocalDateTime.of(
                                2026,
                                7,
                                15,
                                10,
                                30
                        )
                );

        when(accessTokenMemberResolver.resolveMemberId(
                "Bearer access-token"
        )).thenReturn(MEMBER_ID);

        when(allowanceRequestService.getAllowanceRequestDetail(
                MEMBER_ID,
                ALLOWANCE_REQUEST_ID
        )).thenReturn(response);

        mockMvc.perform(get(
                        "/api/v1/allowance-requests/{allowance_request_id}",
                        ALLOWANCE_REQUEST_ID
                )
                        .header(
                                "Authorization",
                                "Bearer access-token"
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allowance_request_id")
                        .value(ALLOWANCE_REQUEST_ID))
                .andExpect(jsonPath("$.child_id")
                        .value(CHILD_ID))
                .andExpect(jsonPath("$.requested_amount")
                        .value(10000))
                .andExpect(jsonPath("$.message")
                        .value("밥먹을래"))
                .andExpect(jsonPath("$.status")
                        .value("PENDING"))
                .andExpect(jsonPath("$.requested_at")
                        .value("2026-07-15T10:30:00"));

        verify(allowanceRequestService)
                .getAllowanceRequestDetail(
                        MEMBER_ID,
                        ALLOWANCE_REQUEST_ID
                );
    }

    @Test
    void rejectsMissingAccessToken() throws Exception {
        when(accessTokenMemberResolver.resolveMemberId(null))
                .thenThrow(new BusinessException(
                        ErrorCode.ACCESS_TOKEN_REQUIRED
                ));

        mockMvc.perform(get(
                        "/api/v1/allowance-requests/{allowance_request_id}",
                        ALLOWANCE_REQUEST_ID
                ))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code")
                        .value("ACCESS_TOKEN_REQUIRED"));

        verifyNoInteractions(allowanceRequestService);
    }

    @Test
    void returnsNotFoundForMissingRequest() throws Exception {
        mockServiceFailure(
                ErrorCode.ALLOWANCE_REQUEST_NOT_FOUND
        );

        mockMvc.perform(get(
                        "/api/v1/allowance-requests/{allowance_request_id}",
                        ALLOWANCE_REQUEST_ID
                )
                        .header(
                                "Authorization",
                                "Bearer access-token"
                        ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code")
                        .value("ALLOWANCE_REQUEST_NOT_FOUND"));
    }

    @Test
    void rejectsMemberWithoutChildAccess() throws Exception {
        mockServiceFailure(
                ErrorCode.CHILD_ACCESS_DENIED
        );

        mockMvc.perform(get(
                        "/api/v1/allowance-requests/{allowance_request_id}",
                        ALLOWANCE_REQUEST_ID
                )
                        .header(
                                "Authorization",
                                "Bearer access-token"
                        ))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code")
                        .value("CHILD_ACCESS_DENIED"));
    }

    private void mockServiceFailure(ErrorCode errorCode) {
        when(accessTokenMemberResolver.resolveMemberId(
                "Bearer access-token"
        )).thenReturn(MEMBER_ID);

        when(allowanceRequestService.getAllowanceRequestDetail(
                MEMBER_ID,
                ALLOWANCE_REQUEST_ID
        )).thenThrow(new BusinessException(errorCode));
    }
}