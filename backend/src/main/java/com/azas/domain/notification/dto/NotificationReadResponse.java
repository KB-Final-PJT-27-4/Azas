package com.azas.domain.notification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NotificationReadResponse {

    @JsonProperty("notification_id")
    private final Long notificationId;

    @JsonProperty("is_read")
    private final boolean read;
}