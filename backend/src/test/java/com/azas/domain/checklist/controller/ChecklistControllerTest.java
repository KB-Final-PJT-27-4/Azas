package com.azas.domain.checklist.controller;

import com.azas.domain.checklist.service.ChecklistItemCompletionService;
import com.azas.domain.checklist.service.ChecklistItemListService;
import com.azas.global.exception.GlobalExceptionHandler;
import com.azas.global.security.AccessTokenMemberResolver;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;


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
}