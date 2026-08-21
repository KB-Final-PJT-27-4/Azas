package com.azas.domain.checklist.controller;

import com.azas.domain.checklist.dto.ChecklistChildLifecycleRow;
import com.azas.domain.checklist.service.ChecklistItemCompletionService;
import com.azas.domain.checklist.service.ChecklistItemListService;
import com.azas.domain.child.entity.BirthStatus;
import com.azas.global.exception.GlobalExceptionHandler;
import com.azas.global.security.AccessTokenMemberResolver;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import com.azas.domain.checklist.dto.ChecklistInfoItemResult;
import com.azas.domain.checklist.dto.ChecklistItemListResult;
import com.azas.domain.checklist.dto.ChecklistItemResult;
import com.azas.domain.checklist.entity.ChecklistItemStatus;
import com.azas.domain.checklist.entity.ChecklistLifecycleStage;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ChecklistControllerTest {

    private MockMvc mockMvc;
    private ChecklistController checklistController;

    @Mock
    private AccessTokenMemberResolver memberResolver;

    @Mock
    private ChecklistItemListService service;

    @Mock
    private ChecklistItemCompletionService checklistItemCompletionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        checklistController =
                new ChecklistController(
                        memberResolver,
                        service,
                        checklistItemCompletionService
                );

        mockMvc = MockMvcBuilders
                .standaloneSetup(checklistController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void 체크리스트_목록에_URL과_상세_내용을_반환한다()
            throws Exception {

        when(memberResolver.resolveMemberId(
                "Bearer parent-access-token"
        )).thenReturn(4L);

        ChecklistInfoItemResult infoItem =
                new ChecklistInfoItemResult(
                        1L,
                        "첫만남 이용권",
                        "지원 내용을 확인해요.",
                        "대상·조건 확인하기",
                        "https://www.bokjiro.go.kr/ssis-tbu/index.do",
                        null
                );

        ChecklistItemResult item =
                new ChecklistItemResult(
                        101L,
                        1L,
                        "prenatal-support-after-birth",
                        "SUPPORT",
                        "출산 후 받을 수 있는 지원제도 확인하기",
                        "출산·육아 관련 지원제도를 미리 확인해요.",
                        "시기별 지원 정보를 먼저 확인해보세요.",
                        "INFO",
                        null,
                        "출산 후 받을 수 있는 지원제도 확인하기",
                        "지원 대상, 금액, 신청 기간은 달라질 수 있어요.",
                        ChecklistItemStatus.PENDING,
                        false,
                        null,
                        List.of(infoItem)
                );

        ChecklistItemListResult result =
                new ChecklistItemListResult(
                        6L,
                        ChecklistLifecycleStage.PREGNANCY,
                        "임신 중~출산 전 · 미래 준비",
                        "출산 전에 아이의 첫 금융 준비를 시작해보세요.",
                        1,
                        0,
                        0,
                        false,
                        List.of(item)
                );

        when(service.getChecklistItems(
                4L,
                6L,
                null
        )).thenReturn(result);

        mockMvc.perform(
                        get(
                                "/api/v1/children/6/checklist-items"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer parent-access-token"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.items[0].template_key")
                                .value(
                                        "prenatal-support-after-birth"
                                )
                )
                .andExpect(
                        jsonPath("$.items[0].category")
                                .value("SUPPORT")
                )
                .andExpect(
                        jsonPath("$.items[0].action_type")
                                .value("INFO")
                )
                .andExpect(
                        jsonPath("$.items[0].url")
                                .doesNotExist()
                )
                .andExpect(
                        jsonPath("$.items[0].content")
                                .value(
                                        "시기별 지원 정보를 먼저 확인해보세요."
                                )
                )
                .andExpect(
                        jsonPath(
                                "$.items[0].info_items[0].url"
                        ).value(
                                "https://www.bokjiro.go.kr/ssis-tbu/index.do"
                        )
                );

        verify(service).getChecklistItems(
                4L,
                6L,
                null
        );
    }
}