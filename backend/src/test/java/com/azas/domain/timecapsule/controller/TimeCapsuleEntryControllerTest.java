package com.azas.domain.timecapsule.controller;

import com.azas.domain.timecapsule.dto.TimeCapsuleEntryListResponse;
import com.azas.domain.timecapsule.dto.TimeCapsuleEntrySealResponse;
import com.azas.domain.timecapsule.dto.TimeCapsuleEntrySummaryResponse;
import com.azas.domain.timecapsule.dto.TimeCapsuleEntryUpdateResponse;
import com.azas.domain.timecapsule.entity.AccountTransactionDirection;
import com.azas.domain.timecapsule.entity.TimeCapsuleEntry;
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
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
    // [JMG] CAPSULE-4 ISO 날짜 형식을 적용한 엔트리 목록 컨트롤러 테스트 환경을 구성한다.
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders
                .standaloneSetup(timeCapsuleController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(
                        new MappingJackson2HttpMessageConverter(objectMapper)
                )
                .build();
    }

    @Test
    // [JMG] CAPSULE-4 엔트리 목록 URL은 프런트 계약에 맞는 안전한 필드만 반환한다.
    void getTimeCapsuleEntriesReturnsEntryList() throws Exception {
        TimeCapsuleEntry entry = createEntry(1000L);
        ReflectionTestUtils.setField(entry, "mediaCount", 2);
        TimeCapsuleEntryListResponse response =
                new TimeCapsuleEntryListResponse(
                        List.of(TimeCapsuleEntrySummaryResponse.from(entry))
                );

        given(accessTokenMemberResolver.resolveMemberId("Bearer access-token"))
                .willReturn(7L);
        given(timeCapsuleEntryService.getTimeCapsuleEntries(7L, 100L))
                .willReturn(response);

        mockMvc.perform(
                        get("/api/v1/time-capsules/{timeCapsuleId}/entries", 100L)
                                .header("Authorization", "Bearer access-token")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entries[0].time_capsule_entry_id")
                        .value(1000))
                .andExpect(jsonPath("$.entries[0].media_mode")
                        .value("NONE"))
                .andExpect(jsonPath("$.entries[0].media_count").value(2))
                .andExpect(jsonPath("$.entries[0].thumbnail_object_key")
                        .doesNotExist())
                .andExpect(jsonPath("$.entries[0].thumbnail_url").isEmpty());
    }

    @Test
    // [JMG] CAPSULE-12 DRAFT 엔트리 수정 URL은 수정 결과를 프런트 계약대로 반환한다.
    void updateTimeCapsuleEntryReturnsUpdatedResponse() throws Exception {
        TimeCapsuleEntry entry = createEntry(1000L);
        ReflectionTestUtils.setField(entry, "title", "첫 저축 기록");
        ReflectionTestUtils.setField(
                entry,
                "message",
                "오늘부터 대학자금을 모으기 시작했어."
        );
        ReflectionTestUtils.setField(entry, "editCount", 1);

        given(accessTokenMemberResolver.resolveMemberId("Bearer access-token"))
                .willReturn(7L);
        given(timeCapsuleEntryService.updateTimeCapsuleEntry(
                org.mockito.ArgumentMatchers.eq(7L),
                org.mockito.ArgumentMatchers.eq(1000L),
                org.mockito.ArgumentMatchers.any()
        )).willReturn(TimeCapsuleEntryUpdateResponse.from(entry));

        mockMvc.perform(
                        patch("/api/v1/time-capsule-entries/{entryId}", 1000L)
                                .header("Authorization", "Bearer access-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "title": "첫 저축 기록",
                                          "message": "오늘부터 대학자금을 모으기 시작했어."
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.time_capsule_entry_id").value(1000))
                .andExpect(jsonPath("$.edit_count").value(1))
                .andExpect(jsonPath("$.title").value("첫 저축 기록"));
    }

    @Test
    // [JMG] CAPSULE-15 DRAFT 엔트리 봉인 URL은 봉인 결과를 프런트 계약대로 반환한다.
    void sealTimeCapsuleEntryReturnsSealedResponse() throws Exception {
        TimeCapsuleEntry entry = createEntry(1000L);
        ReflectionTestUtils.setField(entry, "status",
                com.azas.domain.timecapsule.entity.TimeCapsuleEntryStatus.SEALED);
        ReflectionTestUtils.setField(entry, "sealedAt",
                LocalDateTime.of(2026, 8, 5, 11, 40));

        given(accessTokenMemberResolver.resolveMemberId("Bearer access-token"))
                .willReturn(7L);
        given(timeCapsuleEntryService.sealTimeCapsuleEntry(7L, 1000L))
                .willReturn(TimeCapsuleEntrySealResponse.from(entry));

        mockMvc.perform(
                        patch("/api/v1/time-capsule-entries/{entryId}/seal", 1000L)
                                .header("Authorization", "Bearer access-token")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.time_capsule_entry_id").value(1000))
                .andExpect(jsonPath("$.status").value("SEALED"))
                .andExpect(jsonPath("$.sealed_at")
                        .value("2026-08-05T11:40:00"));
    }

    // [JMG] CAPSULE-4 테스트용 엔트리를 자동 초안 생성 규칙과 동일한 형태로 구성한다.
    private TimeCapsuleEntry createEntry(long entryId) {
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
                LocalDateTime.of(2026, 8, 5, 10, 30)
        );

        TimeCapsuleEntry entry =
                TimeCapsuleEntry.createDraftForSuccessfulTransfer(
                        100L,
                        7L,
                        transaction
                );
        ReflectionTestUtils.setField(entry, "timeCapsuleEntryId", entryId);
        return entry;
    }
}
