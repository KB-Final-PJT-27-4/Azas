package com.azas.domain.notification.dto;

import com.azas.domain.notification.entity.NotificationCategory;
import com.azas.domain.notification.entity.NotificationType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class NotificationListItemResponse {

    @JsonProperty("notification_id")
    private final Long notificationId;

    @JsonProperty("child_id")
    private final Long childId;

    @JsonProperty("notification_category")
    private final NotificationCategory notificationCategory;

    @JsonProperty("notification_type")
    private final NotificationType notificationType;

    private final String title;
    private final String content;

    @JsonProperty("reference_type")
    private final String referenceType;

    @JsonProperty("reference_id")
    private final Long referenceId;

    private final JsonNode metadata;

    @JsonProperty("is_read")
    private final boolean read;

    @JsonProperty("created_at")
    private final LocalDateTime createdAt;
}