package com.azas.domain.notification.controller;

import com.azas.domain.notification.dto.NotificationReadAllResponse;
import com.azas.domain.notification.service.NotificationService;
import com.azas.global.exception.GlobalExceptionHandler;
import com.azas.global.security.AccessTokenMemberResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class NotificationReadAllControllerTest {

    private static final Long MEMBER_ID = 7L;

    private static final String AUTHORIZATION =
            "Bearer access-token";

    @Mock
    private NotificationService notificationService;

    @Mock
    private AccessTokenMemberResolver accessTokenMemberResolver;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        NotificationController controller =
                new NotificationController(
                        notificationService,
                        accessTokenMemberResolver
                );

        mockMvc = standaloneSetup(controller)
                .setControllerAdvice(
                        new GlobalExceptionHandler()
                )
                .build();
    }

    @Test
    void readsAllNotifications() throws Exception {
        when(accessTokenMemberResolver.resolveMemberId(
                AUTHORIZATION
        )).thenReturn(MEMBER_ID);

        when(notificationService.readAllNotifications(
                MEMBER_ID
        )).thenReturn(
                new NotificationReadAllResponse(
                        3,
                        0L
                )
        );

        mockMvc.perform(
                        patch(
                                "/api/v1/notifications/read-all"
                        )
                                .header(
                                        "Authorization",
                                        AUTHORIZATION
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.updated_count")
                                .value(3)
                )
                .andExpect(
                        jsonPath("$.unread_count")
                                .value(0)
                );

        verify(notificationService)
                .readAllNotifications(MEMBER_ID);
    }
}