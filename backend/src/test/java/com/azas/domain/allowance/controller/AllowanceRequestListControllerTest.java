package com.azas.domain.allowance.controller;

import com.azas.domain.allowance.dto.AllowanceRequestListItemResponse;
import com.azas.domain.allowance.dto.AllowanceRequestListResponse;
import com.azas.domain.allowance.entity.AllowanceRequestStatus;
import com.azas.domain.allowance.service.AllowanceRequestService;
import com.azas.global.exception.GlobalExceptionHandler;
import com.azas.global.security.AccessTokenMemberResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AllowanceRequestListControllerTest {

    private static final long MEMBER_ID = 20L;
    private static final long CHILD_ID = 6L;

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
                .registerModule(new JavaTimeModule());

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
    void getsAllowanceRequestList() throws Exception {
        AllowanceRequestListItemResponse item =
                new AllowanceRequestListItemResponse(
                        41L,
                        CHILD_ID,
                        new BigDecimal("10000"),
                        AllowanceRequestStatus.PENDING,
                        LocalDateTime.of(
                                2026,
                                7,
                                15,
                                10,
                                30
                        )
                );

        AllowanceRequestListResponse response =
                new AllowanceRequestListResponse(
                        List.of(item),
                        null,
                        false
                );

        when(accessTokenMemberResolver.resolveMemberId(
                "Bearer access-token"
        )).thenReturn(MEMBER_ID);

        when(allowanceRequestService.getAllowanceRequests(
                MEMBER_ID,
                CHILD_ID,
                "PENDING",
                null,
                "20"
        )).thenReturn(response);

        mockMvc.perform(get(
                        "/api/v1/children/{child_id}/allowance-requests",
                        CHILD_ID
                )
                        .header(
                                "Authorization",
                                "Bearer access-token"
                        )
                        .param("status", "PENDING")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].allowance_request_id")
                        .value(41))
                .andExpect(jsonPath("$.items[0].child_id")
                        .value(CHILD_ID))
                .andExpect(jsonPath("$.items[0].requested_amount")
                        .value(10000))
                .andExpect(jsonPath("$.items[0].status")
                        .value("PENDING"))
                .andExpect(jsonPath("$.next_cursor").doesNotExist())
                .andExpect(jsonPath("$.has_next").value(false));

        verify(allowanceRequestService).getAllowanceRequests(
                MEMBER_ID,
                CHILD_ID,
                "PENDING",
                null,
                "20"
        );
    }

    @Test
    void returnsEmptyItemsWhenNoRequestsExist()
            throws Exception {
        when(accessTokenMemberResolver.resolveMemberId(
                "Bearer access-token"
        )).thenReturn(MEMBER_ID);

        when(allowanceRequestService.getAllowanceRequests(
                MEMBER_ID,
                CHILD_ID,
                null,
                null,
                null
        )).thenReturn(
                new AllowanceRequestListResponse(
                        List.of(),
                        null,
                        false
                )
        );

        mockMvc.perform(get(
                        "/api/v1/children/{child_id}/allowance-requests",
                        CHILD_ID
                )
                        .header(
                                "Authorization",
                                "Bearer access-token"
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.has_next").value(false));
    }
}