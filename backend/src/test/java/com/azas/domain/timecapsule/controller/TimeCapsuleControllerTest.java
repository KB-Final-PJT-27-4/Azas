package com.azas.domain.timecapsule.controller;

import com.azas.domain.timecapsule.dto.TimeCapsuleResponse;
import com.azas.domain.timecapsule.entity.TimeCapsule;
import com.azas.domain.timecapsule.service.AccessTokenMemberResolver;
import com.azas.domain.timecapsule.service.TimeCapsuleService;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import com.azas.global.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TimeCapsuleControllerTest {

    @Mock
    private AccessTokenMemberResolver accessTokenMemberResolver;

    @Mock
    private TimeCapsuleService timeCapsuleService;

    @InjectMocks
    private TimeCapsuleController timeCapsuleController;

    private MockMvc mockMvc;

    @BeforeEach
    // [JMG] CAPSULE-1 타임캡슐 컨트롤러의 요청 검증과 예외 응답 환경을 구성한다.
    void setUp() {
        ObjectMapper objectMapper =
                new ObjectMapper()
                        .registerModule(new JavaTimeModule())
                        .disable(
                                SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
                        );

        LocalValidatorFactoryBean validator =
                new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(timeCapsuleController)
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
    // [JMG] CAPSULE-1 보관함 생성 API는 정상 요청에 201과 ERD 응답을 반환한다.
    void createTimeCapsuleReturnsCreatedResponse() throws Exception {
        TimeCapsuleResponse response =
                TimeCapsuleResponse.from(
                        createTimeCapsule(
                                100L,
                                10L,
                                "깨비의 첫 대학자금 저축"
                        )
                );

        given(accessTokenMemberResolver.resolveMemberId(
                "Bearer access-token"
        )).willReturn(7L);
        given(timeCapsuleService.createTimeCapsule(
                eq(7L),
                eq(1L),
                any()
        )).willReturn(response);

        mockMvc.perform(
                        post(
                                "/api/v1/accounts/{accountId}/time-capsule",
                                1L
                        )
                                .header(
                                        "Authorization",
                                        "Bearer access-token"
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "title": "깨비의 첫 대학자금 저축"
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.time_capsule_id").value(100)
                )
                .andExpect(
                        jsonPath("$.financial_account_id").value(1)
                )
                .andExpect(
                        jsonPath("$.title")
                                .value("깨비의 첫 대학자금 저축")
                )
                .andExpect(
                        jsonPath("$.status").value("COLLECTING")
                );
    }

    @Test
    // [JMG] CAPSULE-1 Authorization 헤더가 없으면 401 표준 오류를 반환한다.
    void createTimeCapsuleReturnsUnauthorizedWithoutAccessToken()
            throws Exception {
        given(accessTokenMemberResolver.resolveMemberId(null))
                .willThrow(
                        new BusinessException(
                                ErrorCode.ACCESS_TOKEN_REQUIRED
                        )
                );

        mockMvc.perform(
                        post(
                                "/api/v1/accounts/{accountId}/time-capsule",
                                1L
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "title": "대학자금"
                                        }
                                        """)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(
                        jsonPath("$.error.code")
                                .value("ACCESS_TOKEN_REQUIRED")
                );
    }

    // [JMG] CAPSULE-1 테스트용 ERD 타임캡슐 응답 엔티티를 구성한다.
    private TimeCapsule createTimeCapsule(
            long timeCapsuleId,
            long childId,
            String title
    ) {
        TimeCapsule timeCapsule = TimeCapsule.create(
                childId,
                1L,
                title,
                LocalDate.of(2030, 7, 23)
        );
        ReflectionTestUtils.setField(
                timeCapsule,
                "timeCapsuleId",
                timeCapsuleId
        );
        ReflectionTestUtils.setField(
                timeCapsule,
                "createdAt",
                LocalDateTime.of(2026, 8, 4, 10, 0)
        );
        return timeCapsule;
    }
}
