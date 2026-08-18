package com.azas.domain.notification.service;

import com.azas.domain.notification.dto.NotificationPreferenceListResponse;
import com.azas.domain.notification.dto.UpdateNotificationPreferencesRequest;

public interface NotificationPreferenceService {

    NotificationPreferenceListResponse
    getNotificationPreferences(Long memberId);

    NotificationPreferenceListResponse updateNotificationPreferences(
            Long memberId,
            UpdateNotificationPreferencesRequest request
    );
}