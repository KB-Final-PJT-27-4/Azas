package com.azas.domain.notification.controller;

import com.azas.domain.notification.dto.NotificationUnreadCountResponse;
import com.azas.domain.notification.service.NotificationService;
import com.azas.global.exception.GlobalExceptionHandler;
import com.azas.global.security.AccessTokenMemberResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class NotificationUnreadCountControllerTest {

    private static final long MEMBER_ID = 1L;

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

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(
                        new GlobalExceptionHandler()
                )
                .build();
    }

    @Test
    void getsUnreadNotificationCount() throws Exception {
        when(accessTokenMemberResolver.resolveMemberId(
                "Bearer access-token"
        )).thenReturn(MEMBER_ID);

        when(notificationService.getUnreadCount(MEMBER_ID))
                .thenReturn(
                        new NotificationUnreadCountResponse(3L)
                );

        mockMvc.perform(
                        get(
                                "/api/v1/notifications/unread-count"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer access-token"
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
                        jsonPath("$.unread_count")
                                .value(3)
                );

        verify(accessTokenMemberResolver)
                .resolveMemberId(
                        "Bearer access-token"
                );

        verify(notificationService)
                .getUnreadCount(MEMBER_ID);
    }

    @Test
    void returnsZeroWhenNoUnreadNotificationExists()
            throws Exception {
        when(accessTokenMemberResolver.resolveMemberId(
                "Bearer access-token"
        )).thenReturn(MEMBER_ID);

        when(notificationService.getUnreadCount(MEMBER_ID))
                .thenReturn(
                        new NotificationUnreadCountResponse(0L)
                );

        mockMvc.perform(
                        get(
                                "/api/v1/notifications/unread-count"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer access-token"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.unread_count")
                                .value(0)
                );
    }
}