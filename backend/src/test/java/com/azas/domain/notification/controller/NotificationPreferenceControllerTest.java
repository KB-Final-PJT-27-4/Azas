package com.azas.domain.notification.controller;

import com.azas.domain.notification.dto.NotificationPreferenceItemResponse;
import com.azas.domain.notification.dto.NotificationPreferenceListResponse;
import com.azas.domain.notification.entity.NotificationCategory;
import com.azas.domain.notification.service.NotificationPreferenceService;
import com.azas.global.exception.GlobalExceptionHandler;
import com.azas.global.security.AccessTokenMemberResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class NotificationPreferenceControllerTest {

    private static final Long MEMBER_ID = 7L;

    private static final String AUTHORIZATION =
            "Bearer access-token";

    @Mock
    private NotificationPreferenceService
            notificationPreferenceService;

    @Mock
    private AccessTokenMemberResolver
            accessTokenMemberResolver;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        NotificationPreferenceController controller =
                new NotificationPreferenceController(
                        notificationPreferenceService,
                        accessTokenMemberResolver
                );

        mockMvc = standaloneSetup(controller)
                .setControllerAdvice(
                        new GlobalExceptionHandler()
                )
                .build();
    }

    @Test
    void getsNotificationPreferences() throws Exception {
        NotificationPreferenceItemResponse item =
                new NotificationPreferenceItemResponse(
                        NotificationCategory.SAVINGS,
                        "저축·자동이체 알림",
                        "저축 예정일과 자동이체 처리 결과를 알려드려요.",
                        true
                );

        when(accessTokenMemberResolver.resolveMemberId(
                AUTHORIZATION
        )).thenReturn(MEMBER_ID);

        when(notificationPreferenceService
                .getNotificationPreferences(MEMBER_ID))
                .thenReturn(
                        new NotificationPreferenceListResponse(
                                List.of(item)
                        )
                );

        mockMvc.perform(
                        get(
                                "/api/v1/notification-preferences"
                        )
                                .header(
                                        "Authorization",
                                        AUTHORIZATION
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        header().string(
                                "Cache-Control",
                                "no-store"
                        )
                )
                .andExpect(
                        jsonPath("$.items[0].notification_category")
                                .value("SAVINGS")
                )
                .andExpect(
                        jsonPath("$.items[0].label")
                                .value("저축·자동이체 알림")
                )
                .andExpect(
                        jsonPath("$.items[0].enabled")
                                .value(true)
                );

        verify(notificationPreferenceService)
                .getNotificationPreferences(MEMBER_ID);
    }
}