package com.azas.domain.notification.service;

import com.azas.domain.notification.dto.NotificationPreferenceListResponse;

public interface NotificationPreferenceService {

    NotificationPreferenceListResponse
    getNotificationPreferences(Long memberId);
}