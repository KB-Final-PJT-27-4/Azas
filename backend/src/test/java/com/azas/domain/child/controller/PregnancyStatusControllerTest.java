package com.azas.domain.child.controller;

import com.azas.domain.child.dto.PregnancyCharacterResponse;
import com.azas.domain.child.dto.PregnancyStatusResponse;
import com.azas.domain.child.service.PregnancyStatusService;
import com.azas.global.exception.GlobalExceptionHandler;
import com.azas.global.security.AccessTokenMemberResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class PregnancyStatusControllerTest {

    private static final Long MEMBER_ID = 7L;
    private static final Long CHILD_ID = 3L;

    private static final String AUTHORIZATION =
            "Bearer access-token";

    @Mock
    private PregnancyStatusService pregnancyStatusService;

    @Mock
    private AccessTokenMemberResolver accessTokenMemberResolver;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        PregnancyStatusController controller =
                new PregnancyStatusController(
                        pregnancyStatusService,
                        accessTokenMemberResolver
                );

        mockMvc = standaloneSetup(controller)
                .setControllerAdvice(
                        new GlobalExceptionHandler()
                )
                .build();
    }

    @Test
    void getsPregnancyStatus() throws Exception {
        PregnancyStatusResponse response =
                new PregnancyStatusResponse(
                        CHILD_ID,
                        "깨비",
                        LocalDate.of(2027, 1, 30),
                        LocalDate.of(2026, 8, 17),
                        16,
                        2,
                        166L,
                        new PregnancyCharacterResponse(
                                "AVOCADO",
                                "아보카도",
                                16
                        ),
                        new PregnancyCharacterResponse(
                                "MANGO",
                                "망고",
                                19
                        ),
                        19L
                );

        when(accessTokenMemberResolver.resolveMemberId(
                AUTHORIZATION
        )).thenReturn(MEMBER_ID);

        when(pregnancyStatusService.getPregnancyStatus(
                MEMBER_ID,
                CHILD_ID
        )).thenReturn(response);

        mockMvc.perform(
                        get(
                                "/api/v1/children/{child_id}/pregnancy-status",
                                CHILD_ID
                        )
                                .header(
                                        "Authorization",
                                        AUTHORIZATION
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.child_id")
                                .value(CHILD_ID)
                )
                .andExpect(
                        jsonPath("$.pregnancy_week")
                                .value(16)
                )
                .andExpect(
                        jsonPath("$.pregnancy_day")
                                .value(2)
                )
                .andExpect(
                        jsonPath("$.character.code")
                                .value("AVOCADO")
                )
                .andExpect(
                        jsonPath("$.next_character.code")
                                .value("MANGO")
                )
                .andExpect(
                        jsonPath("$.days_until_next_character")
                                .value(19)
                );
    }
}