package com.azas.domain.notification.dto;

import com.azas.domain.notification.entity.NotificationCategory;
import com.azas.domain.notification.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationListRow {

    private Long notificationId;
    private Long childId;
    private NotificationCategory notificationCategory;
    private NotificationType notificationType;
    private String title;
    private String content;
    private String referenceType;
    private Long referenceId;
    private String metadataJson;
    private boolean read;
    private LocalDateTime createdAt;
}