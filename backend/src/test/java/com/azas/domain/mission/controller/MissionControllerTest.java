package com.azas.domain.mission.controller;

import com.azas.domain.mission.dto.MissionCreateResponse;
import com.azas.domain.mission.entity.MissionStatus;
import com.azas.domain.mission.service.MissionService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
}