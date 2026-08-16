package com.azas.domain.notification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NotificationReadAllResponse {

    @JsonProperty("updated_count")
    private final int updatedCount;

    @JsonProperty("unread_count")
    private final long unreadCount;
}