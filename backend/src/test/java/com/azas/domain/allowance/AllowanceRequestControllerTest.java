package com.azas.domain.allowance;

import com.azas.domain.allowance.controller.AllowanceRequestController;
import com.azas.domain.allowance.dto.AllowanceRequestResponse;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AllowanceRequestControllerTest {

    private static final long MEMBER_ID = 20L;
    private static final long CHILD_ID = 10L;

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
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(
                        new MappingJackson2HttpMessageConverter(objectMapper)
                )
                .build();
    }

    @Test
    void createsAllowanceRequest() throws Exception {
        LocalDateTime requestedAt =
                LocalDateTime.of(2026, 8, 13, 10, 30);
        AllowanceRequestResponse response =
                new AllowanceRequestResponse(
                        41L,
                        CHILD_ID,
                        new BigDecimal("10000"),
                        "친구 생일 선물을 사려고 해요!",
                        AllowanceRequestStatus.PENDING,
                        requestedAt
                );

        when(accessTokenMemberResolver.resolveMemberId(
                "Bearer child-token"
        )).thenReturn(MEMBER_ID);
        when(allowanceRequestService.createAllowanceRequest(
                org.mockito.ArgumentMatchers.eq(MEMBER_ID),
                any()
        )).thenReturn(response);

        mockMvc.perform(post(
                        "/api/v1/children/me/allowance-requests"
                )
                        .header("Authorization", "Bearer child-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"
                                + "\"requested_amount\":10000,"
                                + "\"message\":\"친구 생일 선물을 사려고 해요!\""
                                + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.allowance_request_id").value(41))
                .andExpect(jsonPath("$.child_id").value(CHILD_ID))
                .andExpect(jsonPath("$.requested_amount").value(10000))
                .andExpect(jsonPath("$.message")
                        .value("친구 생일 선물을 사려고 해요!"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.requested_at")
                        .value("2026-08-13T10:30:00"));

        verify(allowanceRequestService).createAllowanceRequest(
                org.mockito.ArgumentMatchers.eq(MEMBER_ID),
                any()
        );
    }

    @Test
    void rejectsZeroRequestedAmount() throws Exception {
        performRequest("0", "영화를 보고 싶어요.")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("BADREQUEST"));

        verifyNoInteractions(allowanceRequestService);
    }

    @Test
    void rejectsDecimalRequestedAmount() throws Exception {
        performRequest("1000.5", "영화를 보고 싶어요.")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("BADREQUEST"));

        verifyNoInteractions(allowanceRequestService);
    }

    @Test
    void rejectsBlankMessage() throws Exception {
        performRequest("10000", "   ")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("BADREQUEST"));

        verifyNoInteractions(allowanceRequestService);
    }

    @Test
    void rejectsMessageLongerThanTwoHundredCharacters()
            throws Exception {
        performRequest("10000", "가".repeat(201))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("BADREQUEST"));

        verifyNoInteractions(allowanceRequestService);
    }

    @Test
    void rejectsMissingAccessToken() throws Exception {
        when(accessTokenMemberResolver.resolveMemberId(null))
                .thenThrow(new BusinessException(
                        ErrorCode.ACCESS_TOKEN_REQUIRED
                ));

        mockMvc.perform(post(
                        "/api/v1/children/me/allowance-requests"
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"
                                + "\"requested_amount\":10000,"
                                + "\"message\":\"영화를 보고 싶어요.\""
                                + "}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code")
                        .value("ACCESS_TOKEN_REQUIRED"));

        verifyNoInteractions(allowanceRequestService);
    }

    private org.springframework.test.web.servlet.ResultActions performRequest(
            String requestedAmount,
            String message
    ) throws Exception {
        return mockMvc.perform(post(
                        "/api/v1/children/me/allowance-requests"
                )
                        .header("Authorization", "Bearer child-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"
                                + "\"requested_amount\":"
                                + requestedAmount
                                + ",\"message\":\""
                                + message
                                + "\"}"));
    }
}
