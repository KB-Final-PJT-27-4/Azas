package com.azas.domain.timecapsule.controller;

import com.azas.domain.timecapsule.dto.CreateTimeCapsuleEntryResponse;
import com.azas.domain.timecapsule.dto.CompleteTimeCapsuleMediaUploadResponse;
import com.azas.domain.timecapsule.dto.CreateTimeCapsuleMediaUploadUrlResponse;
import com.azas.domain.timecapsule.dto.TimeCapsuleEntryDetailResponse;
import com.azas.domain.timecapsule.dto.TimeCapsuleEntryListResponse;
import com.azas.domain.timecapsule.dto.TimeCapsuleEntrySealResponse;
import com.azas.domain.timecapsule.dto.TimeCapsuleEntrySummaryResponse;
import com.azas.domain.timecapsule.dto.TimeCapsuleSummaryResponse;
import com.azas.domain.timecapsule.entity.AccountTransactionDirection;
import com.azas.domain.timecapsule.entity.TimeCapsule;
import com.azas.domain.timecapsule.entity.TimeCapsuleEntry;
import com.azas.domain.timecapsule.entity.TimeCapsuleEntryStatus;
import com.azas.domain.timecapsule.entity.TimeCapsuleEntryTransaction;
import com.azas.domain.timecapsule.entity.TimeCapsuleMedia;
import com.azas.domain.timecapsule.entity.TimeCapsuleMediaType;
import com.azas.domain.timecapsule.service.TimeCapsuleEntryService;
import com.azas.domain.timecapsule.service.TimeCapsuleService;
import com.azas.global.exception.GlobalExceptionHandler;
import com.azas.global.security.AccessTokenMemberResolver;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
    void getTimeCapsuleEntriesReturnsEntryList() throws Exception {
        TimeCapsuleEntry entry = createEntry(1000L);
        ReflectionTestUtils.setField(entry, "status",
                TimeCapsuleEntryStatus.SEALED);
        TimeCapsule timeCapsule = TimeCapsule.create(
                10L, 31L, "아이사랑적금", LocalDate.of(2027, 8, 8)
        );
        ReflectionTestUtils.setField(timeCapsule, "timeCapsuleId", 100L);
        ReflectionTestUtils.setField(
                timeCapsule, "totalContributionAmount",
                new BigDecimal("150000.00")
        );
        TimeCapsuleEntryListResponse response =
                new TimeCapsuleEntryListResponse(
                        TimeCapsuleSummaryResponse.from(
                                timeCapsule, LocalDate.of(2026, 8, 16)
                        ),
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
                .andExpect(jsonPath("$.time_capsule.time_capsule_id").value(100))
                .andExpect(jsonPath("$.time_capsule.d_day").value(357))
                .andExpect(jsonPath("$.time_capsule.dday").doesNotExist())
                .andExpect(jsonPath("$.total_count").value(1))
                .andExpect(jsonPath("$.entries[0].time_capsule_entry_id")
                        .value(1000));
    }

    @Test
    void getTimeCapsuleEntryReturnsReleasedEntryDetail() throws Exception {
        TimeCapsuleEntry entry = createEntry(1000L);
        ReflectionTestUtils.setField(entry, "status",
                TimeCapsuleEntryStatus.SEALED);
        ReflectionTestUtils.setField(entry, "sealedAt",
                LocalDateTime.of(2026, 8, 5, 11, 40));
        ReflectionTestUtils.setField(entry, "createdAt",
                LocalDateTime.of(2026, 8, 5, 10, 35));
        TimeCapsuleEntryDetailResponse response =
                new TimeCapsuleEntryDetailResponse(
                        entry,
                        1,
                        36,
                        new TimeCapsuleEntryDetailResponse.ImageResponse(
                                "https://storage.example/presigned-get",
                                LocalDateTime.of(2026, 8, 5, 12, 40)
                        )
                );

        given(accessTokenMemberResolver.resolveMemberId("Bearer access-token"))
                .willReturn(7L);
        given(timeCapsuleEntryService.getTimeCapsuleEntry(7L, 1000L))
                .willReturn(response);

        mockMvc.perform(
                        get("/api/v1/time-capsule-entries/{entryId}", 1000L)
                                .header("Authorization", "Bearer access-token")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.time_capsule_entry_id").value(1000))
                .andExpect(jsonPath("$.entry_number").value(1))
                .andExpect(jsonPath("$.total_entry_count").value(36))
                .andExpect(jsonPath("$.title").value("첫 용돈을 받은 날"))
                .andExpect(jsonPath("$.message")
                        .value("남은 돈은 꼭 저축하자."))
                .andExpect(jsonPath("$.contribution_amount").value(150000))
                .andExpect(jsonPath("$.image.url")
                        .value("https://storage.example/presigned-get"))
                .andExpect(jsonPath("$.image.download_url").doesNotExist())
                .andExpect(jsonPath("$.image.length()").value(2))
                .andExpect(jsonPath("$.author_member_id").doesNotExist())
                .andExpect(jsonPath("$.status").doesNotExist())
                .andExpect(jsonPath("$.sealed_at").doesNotExist())
                .andExpect(jsonPath("$.created_at").doesNotExist())
                .andExpect(jsonPath("$.media").doesNotExist())
                .andExpect(jsonPath("$.media_mode").doesNotExist())
                .andExpect(jsonPath("$.image.slot_no").doesNotExist());
    }

    @Test
    void deleteTimeCapsuleEntryReturnsNoContent() throws Exception {
        given(accessTokenMemberResolver.resolveMemberId(
                "Bearer access-token"
        )).willReturn(7L);

        mockMvc.perform(
                        delete(
                                "/api/v1/time-capsule-entries/{entryId}",
                                1000L
                        ).header(
                                "Authorization",
                                "Bearer access-token"
                        )
                )
                .andExpect(status().isNoContent());

        verify(timeCapsuleEntryService)
                .deleteTimeCapsuleEntry(7L, 1000L);
    }

    @Test
    void createTimeCapsuleEntryReturnsDraftResponse() throws Exception {
        TimeCapsuleEntry entry = createEntry(1000L);
        ReflectionTestUtils.setField(
                entry, "createdAt", LocalDateTime.of(2026, 8, 16, 14, 30)
        );
        given(accessTokenMemberResolver.resolveMemberId("Bearer access-token"))
                .willReturn(7L);
        given(timeCapsuleEntryService.createTimeCapsuleEntry(
                eq(7L), eq(100L), any()
        )).willReturn(CreateTimeCapsuleEntryResponse.from(entry));

        mockMvc.perform(
                        post("/api/v1/time-capsules/{timeCapsuleId}/entries", 100L)
                                .header("Authorization", "Bearer access-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "account_transaction_id": 901,
                                          "title": "첫 용돈을 받은 날",
                                          "message": "남은 돈은 꼭 저축하자."
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.time_capsule_entry_id").value(1000))
                .andExpect(jsonPath("$.time_capsule_id").value(100))
                .andExpect(jsonPath("$.account_transaction_id").value(901))
                .andExpect(jsonPath("$.contribution_amount").value(150000.00))
                .andExpect(jsonPath("$.contributed_at")
                        .value("2026-08-05T10:30:00"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.created_at")
                        .value("2026-08-16T14:30:00"));
    }

    @Test
    void createTimeCapsuleEntryRejectsInvalidRequest() throws Exception {
        mockMvc.perform(
                        post("/api/v1/time-capsules/{timeCapsuleId}/entries", 100L)
                                .header("Authorization", "Bearer access-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "account_transaction_id": 0,
                                          "title": " ",
                                          "message": " "
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void createMediaUploadUrlReturnsSingleImageUploadContract()
            throws Exception {
        CreateTimeCapsuleMediaUploadUrlResponse response =
                new CreateTimeCapsuleMediaUploadUrlResponse(
                        1000L,
                        2000L,
                        "https://storage.example/presigned-put",
                        LocalDateTime.of(2026, 8, 17, 15, 15),
                        Map.of("Content-Type", "image/jpeg")
                );
        given(accessTokenMemberResolver.resolveMemberId(
                "Bearer access-token"
        )).willReturn(7L);
        given(timeCapsuleEntryService.createMediaUploadUrl(
                eq(7L), eq(1000L), any()
        )).willReturn(response);

        mockMvc.perform(
                        post(
                                "/api/v1/time-capsule-entries/{entryId}/media/upload-url",
                                1000L
                        )
                                .header(
                                        "Authorization",
                                        "Bearer access-token"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "mime_type": "image/jpeg",
                                          "file_size": 1048576
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.time_capsule_entry_id")
                        .value(1000))
                .andExpect(jsonPath("$.time_capsule_media_id")
                        .value(2000))
                .andExpect(jsonPath("$.upload_url")
                        .value("https://storage.example/presigned-put"))
                .andExpect(jsonPath("$.expires_at")
                        .value("2026-08-17T15:15:00"))
                .andExpect(jsonPath("$['required_headers']['Content-Type']")
                        .value("image/jpeg"))
                .andExpect(jsonPath("$.uploads").doesNotExist())
                .andExpect(jsonPath("$.slot_no").doesNotExist());
    }

    @Test
    void completeMediaUploadReturnsSingleActiveImageContract()
            throws Exception {
        TimeCapsuleMedia media = TimeCapsuleMedia.createPendingUpload(
                1000L,
                TimeCapsuleMediaType.IMAGE,
                "time-capsules/100/entries/1000/media/slot-1.jpg",
                "image/jpeg",
                1048576L,
                1
        );
        ReflectionTestUtils.setField(
                media, "timeCapsuleMediaId", 2000L
        );
        media.activate();
        given(accessTokenMemberResolver.resolveMemberId(
                "Bearer access-token"
        )).willReturn(7L);
        given(timeCapsuleEntryService.completeMediaUpload(
                eq(7L), eq(1000L), any()
        )).willReturn(new CompleteTimeCapsuleMediaUploadResponse(media));

        mockMvc.perform(
                        post(
                                "/api/v1/time-capsule-entries/{entryId}/media/complete",
                                1000L
                        )
                                .header(
                                        "Authorization",
                                        "Bearer access-token"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "time_capsule_media_id": 2000
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.time_capsule_entry_id").value(1000))
                .andExpect(jsonPath("$.time_capsule_media_id").value(2000))
                .andExpect(jsonPath("$.media_status").value("ACTIVE"))
                .andExpect(jsonPath("$.media_mode").doesNotExist())
                .andExpect(jsonPath("$.media_count").doesNotExist())
                .andExpect(jsonPath("$.thumbnail_ready").doesNotExist())
                .andExpect(jsonPath("$.media").doesNotExist());
    }

    @Test
    void sealTimeCapsuleEntryReturnsSealedResponse() throws Exception {
        TimeCapsuleEntry entry = createEntry(1000L);
        ReflectionTestUtils.setField(entry, "status",
                TimeCapsuleEntryStatus.SEALED);
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

    private TimeCapsuleEntry createEntry(long entryId) {
        TimeCapsuleEntryTransaction transaction = new TimeCapsuleEntryTransaction();
        ReflectionTestUtils.setField(transaction, "accountTransactionId", 901L);
        ReflectionTestUtils.setField(
                transaction, "direction", AccountTransactionDirection.CREDIT
        );
        ReflectionTestUtils.setField(
                transaction, "amount", new BigDecimal("150000.00")
        );
        ReflectionTestUtils.setField(
                transaction, "occurredAt", LocalDateTime.of(2026, 8, 5, 10, 30)
        );
        TimeCapsuleEntry entry = TimeCapsuleEntry.createDraft(
                100L, 7L, transaction,
                "첫 용돈을 받은 날", "남은 돈은 꼭 저축하자."
        );
        ReflectionTestUtils.setField(entry, "timeCapsuleEntryId", entryId);
        return entry;
    }
}
