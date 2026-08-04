package com.azas.domain.timecapsule.controller;

import com.azas.domain.timecapsule.dto.TimeCapsuleEntryListResponse;
import com.azas.domain.timecapsule.dto.TimeCapsuleEntryResponse;
import com.azas.domain.timecapsule.dto.TimeCapsuleEntrySummaryResponse;
import com.azas.domain.timecapsule.entity.AccountTransactionDirection;
import com.azas.domain.timecapsule.entity.TimeCapsuleEntry;
import com.azas.domain.timecapsule.entity.TimeCapsuleEntryMediaMode;
import com.azas.domain.timecapsule.entity.TimeCapsuleEntryTransaction;
import com.azas.domain.timecapsule.service.AccessTokenMemberResolver;
import com.azas.domain.timecapsule.service.TimeCapsuleEntryService;
import com.azas.domain.timecapsule.service.TimeCapsuleService;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TimeCapsuleEntryControllerTest {

    @Mock
    private AccessTokenMemberResolver accessTokenMemberResolver;

    @Mock
    private TimeCapsuleService timeCapsuleService;

    @Mock
    private TimeCapsuleEntryService timeCapsuleEntryService;

    @InjectMocks
    private TimeCapsuleController timeCapsuleController;

    private MockMvc mockMvc;

    @BeforeEach
    // [JMG] CAPSULE-4~5 기록 컨트롤러의 검증과 JSON 응답 설정을 구성한다.
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        LocalValidatorFactoryBean validator =
                new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(timeCapsuleController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setMessageConverters(
                        new MappingJackson2HttpMessageConverter(objectMapper)
                )
                .build();
    }

    @Test
    // [JMG] CAPSULE-4 기록 목록 URL은 인증된 부모에게 목록 응답 계약을 반환한다.
    void getTimeCapsuleEntriesReturnsEntryList() throws Exception {
        TimeCapsuleEntry entry = createEntry(1000L, "IMAGE");
        ReflectionTestUtils.setField(entry, "mediaCount", 2);
        TimeCapsuleEntryListResponse response =
                new TimeCapsuleEntryListResponse(
                        List.of(TimeCapsuleEntrySummaryResponse.from(entry))
                );

        given(accessTokenMemberResolver.resolveMemberId(
                "Bearer access-token"
        )).willReturn(7L);
        given(timeCapsuleEntryService.getTimeCapsuleEntries(7L, 100L))
                .willReturn(response);

        mockMvc.perform(
                        get("/api/v1/time-capsules/{timeCapsuleId}/entries", 100L)
                                .header(
                                        "Authorization",
                                        "Bearer access-token"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entries[0].time_capsule_entry_id")
                        .value(1000))
                .andExpect(jsonPath("$.entries[0].media_mode")
                        .value("IMAGE"))
                .andExpect(jsonPath("$.entries[0].media_count").value(2));
    }

    @Test
    // [JMG] CAPSULE-5 기록 생성 URL은 유효한 요청에 201과 생성된 기록 스냅샷을 반환한다.
    void createTimeCapsuleEntryReturnsCreatedResponse() throws Exception {
        TimeCapsuleEntry entry = createEntry(1000L, "VIDEO");
        TimeCapsuleEntryResponse response = TimeCapsuleEntryResponse.from(
                entry
        );

        given(accessTokenMemberResolver.resolveMemberId(
                "Bearer access-token"
        )).willReturn(7L);
        given(timeCapsuleEntryService.createTimeCapsuleEntry(
                eq(7L),
                eq(100L),
                any()
        )).willReturn(response);

        mockMvc.perform(
                        post(
                                "/api/v1/time-capsules/{timeCapsuleId}/entries",
                                100L
                        )
                                .header(
                                        "Authorization",
                                        "Bearer access-token"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "account_transaction_id": 901,
                                          "title": "첫 생일 축하",
                                          "message": "오늘도 저축했어.",
                                          "media_mode": "VIDEO"
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.time_capsule_entry_id")
                        .value(1000))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.contribution_amount")
                        .value(150000))
                .andExpect(jsonPath("$.contributed_at")
                        .value("2026-07-15T10:00:00"))
                .andExpect(jsonPath("$.media_mode").value("VIDEO"));
    }

    @Test
    // [JMG] CAPSULE-5 필수 메시지가 빠진 요청은 서비스 호출 전 400 오류로 검증한다.
    void createTimeCapsuleEntryRejectsMissingMessage() throws Exception {
        mockMvc.perform(
                        post(
                                "/api/v1/time-capsules/{timeCapsuleId}/entries",
                                100L
                        )
                                .header(
                                        "Authorization",
                                        "Bearer access-token"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "account_transaction_id": 901,
                                          "title": "첫 생일 축하",
                                          "media_mode": "IMAGE"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("BADREQUEST"));
    }

    // [JMG] CAPSULE-4~5 테스트용 입금 거래를 연결한 기록 엔티티를 구성한다.
    private TimeCapsuleEntry createEntry(long entryId, String mediaMode) {
        TimeCapsuleEntryTransaction transaction =
                new TimeCapsuleEntryTransaction();
        ReflectionTestUtils.setField(
                transaction,
                "accountTransactionId",
                901L
        );
        ReflectionTestUtils.setField(
                transaction,
                "direction",
                AccountTransactionDirection.CREDIT
        );
        ReflectionTestUtils.setField(
                transaction,
                "amount",
                new BigDecimal("150000.00")
        );
        ReflectionTestUtils.setField(
                transaction,
                "occurredAt",
                LocalDateTime.of(2026, 7, 15, 10, 0)
        );

        TimeCapsuleEntry entry = TimeCapsuleEntry.create(
                100L,
                7L,
                transaction,
                "첫 생일 축하",
                "오늘도 저축했어.",
                TimeCapsuleEntryMediaMode.from(mediaMode)
        );
        ReflectionTestUtils.setField(
                entry,
                "timeCapsuleEntryId",
                entryId
        );
        return entry;
    }
}
