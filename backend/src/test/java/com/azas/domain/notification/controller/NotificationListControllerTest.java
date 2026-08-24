package com.azas.domain.notification.controller;

import com.azas.domain.notification.dto.NotificationListItemResponse;
import com.azas.domain.notification.dto.NotificationListResponse;
import com.azas.domain.notification.entity.NotificationCategory;
import com.azas.domain.notification.entity.NotificationType;
import com.azas.domain.notification.service.NotificationService;
import com.azas.global.exception.GlobalExceptionHandler;
import com.azas.global.security.AccessTokenMemberResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class NotificationListControllerTest {

    private static final long MEMBER_ID = 1L;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AccessTokenMemberResolver accessTokenMemberResolver;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        NotificationController controller = new NotificationController(
                notificationService,
                accessTokenMemberResolver
        );

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void returnsInitialPollingContractWithoutCaching()
            throws Exception {
        givenMember();
        when(notificationService.getNotifications(
                MEMBER_ID,
                null,
                null,
                null,
                null,
                null,
                null,
                "50"
        )).thenReturn(new NotificationListResponse(
                List.of(),
                null,
                false,
                0L,
                false,
                0L,
                5
        ));

        mockMvc.perform(
                        get("/api/v1/notifications")
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer access-token"
                                )
                                .param("size", "50")
                )
                .andExpect(status().isOk())
                .andExpect(header().string(
                        HttpHeaders.CACHE_CONTROL,
                        "no-store"
                ))
                .andExpect(header().string(
                        HttpHeaders.PRAGMA,
                        "no-cache"
                ))
                .andExpect(header().string(
                        HttpHeaders.VARY,
                        HttpHeaders.AUTHORIZATION
                ))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.poll_cursor").value(0))
                .andExpect(jsonPath("$.has_more_new").value(false))
                .andExpect(jsonPath("$.unread_count").value(0))
                .andExpect(jsonPath(
                        "$.recommended_poll_interval_seconds"
                ).value(5));
    }

    @Test
    void pollsOnlyNotificationsAfterLastReceivedId()
            throws Exception {
        givenMember();
        when(notificationService.getNotifications(
                MEMBER_ID,
                null,
                null,
                null,
                null,
                null,
                "41",
                "20"
        )).thenReturn(new NotificationListResponse(
                List.of(item(42L)),
                null,
                false,
                42L,
                true,
                3L,
                15
        ));

        mockMvc.perform(
                        get("/api/v1/notifications")
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer access-token"
                                )
                                .param("after_id", "41")
                                .param("size", "20")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.items[0].notification_id"
                ).value(42))
                .andExpect(jsonPath(
                        "$.items[0].notification_type"
                ).value("ALLOWANCE_REQUESTED"))
                .andExpect(jsonPath("$.poll_cursor").value(42))
                .andExpect(jsonPath("$.has_more_new").value(true))
                .andExpect(jsonPath("$.unread_count").value(3));

        verify(notificationService).getNotifications(
                MEMBER_ID,
                null,
                null,
                null,
                null,
                null,
                "41",
                "20"
        );
    }

    private void givenMember() {
        when(accessTokenMemberResolver.resolveMemberId(
                "Bearer access-token"
        )).thenReturn(MEMBER_ID);
    }

    private NotificationListItemResponse item(Long notificationId) {
        return new NotificationListItemResponse(
                notificationId,
                3L,
                NotificationCategory.ALLOWANCE,
                NotificationType.ALLOWANCE_REQUESTED,
                "자녀가 용돈을 요청했어요",
                "10,000원을 요청했어요.",
                "ALLOWANCE_REQUEST",
                41L,
                null,
                false,
                LocalDateTime.of(2026, 8, 23, 10, 30)
        );
    }
}
