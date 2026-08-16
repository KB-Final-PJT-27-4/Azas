package com.azas.domain.notification.service;

import com.azas.domain.notification.dto.NotificationListResponse;

public interface NotificationService {

    NotificationListResponse getNotifications(
            Long memberId,
            Long childId,
            String category,
            String notificationType,
            String isRead,
            String cursor,
            String afterId,
            String size
    );
}