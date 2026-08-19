package com.azas.domain.checklist.controller;

import com.azas.domain.checklist.dto.ChecklistItemListResult;
import com.azas.domain.checklist.dto.ChecklistItemResult;
import com.azas.domain.checklist.entity.ChecklistItemStatus;
import com.azas.domain.checklist.entity.ChecklistLifecycleStage;
import com.azas.domain.checklist.service.ChecklistItemListService;
import com.azas.global.exception.GlobalExceptionHandler;
import com.azas.global.security.AccessTokenMemberResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ChecklistControllerTest {

    private MockMvc mockMvc;
    private AccessTokenMemberResolver memberResolver;
    private ChecklistItemListService service;

    @BeforeEach
    void setUp() {
        memberResolver =
                mock(AccessTokenMemberResolver.class);
        service =
                mock(ChecklistItemListService.class);

        ChecklistController controller =
                new ChecklistController(
                        memberResolver,
                        service
                );

        mockMvc = org.springframework.test.web.servlet
                .setup.MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(
                        new GlobalExceptionHandler()
                )
                .build();
    }

    @Test
    void 생애주기_체크리스트를_조회한다()
            throws Exception {
        ChecklistItemResult pending =
                new ChecklistItemResult(
                        31L,
                        12L,
                        "갖고 싶은 물건의 가격 알아보기",
                        "가격표를 보며 돈의 크기를 이해해요.",
                        ChecklistItemStatus.PENDING,
                        false,
                        null
                );

        ChecklistItemResult completed =
                new ChecklistItemResult(
                        29L,
                        10L,
                        "사고 싶은 것과 필요한 것 구분해보기",
                        "아이와 소비 우선순위를 이야기해요.",
                        ChecklistItemStatus.COMPLETED,
                        true,
                        LocalDateTime.of(
                                2026, 8, 19, 9, 0
                        )
                );

        ChecklistItemListResult result =
                new ChecklistItemListResult(
                        6L,
                        ChecklistLifecycleStage.AGE_5_TO_7,
                        "5~7세 · 금융 습관 형성",
                        "소비와 저축의 차이를 함께 배워볼 차례예요.",
                        2,
                        1,
                        50,
                        false,
                        List.of(pending, completed)
                );

        when(memberResolver.resolveMemberId(anyString()))
                .thenReturn(7L);

        when(service.getChecklistItems(
                7L,
                6L,
                "AGE_5_TO_7"
        )).thenReturn(result);

        mockMvc.perform(
                        get(
                                "/api/v1/children/{child_id}/checklist-items",
                                6L
                        )
                                .header(
                                        "Authorization",
                                        "Bearer access-token"
                                )
                                .param(
                                        "stage",
                                        "AGE_5_TO_7"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.child_id").value(6)
                )
                .andExpect(
                        jsonPath("$.lifecycle_stage")
                                .value("AGE_5_TO_7")
                )
                .andExpect(
                        jsonPath("$.total_count").value(2)
                )
                .andExpect(
                        jsonPath("$.completed_count").value(1)
                )
                .andExpect(
                        jsonPath("$.progress_percent").value(50)
                )
                .andExpect(
                        jsonPath("$.stage_completed")
                                .value(false)
                )
                .andExpect(
                        jsonPath("$.items[0].status")
                                .value("PENDING")
                )
                .andExpect(
                        jsonPath("$.items[1].status")
                                .value("COMPLETED")
                );
    }
}