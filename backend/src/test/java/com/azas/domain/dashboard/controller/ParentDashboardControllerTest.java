package com.azas.domain.dashboard.controller;

import com.azas.domain.dashboard.dto.ParentDashboardResponse;
import com.azas.domain.dashboard.service.ParentDashboardService;
import com.azas.global.exception.GlobalExceptionHandler;
import com.azas.global.security.AccessTokenMemberResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ParentDashboardControllerTest {

    @Mock
    private AccessTokenMemberResolver accessTokenMemberResolver;

    @Mock
    private ParentDashboardService parentDashboardService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        ParentDashboardController controller =
                new ParentDashboardController(
                        accessTokenMemberResolver,
                        parentDashboardService
                );

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void 부모용_대시보드_조회는_200을_반환한다()
            throws Exception {

        ParentDashboardResponse response =
                new ParentDashboardResponse(
                        new ParentDashboardResponse.ChildInfo(
                                6L,
                                "깨비",
                                null
                        ),
                        new ParentDashboardResponse.AssetSummary(
                                new BigDecimal("9600000"),
                                new BigDecimal("350000"),
                                List.of(
                                        new ParentDashboardResponse.FlowItem(
                                                "2026-08",
                                                new BigDecimal("9600000")
                                        )
                                )
                        ),
                        null,
                        new ParentDashboardResponse.QuickSummary(
                                2,
                                null
                        ),
                        new ParentDashboardResponse.ChecklistSummary(
                                6,
                                3,
                                new BigDecimal("50.00"),
                                3,
                                List.of()
                        ),
                        new ParentDashboardResponse.NotificationSummary(2)
                );

        when(accessTokenMemberResolver.resolveMemberId(
                "Bearer access-token"
        )).thenReturn(7L);

        when(parentDashboardService.getDashboard(7L, 6L))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/v1/children/6/dashboard")
                                .header(
                                        "Authorization",
                                        "Bearer access-token"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.child.child_id").value(6))
                .andExpect(jsonPath("$.child.name").value("깨비"))
                .andExpect(
                        jsonPath("$.asset_summary.total_asset_amount")
                                .value(9600000)
                )
                .andExpect(
                        jsonPath("$.checklist_summary.completed_count")
                                .value(3)
                )
                .andExpect(
                        jsonPath("$.notifications.unread_count")
                                .value(2)
                );
    }
}