package com.azas.domain.notification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class NotificationListResponse {

    private final List<NotificationListItemResponse> items;

    @JsonProperty("next_cursor")
    private final Long nextCursor;

    @JsonProperty("has_next")
    private final boolean hasNext;

    @JsonProperty("poll_cursor")
    private final Long pollCursor;

    @JsonProperty("has_more_new")
    private final boolean hasMoreNew;

    @JsonProperty("unread_count")
    private final long unreadCount;
}