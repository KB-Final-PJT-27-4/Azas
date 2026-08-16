package com.azas.domain.notification.service;

import com.azas.domain.notification.dto.NotificationListResponse;
import com.azas.domain.notification.dto.NotificationReadResponse;
import com.azas.domain.notification.dto.NotificationUnreadCountResponse;

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

    NotificationUnreadCountResponse getUnreadCount(
            Long memberId
    );

    NotificationReadResponse readNotification(
            Long memberId,
            Long notificationId
    );
}