package com.azas.domain.notification.service;

import com.azas.domain.notification.dto.NotificationPreferenceListResponse;
import com.azas.domain.notification.dto.NotificationPreferenceRow;
import com.azas.domain.notification.entity.NotificationCategory;
import com.azas.domain.notification.mapper.NotificationPreferenceMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationPreferenceServiceTest {

    private static final Long MEMBER_ID = 7L;

    @Mock
    private NotificationPreferenceMapper
            notificationPreferenceMapper;

    private NotificationPreferenceService
            notificationPreferenceService;

    @BeforeEach
    void setUp() {
        notificationPreferenceService =
                new NotificationPreferenceServiceImpl(
                        notificationPreferenceMapper
                );
    }

    @Test
    void getsNotificationPreferencesInDisplayOrder() {
        when(notificationPreferenceMapper
                .findNotificationPreferences(MEMBER_ID))
                .thenReturn(List.of(
                        new NotificationPreferenceRow(
                                NotificationCategory.USAGE_LIMIT,
                                false
                        ),
                        new NotificationPreferenceRow(
                                NotificationCategory.SAVINGS,
                                true
                        )
                ));

        NotificationPreferenceListResponse response =
                notificationPreferenceService
                        .getNotificationPreferences(MEMBER_ID);

        assertEquals(6, response.getItems().size());

        assertEquals(
                NotificationCategory.SAVINGS,
                response.getItems()
                        .get(0)
                        .getNotificationCategory()
        );

        assertTrue(
                response.getItems()
                        .get(0)
                        .isEnabled()
        );

        assertEquals(
                NotificationCategory.USAGE_LIMIT,
                response.getItems()
                        .get(4)
                        .getNotificationCategory()
        );

        assertFalse(
                response.getItems()
                        .get(4)
                        .isEnabled()
        );

        verify(notificationPreferenceMapper)
                .findNotificationPreferences(MEMBER_ID);
    }

    @Test
    void returnsDefaultEnabledWhenPreferenceDoesNotExist() {
        when(notificationPreferenceMapper
                .findNotificationPreferences(MEMBER_ID))
                .thenReturn(List.of());

        NotificationPreferenceListResponse response =
                notificationPreferenceService
                        .getNotificationPreferences(MEMBER_ID);

        assertEquals(6, response.getItems().size());

        assertTrue(
                response.getItems()
                        .stream()
                        .allMatch(item -> item.isEnabled())
        );
    }
}