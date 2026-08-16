package com.azas.domain.notification.controller;

import com.azas.domain.notification.dto.NotificationReadResponse;
import com.azas.domain.notification.service.NotificationService;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
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
class NotificationReadControllerTest {

    private static final Long MEMBER_ID = 7L;
    private static final Long NOTIFICATION_ID = 101L;
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
    void readsNotification() throws Exception {
        when(accessTokenMemberResolver.resolveMemberId(AUTHORIZATION))
                .thenReturn(MEMBER_ID);

        when(notificationService.readNotification(
                MEMBER_ID,
                NOTIFICATION_ID
        )).thenReturn(
                new NotificationReadResponse(
                        NOTIFICATION_ID,
                        true
                )
        );

        mockMvc.perform(
                        patch(
                                "/api/v1/notifications/{notification_id}/read",
                                NOTIFICATION_ID
                        )
                                .header(
                                        "Authorization",
                                        AUTHORIZATION
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.notification_id")
                                .value(NOTIFICATION_ID)
                )
                .andExpect(
                        jsonPath("$.is_read")
                                .value(true)
                );

        verify(accessTokenMemberResolver)
                .resolveMemberId(AUTHORIZATION);

        verify(notificationService)
                .readNotification(
                        MEMBER_ID,
                        NOTIFICATION_ID
                );
    }

    @Test
    void returnsNotFoundWhenNotificationDoesNotExist()
            throws Exception {
        when(accessTokenMemberResolver.resolveMemberId(AUTHORIZATION))
                .thenReturn(MEMBER_ID);

        when(notificationService.readNotification(
                MEMBER_ID,
                NOTIFICATION_ID
        )).thenThrow(
                new BusinessException(
                        ErrorCode.NOTIFICATION_NOT_FOUND
                )
        );

        mockMvc.perform(
                        patch(
                                "/api/v1/notifications/{notification_id}/read",
                                NOTIFICATION_ID
                        )
                                .header(
                                        "Authorization",
                                        AUTHORIZATION
                                )
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.error.code")
                                .value("NOTIFICATION_NOT_FOUND")
                )
                .andExpect(
                        jsonPath("$.error.message")
                                .value("알림을 찾을 수 없습니다.")
                );
    }
}