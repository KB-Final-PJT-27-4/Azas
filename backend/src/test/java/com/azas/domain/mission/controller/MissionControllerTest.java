package com.azas.domain.mission.controller;

import com.azas.domain.mission.dto.MissionCreateResponse;
import com.azas.domain.mission.entity.MissionStatus;
import com.azas.domain.mission.service.MissionService;

import com.azas.domain.mission.dto.MissionDetailResponse;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import com.azas.global.exception.GlobalExceptionHandler;
import com.azas.global.security.AccessTokenMemberResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MissionControllerTest {

    private MockMvc mockMvc;
    private MissionService missionService;
    private AccessTokenMemberResolver memberResolver;

    @BeforeEach
    void setUp() {
        missionService = mock(MissionService.class);
        memberResolver =
                mock(AccessTokenMemberResolver.class);

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
                        new MissionController(
                                missionService,
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
    void 부모가_미션을_생성하면_201을_반환한다()
            throws Exception {
        given(memberResolver.resolveMemberId(
                "Bearer access-token"
        )).willReturn(7L);

        given(missionService.createMission(
                eq(7L),
                eq(6L),
                any()
        )).willReturn(
                new MissionCreateResponse(
                        71L,
                        6L,
                        "일주일 동안 방 정리하기",
                        "매일 자기 전에 책상과 바닥을 정리해요.",
                        new BigDecimal("5000"),
                        MissionStatus.ASSIGNED,
                        Instant.parse(
                                "2026-08-18T01:00:00Z"
                        )
                )
        );

        mockMvc.perform(
                        post(
                                "/api/v1/children/{child_id}/missions",
                                6L
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer access-token"
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                    {
                                      "title": "일주일 동안 방 정리하기",
                                      "description": "매일 자기 전에 책상과 바닥을 정리해요.",
                                      "reward_amount": 5000
                                    }
                                    """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mission_id")
                        .value(71))
                .andExpect(jsonPath("$.child_id")
                        .value(6))
                .andExpect(jsonPath("$.title")
                        .value("일주일 동안 방 정리하기"))
                .andExpect(jsonPath("$.description")
                        .value("매일 자기 전에 책상과 바닥을 정리해요."))
                .andExpect(jsonPath("$.reward_amount")
                        .value(5000))
                .andExpect(jsonPath("$.status")
                        .value("ASSIGNED"))
                .andExpect(jsonPath("$.created_at")
                        .value("2026-08-18T01:00:00Z"));

        verify(missionService).createMission(
                eq(7L),
                eq(6L),
                any()
        );
    }

    @Test
    void 미션_이름이_비어있으면_400을_반환한다()
            throws Exception {
        mockMvc.perform(
                        post(
                                "/api/v1/children/{child_id}/missions",
                                6L
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer access-token"
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                    {
                                      "title": " ",
                                      "description": "방을 정리해요.",
                                      "reward_amount": 5000
                                    }
                                    """)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void 미션_내용이_비어있으면_400을_반환한다()
            throws Exception {
        mockMvc.perform(
                        post(
                                "/api/v1/children/{child_id}/missions",
                                6L
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer access-token"
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                    {
                                      "title": "방 정리하기",
                                      "description": " ",
                                      "reward_amount": 5000
                                    }
                                    """)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void 완료_보상이_0원이면_400을_반환한다()
            throws Exception {
        mockMvc.perform(
                        post(
                                "/api/v1/children/{child_id}/missions",
                                6L
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer access-token"
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                    {
                                      "title": "방 정리하기",
                                      "description": "방과 책상을 정리해요.",
                                      "reward_amount": 0
                                    }
                                    """)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void 부모_권한이_없으면_403을_반환한다()
            throws Exception {
        given(memberResolver.resolveMemberId(
                "Bearer access-token"
        )).willReturn(99L);

        given(missionService.createMission(
                eq(99L),
                eq(6L),
                any()
        )).willThrow(
                new BusinessException(
                        ErrorCode.PARENT_ACCESS_REQUIRED
                )
        );

        mockMvc.perform(
                        post(
                                "/api/v1/children/{child_id}/missions",
                                6L
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer access-token"
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                    {
                                      "title": "방 정리하기",
                                      "description": "방과 책상을 정리해요.",
                                      "reward_amount": 5000
                                    }
                                    """)
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code")
                        .value("PARENT_ACCESS_REQUIRED"));
    }

    // 상세 조회 성공 테스트
    @Test
    void 부모가_미션_상세를_조회한다()
            throws Exception {
        given(memberResolver.resolveMemberId(
                "Bearer access-token"
        )).willReturn(7L);

        given(missionService.getMissionDetail(
                7L,
                13L
        )).willReturn(
                new MissionDetailResponse(
                        13L,
                        6L,
                        "소비 계획 지키기",
                        "이번 주 계획한 소비 지키기",
                        new BigDecimal("2000"),
                        MissionStatus.SUBMITTED,
                        Instant.parse(
                                "2026-08-18T01:00:00Z"
                        ),
                        Instant.parse(
                                "2026-08-18T02:00:00Z"
                        )
                )
        );

        mockMvc.perform(
                        get(
                                "/api/v1/missions/{mission_id}",
                                13L
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer access-token"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.mission_id"
                ).value(13))
                .andExpect(jsonPath(
                        "$.child_id"
                ).value(6))
                .andExpect(jsonPath(
                        "$.title"
                ).value("소비 계획 지키기"))
                .andExpect(jsonPath(
                        "$.description"
                ).value("이번 주 계획한 소비 지키기"))
                .andExpect(jsonPath(
                        "$.reward_amount"
                ).value(2000))
                .andExpect(jsonPath(
                        "$.status"
                ).value("SUBMITTED"))
                .andExpect(jsonPath(
                        "$.created_at"
                ).value("2026-08-18T01:00:00Z"))
                .andExpect(jsonPath(
                        "$.updated_at"
                ).value("2026-08-18T02:00:00Z"));

        verify(missionService).getMissionDetail(
                7L,
                13L
        );
    }

    // 미션 없음 테스트
    @Test
    void 존재하지_않는_미션은_404를_반환한다()
            throws Exception {
        given(memberResolver.resolveMemberId(
                "Bearer access-token"
        )).willReturn(7L);

        given(missionService.getMissionDetail(
                7L,
                999L
        )).willThrow(
                new BusinessException(
                        ErrorCode.MISSION_NOT_FOUND
                )
        );

        mockMvc.perform(
                        get(
                                "/api/v1/missions/{mission_id}",
                                999L
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer access-token"
                                )
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath(
                        "$.error.code"
                ).value("MISSION_NOT_FOUND"));
    }

    // 자녀 완료
    @Test
    void 자녀가_미션_완료를_요청한다()
            throws Exception {
        given(memberResolver.resolveMemberId(
                "Bearer child-token"
        )).willReturn(20L);

        given(missionService.updateMissionStatus(
                eq(20L),
                eq(13L),
                any()
        )).willReturn(
                new MissionDetailResponse(
                        13L,
                        6L,
                        "소비 계획 지키기",
                        "이번 주 계획한 소비 지키기",
                        new BigDecimal("2000"),
                        MissionStatus.SUBMITTED,
                        Instant.parse(
                                "2026-08-18T01:00:00Z"
                        ),
                        Instant.parse(
                                "2026-08-18T03:00:00Z"
                        )
                )
        );

        mockMvc.perform(
                        patch(
                                "/api/v1/missions/{mission_id}",
                                13L
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer child-token"
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                {
                                  "action": "SUBMIT"
                                }
                                """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.mission_id"
                ).value(13))
                .andExpect(jsonPath(
                        "$.status"
                ).value("SUBMITTED"));
    }

    // 부모 승인
    @Test
    void 부모가_미션을_승인하고_보상한다()
            throws Exception {
        given(memberResolver.resolveMemberId(
                "Bearer parent-token"
        )).willReturn(7L);

        given(missionService.updateMissionStatus(
                eq(7L),
                eq(13L),
                any()
        )).willReturn(
                new MissionDetailResponse(
                        13L,
                        6L,
                        "소비 계획 지키기",
                        "이번 주 계획한 소비 지키기",
                        new BigDecimal("2000"),
                        MissionStatus.APPROVED,
                        Instant.parse(
                                "2026-08-18T01:00:00Z"
                        ),
                        Instant.parse(
                                "2026-08-18T03:00:00Z"
                        )
                )
        );

        mockMvc.perform(
                        patch(
                                "/api/v1/missions/{mission_id}",
                                13L
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer parent-token"
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                {
                                  "action": "APPROVE",
                                  "source_account_id": 1,
                                  "destination_account_id": 12
                                }
                                """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.status"
                ).value("APPROVED"));
    }
    // 승인 계좌 누락
    @Test
    void 승인할_때_보상_계좌가_없으면_400을_반환한다()
            throws Exception {
        given(memberResolver.resolveMemberId(
                "Bearer parent-token"
        )).willReturn(7L);

        given(missionService.updateMissionStatus(
                eq(7L),
                eq(13L),
                any()
        )).willThrow(
                new BusinessException(
                        ErrorCode.INVALID_MISSION_ACTION
                )
        );

        mockMvc.perform(
                        patch(
                                "/api/v1/missions/{mission_id}",
                                13L
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer parent-token"
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                {
                                  "action": "APPROVE"
                                }
                                """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(
                        "$.error.code"
                ).value("INVALID_MISSION_ACTION"));
    }
}