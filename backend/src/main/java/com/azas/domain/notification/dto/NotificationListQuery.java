package com.azas.domain.notification.dto;

import com.azas.domain.notification.entity.NotificationCategory;
import com.azas.domain.notification.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NotificationListQuery {

    private final Long memberId;
    private final Long childId;
    private final NotificationCategory category;
    private final NotificationType notificationType;
    private final Boolean isRead;

    // 과거 알림 페이지 조회: notification_id < cursorId
    private final Long cursorId;

    // 신규 알림 폴링: notification_id > afterId
    private final Long afterId;

    private final int limit;
}