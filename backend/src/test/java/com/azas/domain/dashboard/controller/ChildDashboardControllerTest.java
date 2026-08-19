package com.azas.domain.dashboard.controller;

import com.azas.domain.dashboard.dto.ChildDashboardResponse;
import com.azas.domain.dashboard.service.ChildDashboardService;
import com.azas.domain.finance.account.entity.ChildUsageMode;
import com.azas.domain.mission.entity.MissionStatus;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import com.azas.global.exception.GlobalExceptionHandler;
import com.azas.global.security.AccessTokenMemberResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ChildDashboardControllerTest {

    private static final long MEMBER_ID = 9L;

    @Mock
    private AccessTokenMemberResolver accessTokenMemberResolver;

    @Mock
    private ChildDashboardService childDashboardService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ChildDashboardController controller =
                new ChildDashboardController(
                        accessTokenMemberResolver,
                        childDashboardService
                );

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(
                        new MappingJackson2HttpMessageConverter(
                                new ObjectMapper()
                        )
                )
                .build();
    }

    @Test
    void childReadsOwnDashboard() throws Exception {
        when(accessTokenMemberResolver.resolveMemberId(
                "Bearer child-access-token"
        )).thenReturn(MEMBER_ID);
        when(childDashboardService.getDashboard(MEMBER_ID))
                .thenReturn(response());

        mockMvc.perform(
                        get("/api/v1/children/me/dashboard")
                                .header(
                                        "Authorization",
                                        "Bearer child-access-token"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.child.child_id").value(6))
                .andExpect(jsonPath("$.child.name").value("깨비"))
                .andExpect(
                        jsonPath(
                                "$.spending_summary"
                                        + ".display_available_amount"
                        ).value(6000)
                )
                .andExpect(
                        jsonPath(
                                "$.spending_summary"
                                        + ".account_balance_hidden"
                        ).value(true)
                )
                .andExpect(
                        jsonPath(
                                "$.activity_summary"
                                        + ".current_month_transaction_count"
                        ).value(2)
                )
                .andExpect(
                        jsonPath("$.mission_summary.active_count")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.mission_summary.items.length()")
                                .value(2)
                )
                .andExpect(
                        jsonPath(
                                "$.notification_summary.unread_count"
                        ).value(2)
                );

        verify(childDashboardService).getDashboard(MEMBER_ID);
    }

    @Test
    void returnsNullSpendingSummaryWithoutAccount()
            throws Exception {
        when(accessTokenMemberResolver.resolveMemberId(
                "Bearer child-access-token"
        )).thenReturn(MEMBER_ID);

        ChildDashboardResponse response = response();
        response = new ChildDashboardResponse(
                response.getChild(),
                null,
                response.getActivitySummary(),
                response.getMissionSummary(),
                response.getNotificationSummary()
        );
        when(childDashboardService.getDashboard(MEMBER_ID))
                .thenReturn(response);

        performRequest()
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.spending_summary").value(
                                org.hamcrest.Matchers.nullValue()
                        )
                );
    }

    @Test
    void rejectsMissingAccessToken() throws Exception {
        when(accessTokenMemberResolver.resolveMemberId(null))
                .thenThrow(new BusinessException(
                        ErrorCode.ACCESS_TOKEN_REQUIRED
                ));

        mockMvc.perform(get("/api/v1/children/me/dashboard"))
                .andExpect(status().isUnauthorized())
                .andExpect(
                        jsonPath("$.error.code")
                                .value("ACCESS_TOKEN_REQUIRED")
                );

        verifyNoInteractions(childDashboardService);
    }

    @Test
    void rejectsParentMember() throws Exception {
        mockFailure(ErrorCode.CHILD_MEMBER_ACCESS_REQUIRED);

        performRequest()
                .andExpect(status().isForbidden())
                .andExpect(
                        jsonPath("$.error.code")
                                .value("CHILD_MEMBER_ACCESS_REQUIRED")
                );
    }

    @Test
    void returnsNotFoundForMissingChildProfile()
            throws Exception {
        mockFailure(ErrorCode.CHILD_NOT_FOUND);

        performRequest()
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.error.code")
                                .value("CHILD_NOT_FOUND")
                );
    }

    @Test
    void returnsConflictForMissingUsagePolicy()
            throws Exception {
        mockFailure(ErrorCode.CHILD_USAGE_POLICY_NOT_CONFIGURED);

        performRequest()
                .andExpect(status().isConflict())
                .andExpect(
                        jsonPath("$.error.code")
                                .value(
                                        "CHILD_USAGE_POLICY_NOT_CONFIGURED"
                                )
                );
    }

    private org.springframework.test.web.servlet.ResultActions
    performRequest() throws Exception {
        return mockMvc.perform(
                get("/api/v1/children/me/dashboard")
                        .header(
                                "Authorization",
                                "Bearer child-access-token"
                        )
        );
    }

    private void mockFailure(ErrorCode errorCode) {
        when(accessTokenMemberResolver.resolveMemberId(
                "Bearer child-access-token"
        )).thenReturn(MEMBER_ID);
        when(childDashboardService.getDashboard(MEMBER_ID))
                .thenThrow(new BusinessException(errorCode));
    }

    private ChildDashboardResponse response() {
        return new ChildDashboardResponse(
                new ChildDashboardResponse.ChildInfo(
                        6L,
                        "깨비",
                        null
                ),
                new ChildDashboardResponse.SpendingSummary(
                        40002L,
                        ChildUsageMode.CO_MANAGED,
                        new BigDecimal("6000.00"),
                        true,
                        new BigDecimal("20000.00"),
                        new BigDecimal("14000.00"),
                        new BigDecimal("6000.00"),
                        70,
                        false,
                        "2026-08"
                ),
                new ChildDashboardResponse.ActivitySummary(
                        1,
                        2
                ),
                new ChildDashboardResponse.MissionSummary(
                        1,
                        List.of(
                                new ChildDashboardResponse.MissionItem(
                                        1L,
                                        "용돈기입장 작성하기",
                                        "이번 주 용돈기입장 쓰기",
                                        new BigDecimal("1000.00"),
                                        MissionStatus.APPROVED
                                ),
                                new ChildDashboardResponse.MissionItem(
                                        2L,
                                        "소비 계획 지키기",
                                        "이번 주 계획한 소비 지키기",
                                        new BigDecimal("2000.00"),
                                        MissionStatus.ASSIGNED
                                )
                        )
                ),
                new ChildDashboardResponse.NotificationSummary(2L)
        );
    }
}
