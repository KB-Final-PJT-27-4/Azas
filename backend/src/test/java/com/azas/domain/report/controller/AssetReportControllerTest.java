package com.azas.domain.report.controller;

import com.azas.domain.report.dto.AssetReportListItemResponse;
import com.azas.domain.report.dto.AssetReportListResponse;
import com.azas.domain.report.service.AssetReportService;
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
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AssetReportControllerTest {

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
    void 자산_리포트_월_목록_조회는_200을_반환한다()
            throws Exception {

        when(memberResolver.resolveMemberId(
                "Bearer parent-access-token"
        )).thenReturn(4L);

        AssetReportListItemResponse item =
                new AssetReportListItemResponse(
                        10001L,
                        2026,
                        7,
                        "2026-07",
                        new BigDecimal("20750000"),
                        new BigDecimal("350000"),
                        new BigDecimal("1250000"),
                        new BigDecimal("41.5"),
                        Instant.parse(
                                "2026-07-31T15:05:00Z"
                        ),
                        Instant.parse(
                                "2026-07-31T15:05:00Z"
                        )
                );

        when(assetReportService.getAssetReports(
                4L,
                6L,
                2026,
                null,
                12
        )).thenReturn(
                new AssetReportListResponse(
                        List.of(item),
                        null,
                        false
                )
        );

        mockMvc.perform(
                        get(
                                "/api/v1/children/6/asset-reports"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer parent-access-token"
                                )
                                .param("year", "2026")
                                .param("size", "12")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.items[0].asset_report_id")
                                .value(10001)
                )
                .andExpect(
                        jsonPath("$.items[0].report_year")
                                .value(2026)
                )
                .andExpect(
                        jsonPath("$.items[0].report_month")
                                .value(7)
                )
                .andExpect(
                        jsonPath("$.items[0].period")
                                .value("2026-07")
                )
                .andExpect(
                        jsonPath(
                                "$.items[0].total_asset_amount"
                        ).value(20750000)
                )
                .andExpect(
                        jsonPath("$.next_cursor")
                                .doesNotExist()
                )
                .andExpect(
                        jsonPath("$.has_next")
                                .value(false)
                );

        verify(assetReportService).getAssetReports(
                4L,
                6L,
                2026,
                null,
                12
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
                                "/api/v1/children/6/asset-reports"
                        )
                )
                .andExpect(status().isUnauthorized())
                .andExpect(
                        jsonPath("$.error.code")
                                .value("ACCESS_TOKEN_REQUIRED")
                );

        verifyNoInteractions(assetReportService);
    }

    @Test
    void 잘못된_조회조건은_400을_반환한다()
            throws Exception {

        when(memberResolver.resolveMemberId(
                "Bearer parent-access-token"
        )).thenReturn(4L);

        when(assetReportService.getAssetReports(
                4L,
                6L,
                2026,
                null,
                13
        )).thenThrow(
                new BusinessException(
                        ErrorCode.INVALID_QUERY_PARAMETER
                )
        );

        mockMvc.perform(
                        get(
                                "/api/v1/children/6/asset-reports"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer parent-access-token"
                                )
                                .param("year", "2026")
                                .param("size", "13")
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.error.code")
                                .value(
                                        "INVALID_QUERY_PARAMETER"
                                )
                );
    }
}