package com.azas.domain.notification.dto;

import com.azas.domain.notification.entity.NotificationCategory;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NotificationPreferenceItemResponse {

    @JsonProperty("notification_category")
    private final NotificationCategory notificationCategory;

    private final String label;

    private final String description;

    private final boolean enabled;
}