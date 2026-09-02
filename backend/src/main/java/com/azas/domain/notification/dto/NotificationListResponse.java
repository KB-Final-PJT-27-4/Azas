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

    /**
     * 활성 화면에서 다음 폴링을 예약할 때 사용할 권장 간격이다.
     * PWA가 백그라운드에서 복귀하면 이 간격을 기다리지 않고
     * poll_cursor를 after_id로 전달해 즉시 누락분을 조회한다.
     */
    @JsonProperty("recommended_poll_interval_seconds")
    private final int recommendedPollIntervalSeconds;
}
