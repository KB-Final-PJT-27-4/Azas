package com.azas.domain.finance.autotransfer.controller;

import com.azas.domain.finance.autotransfer.dto.AutoTransferScheduleResponse;
import com.azas.domain.finance.autotransfer.entity.AutoTransferFrequency;
import com.azas.domain.finance.autotransfer.entity.AutoTransferScheduleStatus;
import com.azas.domain.finance.autotransfer.service.AutoTransferScheduleService;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import com.azas.global.exception.GlobalExceptionHandler;
import com.azas.global.security.AccessTokenMemberResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import com.azas.domain.finance.autotransfer.dto.AutoTransferScheduleListItemResponse;
import com.azas.domain.finance.autotransfer.dto.AutoTransferScheduleListResponse;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AutoTransferScheduleControllerTest {

    private MockMvc mockMvc;
    private AutoTransferScheduleService service;
    private AccessTokenMemberResolver memberResolver;

    @BeforeEach
    void setUp() {
        service = org.mockito.Mockito.mock(
                AutoTransferScheduleService.class
        );
        memberResolver = org.mockito.Mockito.mock(
                AccessTokenMemberResolver.class
        );

        LocalValidatorFactoryBean validator =
                new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        ObjectMapper objectMapper =
                new ObjectMapper()
                        .registerModule(new JavaTimeModule())
                        .disable(
                                SerializationFeature
                                        .WRITE_DATES_AS_TIMESTAMPS
                        );

        mockMvc = MockMvcBuilders.standaloneSetup(
                        new AutoTransferScheduleController(
                                service,
                                memberResolver
                        )
                )
                .setControllerAdvice(
                        new GlobalExceptionHandler()
                )
                .setValidator(validator)
                .setMessageConverters(
                        new MappingJackson2HttpMessageConverter(
                                objectMapper
                        )
                )
                .build();
    }

    @Test
    void 자동이체_일정_등록은_201을_반환한다()
            throws Exception {
        String key = UUID.randomUUID().toString();

        given(memberResolver.resolveMemberId(
                "Bearer access-token"
        )).willReturn(7L);

        given(service.createSchedule(
                eq(7L),
                eq(key),
                any()
        )).willReturn(
                new AutoTransferScheduleResponse(
                        21L,
                        31L,
                        1L,
                        12L,
                        new BigDecimal("80000"),
                        AutoTransferFrequency.MONTHLY,
                        10,
                        LocalDate.of(2026, 9, 10),
                        LocalDate.of(2029, 2, 10),
                        Instant.parse(
                                "2026-09-10T00:00:00Z"
                        ),
                        null,
                        null,
                        AutoTransferScheduleStatus.ACTIVE,
                        Instant.parse(
                                "2026-08-17T06:00:00Z"
                        )
                )
        );

        mockMvc.perform(
                        post("/api/v1/auto-transfer-schedules")
                                .header(
                                        "Authorization",
                                        "Bearer access-token"
                                )
                                .header("Idempotency-Key", key)
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                                {
                                                  "child_id": 6,
                                                  "source_account_id": 1,
                                                  "destination_account_id": 12,
                                                  "amount": 80000,
                                                  "frequency": "MONTHLY",
                                                  "transfer_day": 10,
                                                  "start_date": "2026-09-10",
                                                  "end_date": "2029-02-10"
                                                }
                                                """
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath(
                        "$.auto_transfer_schedule_id"
                ).value(21))
                .andExpect(jsonPath(
                        "$.financial_goal_id"
                ).value(31))
                .andExpect(jsonPath(
                        "$.next_transfer_at"
                ).value("2026-09-10T00:00:00Z"))
                .andExpect(jsonPath(
                        "$.status"
                ).value("ACTIVE"));
    }

    @Test
    void 필수_본문이_누락되면_400을_반환한다()
            throws Exception {
        mockMvc.perform(
                post("/api/v1/auto-transfer-schedules")
                        .header(
                                "Authorization",
                                "Bearer access-token"
                        )
                        .header(
                                "Idempotency-Key",
                                UUID.randomUUID().toString()
                        )
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(
                                """
                                        {
                                          "child_id": 6,
                                          "frequency": "MONTHLY"
                                        }
                                        """
                        )
        ).andExpect(status().isBadRequest());
    }

    @Test
    void 멱등키가_없으면_400을_반환한다()
            throws Exception {
        given(memberResolver.resolveMemberId(
                "Bearer access-token"
        )).willReturn(7L);

        given(service.createSchedule(
                eq(7L),
                isNull(),
                any()
        )).willThrow(
                new BusinessException(ErrorCode.BADREQUEST)
        );

        mockMvc.perform(
                post("/api/v1/auto-transfer-schedules")
                        .header(
                                "Authorization",
                                "Bearer access-token"
                        )
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(
                                """
                                        {
                                          "child_id": 6,
                                          "source_account_id": 1,
                                          "destination_account_id": 12,
                                          "amount": 80000,
                                          "frequency": "MONTHLY",
                                          "transfer_day": 10,
                                          "start_date": "2026-09-10"
                                        }
                                        """
                        )
        ).andExpect(status().isBadRequest());
    }

    @Test
    void 자녀_자동이체_일정_목록을_조회한다()
            throws Exception {
        given(memberResolver.resolveMemberId(
                "Bearer access-token"
        )).willReturn(1L);

        AutoTransferScheduleListItemResponse item =
                new AutoTransferScheduleListItemResponse(
                        21L,
                        1L,
                        "대학자금 마련",
                        new BigDecimal("80000"),
                        AutoTransferFrequency.MONTHLY,
                        10,
                        Instant.parse(
                                "2026-09-10T00:00:00Z"
                        ),
                        null,
                        null,
                        AutoTransferScheduleStatus.ACTIVE
                );

        given(service.getSchedules(
                1L,
                1L,
                "ACTIVE",
                null,
                20
        )).willReturn(
                new AutoTransferScheduleListResponse(
                        List.of(item),
                        null,
                        false
                )
        );

        mockMvc.perform(
                        get(
                                "/api/v1/children/1/"
                                        + "auto-transfer-schedules"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer access-token"
                                )
                                .param("status", "ACTIVE")
                                .param("size", "20")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.items[0].auto_transfer_schedule_id"
                ).value(21))
                .andExpect(jsonPath(
                        "$.items[0].financial_goal_id"
                ).value(1))
                .andExpect(jsonPath(
                        "$.items[0].goal_title"
                ).value("대학자금 마련"))
                .andExpect(jsonPath(
                        "$.items[0].next_transfer_at"
                ).value("2026-09-10T00:00:00Z"))
                .andExpect(jsonPath(
                        "$.items[0].status"
                ).value("ACTIVE"))
                .andExpect(jsonPath(
                        "$.next_cursor"
                ).doesNotExist())
                .andExpect(jsonPath(
                        "$.has_next"
                ).value(false));
    }
}