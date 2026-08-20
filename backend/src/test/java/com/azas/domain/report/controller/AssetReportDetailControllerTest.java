package com.azas.domain.report.controller;

import com.azas.domain.report.dto.AssetReportDetailResponse;
import com.azas.domain.report.service.AssetReportService;
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
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AssetReportDetailControllerTest {

    @Mock
    private AccessTokenMemberResolver memberResolver;

    @Mock
    private AssetReportService assetReportService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AssetReportController controller =
                new AssetReportController(
                        memberResolver,
                        assetReportService
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
    void 월간_자산_리포트_상세_조회는_200을_반환한다()
            throws Exception {

        when(memberResolver.resolveMemberId(
                "Bearer parent-access-token"
        )).thenReturn(4L);

        when(assetReportService.getAssetReportDetail(
                4L,
                6L,
                2026,
                7
        )).thenReturn(response());

        mockMvc.perform(
                        get(
                                "/api/v1/children/6"
                                        + "/asset-reports/2026/7"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer parent-access-token"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.asset_report_id")
                                .value(10001)
                )
                .andExpect(
                        jsonPath("$.child_id")
                                .value(6)
                )
                .andExpect(
                        jsonPath("$.report_year")
                                .value(2026)
                )
                .andExpect(
                        jsonPath("$.report_month")
                                .value(7)
                )
                .andExpect(
                        jsonPath("$.period.start_date")
                                .value("2026-07-01")
                )
                .andExpect(
                        jsonPath("$.period.end_date")
                                .value("2026-07-31")
                )
                .andExpect(
                        jsonPath(
                                "$.summary.total_asset_amount"
                        ).value(20750000)
                )
                .andExpect(
                        jsonPath(
                                "$.summary"
                                        + ".monthly_saving_achievement_rate"
                        ).value(25.0)
                )
                .andExpect(
                        jsonPath(
                                "$.goal_summary[0].title"
                        ).value("대학자금")
                )
                .andExpect(
                        jsonPath(
                                "$.goal_summary[0]"
                                        + ".linked_account_count"
                        ).value(2)
                )
                .andExpect(
                        jsonPath(
                                "$.goal_summary[0]"
                                        + ".linked_accounts[0]"
                                        + ".account_number_masked"
                        ).value("952-****-**43")
                )
                .andExpect(
                        jsonPath(
                                "$.insight_items[0].type"
                        ).value(
                                "MONTHLY_SAVING_COMPARISON"
                        )
                );

        verify(assetReportService)
                .getAssetReportDetail(
                        4L,
                        6L,
                        2026,
                        7
                );
    }

    @Test
    void 해당_월_리포트가_없으면_404를_반환한다()
            throws Exception {

        when(memberResolver.resolveMemberId(
                "Bearer parent-access-token"
        )).thenReturn(4L);

        when(assetReportService.getAssetReportDetail(
                4L,
                6L,
                2026,
                7
        )).thenThrow(
                new BusinessException(
                        ErrorCode.ASSET_REPORT_NOT_FOUND
                )
        );

        mockMvc.perform(
                        get(
                                "/api/v1/children/6"
                                        + "/asset-reports/2026/7"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer parent-access-token"
                                )
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.error.code")
                                .value(
                                        "ASSET_REPORT_NOT_FOUND"
                                )
                );
    }

    private AssetReportDetailResponse response() {
        List<AssetReportDetailResponse.LinkedAccount>
                accounts = List.of(
                new AssetReportDetailResponse.LinkedAccount(
                        3L,
                        "KB 아이사랑적금 1",
                        "KB국민은행",
                        "952-****-**43",
                        new BigDecimal("9600000")
                ),
                new AssetReportDetailResponse.LinkedAccount(
                        4L,
                        "KB 아이사랑적금 2",
                        "KB국민은행",
                        "952-****-**57",
                        new BigDecimal("5000000")
                )
        );

        List<AssetReportDetailResponse.GoalSummary>
                goals = List.of(
                new AssetReportDetailResponse.GoalSummary(
                        100L,
                        "대학자금",
                        new BigDecimal("14600000"),
                        new BigDecimal("30000000"),
                        new BigDecimal("48.67"),
                        new BigDecimal("1250000"),
                        2,
                        accounts
                )
        );

        List<AssetReportDetailResponse.InsightItem>
                insights = List.of(
                new AssetReportDetailResponse.InsightItem(
                        "MONTHLY_SAVING_COMPARISON",
                        "지난달보다 90,000원을 더 저축했어요.",
                        "꾸준한 저축 흐름이 아주 좋아요."
                )
        );

        return new AssetReportDetailResponse(
                10001L,
                6L,
                2026,
                7,
                new AssetReportDetailResponse.Period(
                        LocalDate.of(2026, 7, 1),
                        LocalDate.of(2026, 7, 31)
                ),
                new AssetReportDetailResponse.Summary(
                        new BigDecimal("20750000"),
                        new BigDecimal("350000"),
                        new BigDecimal("50000000"),
                        new BigDecimal("20750000"),
                        new BigDecimal("41.5"),
                        new BigDecimal("1250000"),
                        new BigDecimal("5000000"),
                        new BigDecimal("25.0")
                ),
                goals,
                insights,
                Instant.parse(
                        "2026-08-01T00:05:00Z"
                ),
                Instant.parse(
                        "2026-08-01T00:05:00Z"
                )
        );
    }
}
