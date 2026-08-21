package com.azas.domain.report.controller;

import com.azas.domain.report.dto.ChildcareReportDetailResponse;
import com.azas.domain.report.service.ChildcareReportService;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
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
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.fasterxml.jackson.databind.SerializationFeature;

@ExtendWith(MockitoExtension.class)
class ChildcareReportControllerTest {

    @Mock
    private AccessTokenMemberResolver memberResolver;

    @Mock
    private ChildcareReportService childcareReportService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ChildcareReportController controller =
                new ChildcareReportController(
                        memberResolver,
                        childcareReportService
                );

        ObjectMapper objectMapper =
                new ObjectMapper();

        objectMapper.registerModule(
                new JavaTimeModule()
        );

        objectMapper.disable(
                SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
        );

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(
                        new GlobalExceptionHandler()
                )
                .setMessageConverters(
                        new MappingJackson2HttpMessageConverter(
                                objectMapper
                        )
                )
                .build();
    }

    @Test
    void 월간_양육비_리포트_조회는_200을_반환한다()
            throws Exception {

        when(memberResolver.resolveMemberId(
                "Bearer parent-access-token"
        )).thenReturn(4L);

        when(childcareReportService.getReport(
                4L,
                6L,
                2026,
                7
        )).thenReturn(response());

        mockMvc.perform(
                        get(
                                "/api/v1/children/6"
                                        + "/childcare-reports/2026/7"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer parent-access-token"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.child_id").value(6)
                )
                .andExpect(
                        jsonPath("$.report_year").value(2026)
                )
                .andExpect(
                        jsonPath("$.report_month").value(7)
                )
                .andExpect(
                        jsonPath("$.period.start_date")
                                .value("2026-07-01")
                )
                .andExpect(
                        jsonPath(
                                "$.summary.total_expense_amount"
                        ).value(1860000)
                )
                .andExpect(
                        jsonPath(
                                "$.summary"
                                        + ".previous_month_change_rate"
                        ).value(6.9)
                )
                .andExpect(
                        jsonPath(
                                "$.summary.annual_expense_amount"
                        ).value(3600000)
                )
                .andExpect(
                        jsonPath("$.monthly_flow.length()")
                                .value(12)
                );
    }

    @Test
    void 토큰이_없으면_401을_반환한다()
            throws Exception {

        when(memberResolver.resolveMemberId(null))
                .thenThrow(
                        new BusinessException(
                                ErrorCode.ACCESS_TOKEN_REQUIRED
                        )
                );

        mockMvc.perform(
                        get(
                                "/api/v1/children/6"
                                        + "/childcare-reports/2026/7"
                        )
                )
                .andExpect(status().isUnauthorized())
                .andExpect(
                        jsonPath("$.error.code")
                                .value("ACCESS_TOKEN_REQUIRED")
                );

        verifyNoInteractions(childcareReportService);
    }

    @Test
    void 부모_권한이_없으면_403을_반환한다()
            throws Exception {

        when(memberResolver.resolveMemberId(
                "Bearer access-token"
        )).thenReturn(4L);

        when(childcareReportService.getReport(
                4L,
                6L,
                2026,
                7
        )).thenThrow(
                new BusinessException(
                        ErrorCode.PARENT_ACCESS_REQUIRED
                )
        );

        mockMvc.perform(
                        get(
                                "/api/v1/children/6"
                                        + "/childcare-reports/2026/7"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer access-token"
                                )
                )
                .andExpect(status().isForbidden())
                .andExpect(
                        jsonPath("$.error.code")
                                .value("PARENT_ACCESS_REQUIRED")
                );
    }

    private ChildcareReportDetailResponse response() {
        return new ChildcareReportDetailResponse(
                6L,
                2026,
                7,
                new ChildcareReportDetailResponse.Period(
                        LocalDate.of(2026, 7, 1),
                        LocalDate.of(2026, 7, 31)
                ),
                new ChildcareReportDetailResponse.Summary(
                        new BigDecimal("1860000"),
                        new BigDecimal("1740000"),
                        new BigDecimal("120000"),
                        new BigDecimal("6.9"),
                        new BigDecimal("3600000")
                ),
                createMonthlyFlow(),
                Instant.parse(
                        "2026-08-20T01:30:00Z"
                )
        );
    }

    private List<
            ChildcareReportDetailResponse.MonthlyFlowItem
            > createMonthlyFlow() {

        return List.of(
                new ChildcareReportDetailResponse.MonthlyFlowItem(
                        2025, 8, BigDecimal.ZERO
                ),
                new ChildcareReportDetailResponse.MonthlyFlowItem(
                        2025, 9, BigDecimal.ZERO
                ),
                new ChildcareReportDetailResponse.MonthlyFlowItem(
                        2025, 10, BigDecimal.ZERO
                ),
                new ChildcareReportDetailResponse.MonthlyFlowItem(
                        2025, 11, BigDecimal.ZERO
                ),
                new ChildcareReportDetailResponse.MonthlyFlowItem(
                        2025, 12, BigDecimal.ZERO
                ),
                new ChildcareReportDetailResponse.MonthlyFlowItem(
                        2026, 1, BigDecimal.ZERO
                ),
                new ChildcareReportDetailResponse.MonthlyFlowItem(
                        2026, 2, BigDecimal.ZERO
                ),
                new ChildcareReportDetailResponse.MonthlyFlowItem(
                        2026, 3, BigDecimal.ZERO
                ),
                new ChildcareReportDetailResponse.MonthlyFlowItem(
                        2026, 4, BigDecimal.ZERO
                ),
                new ChildcareReportDetailResponse.MonthlyFlowItem(
                        2026, 5, BigDecimal.ZERO
                ),
                new ChildcareReportDetailResponse.MonthlyFlowItem(
                        2026,
                        6,
                        new BigDecimal("1740000")
                ),
                new ChildcareReportDetailResponse.MonthlyFlowItem(
                        2026,
                        7,
                        new BigDecimal("1860000")
                )
        );
    }
}