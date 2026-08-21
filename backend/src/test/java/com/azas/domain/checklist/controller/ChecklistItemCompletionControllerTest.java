package com.azas.domain.checklist.controller;

import com.azas.global.security.AccessTokenMemberResolver;
import com.azas.domain.checklist.dto.ChecklistItemCompletionResult;
import com.azas.domain.checklist.entity.ChecklistItemStatus;
import com.azas.domain.checklist.service.ChecklistItemCompletionService;
import com.azas.domain.checklist.service.ChecklistItemListService;
import com.azas.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class ChecklistItemCompletionControllerTest {

    private AccessTokenMemberResolver memberResolver;
    private ChecklistItemListService checklistItemListService;
    private ChecklistItemCompletionService completionService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        memberResolver = mock(AccessTokenMemberResolver.class);
        checklistItemListService =
                mock(ChecklistItemListService.class);
        completionService =
                mock(ChecklistItemCompletionService.class);

        ChecklistController controller =
                new ChecklistController(
                        memberResolver,
                        checklistItemListService,
                        completionService
                );

        mockMvc = standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void 체크리스트_항목을_완료한다() throws Exception {
        LocalDateTime completedAt =
                LocalDateTime.of(2026, 8, 19, 10, 30);

        when(memberResolver.resolveMemberId(anyString()))
                .thenReturn(7L);

        when(completionService.updateCompletion(
                7L,
                31L,
                true
        )).thenReturn(
                new ChecklistItemCompletionResult(
                        31L,
                        ChecklistItemStatus.COMPLETED,
                        true,
                        completedAt
                )
        );

        mockMvc.perform(
                        patch(
                                "/api/v1/checklist-items/31/completion"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer access-token"
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        "{\"completed\":true}"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.checklist_item_id")
                                .value(31)
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("COMPLETED")
                )
                .andExpect(
                        jsonPath("$.is_completed")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.completed_at")
                                .value("2026-08-19T10:30:00")
                );

        verify(completionService)
                .updateCompletion(7L, 31L, true);
    }

    @Test
    void 체크리스트_항목_완료를_취소한다() throws Exception {
        when(memberResolver.resolveMemberId(anyString()))
                .thenReturn(7L);

        when(completionService.updateCompletion(
                7L,
                31L,
                false
        )).thenReturn(
                new ChecklistItemCompletionResult(
                        31L,
                        ChecklistItemStatus.PENDING,
                        false,
                        null
                )
        );

        mockMvc.perform(
                        patch(
                                "/api/v1/checklist-items/31/completion"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer access-token"
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        "{\"completed\":false}"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.status")
                                .value("PENDING")
                )
                .andExpect(
                        jsonPath("$.is_completed")
                                .value(false)
                )
                .andExpect(
                        jsonPath("$.completed_at")
                                .doesNotExist()
                );
    }

    @Test
    void completed가_누락되면_400을_반환한다()
            throws Exception {
        mockMvc.perform(
                        patch(
                                "/api/v1/checklist-items/31/completion"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer access-token"
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("{}")
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(completionService);
    }
}