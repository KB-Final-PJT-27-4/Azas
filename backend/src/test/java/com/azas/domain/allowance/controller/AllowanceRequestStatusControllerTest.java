package com.azas.domain.allowance.controller;

import com.azas.domain.allowance.dto.AllowanceRequestDetailResponse;
import com.azas.domain.allowance.dto.UpdateAllowanceRequestStatus;
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
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AllowanceRequestStatusControllerTest {

    private static final long MEMBER_ID = 20L;
    private static final long CHILD_ID = 6L;
    private static final long REQUEST_ID = 41L;

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
    void updatesAllowanceRequestStatus() throws Exception {
        when(accessTokenMemberResolver.resolveMemberId(
                "Bearer access-token"
        )).thenReturn(MEMBER_ID);

        when(allowanceRequestService.updateAllowanceRequestStatus(
                org.mockito.ArgumentMatchers.eq(MEMBER_ID),
                org.mockito.ArgumentMatchers.eq(REQUEST_ID),
                any(UpdateAllowanceRequestStatus.class)
        )).thenReturn(response(
                AllowanceRequestStatus.APPROVED
        ));

        mockMvc.perform(patch(
                        "/api/v1/allowance-requests/{allowance_request_id}",
                        REQUEST_ID
                )
                        .header(
                                "Authorization",
                                "Bearer access-token"
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "action": "APPROVE"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allowance_request_id")
                        .value(REQUEST_ID))
                .andExpect(jsonPath("$.child_id")
                        .value(CHILD_ID))
                .andExpect(jsonPath("$.requested_amount")
                        .value(10000))
                .andExpect(jsonPath("$.message")
                        .value("밥먹을래"))
                .andExpect(jsonPath("$.status")
                        .value("APPROVED"))
                .andExpect(jsonPath("$.requested_at")
                        .value("2026-07-15T10:30:00"));

        verify(allowanceRequestService)
                .updateAllowanceRequestStatus(
                        org.mockito.ArgumentMatchers.eq(MEMBER_ID),
                        org.mockito.ArgumentMatchers.eq(REQUEST_ID),
                        any(UpdateAllowanceRequestStatus.class)
                );
    }

    @Test
    void returnsConflictForProcessedRequest() throws Exception {
        when(accessTokenMemberResolver.resolveMemberId(
                "Bearer access-token"
        )).thenReturn(MEMBER_ID);

        when(allowanceRequestService.updateAllowanceRequestStatus(
                org.mockito.ArgumentMatchers.eq(MEMBER_ID),
                org.mockito.ArgumentMatchers.eq(REQUEST_ID),
                any(UpdateAllowanceRequestStatus.class)
        )).thenThrow(new BusinessException(
                ErrorCode.INVALID_ALLOWANCE_STATUS_TRANSITION
        ));

        mockMvc.perform(patch(
                        "/api/v1/allowance-requests/{allowance_request_id}",
                        REQUEST_ID
                )
                        .header(
                                "Authorization",
                                "Bearer access-token"
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "action": "APPROVE"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code")
                        .value(
                                "INVALID_ALLOWANCE_STATUS_TRANSITION"
                        ));
    }

    @Test
    void returnsForbiddenWithoutStatusChangeAccess()
            throws Exception {
        when(accessTokenMemberResolver.resolveMemberId(
                "Bearer access-token"
        )).thenReturn(MEMBER_ID);

        when(allowanceRequestService.updateAllowanceRequestStatus(
                org.mockito.ArgumentMatchers.eq(MEMBER_ID),
                org.mockito.ArgumentMatchers.eq(REQUEST_ID),
                any(UpdateAllowanceRequestStatus.class)
        )).thenThrow(new BusinessException(
                ErrorCode.ALLOWANCE_REQUEST_ACCESS_DENIED
        ));

        mockMvc.perform(patch(
                        "/api/v1/allowance-requests/{allowance_request_id}",
                        REQUEST_ID
                )
                        .header(
                                "Authorization",
                                "Bearer access-token"
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "action": "APPROVE"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code")
                        .value(
                                "ALLOWANCE_REQUEST_ACCESS_DENIED"
                        ));
    }

    private AllowanceRequestDetailResponse response(
            AllowanceRequestStatus status
    ) {
        return new AllowanceRequestDetailResponse(
                REQUEST_ID,
                CHILD_ID,
                new BigDecimal("10000"),
                "밥먹을래",
                status,
                LocalDateTime.of(
                        2026,
                        7,
                        15,
                        10,
                        30
                )
        );
    }
}