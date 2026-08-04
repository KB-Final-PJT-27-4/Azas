package com.azas.domain.timecapsule.dto;

import com.azas.domain.timecapsule.entity.TimeCapsuleStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class TimeCapsuleSearchCondition {

    private final long childId;
    private final TimeCapsuleStatus status;
    private final LocalDateTime cursorAt;
    private final Long cursorId;
    private final int limit;
    private final LocalDateTime calendarStart;
    private final LocalDateTime calendarEnd;

    private TimeCapsuleSearchCondition(
            long childId,
            TimeCapsuleStatus status,
            LocalDateTime cursorAt,
            Long cursorId,
            int limit,
            LocalDateTime calendarStart,
            LocalDateTime calendarEnd
    ) {
        this.childId = childId;
        this.status = status;
        this.cursorAt = cursorAt;
        this.cursorId = cursorId;
        this.limit = limit;
        this.calendarStart = calendarStart;
        this.calendarEnd = calendarEnd;
    }

    // [JMG] CAPSULE-2 카드형 목록의 keyset pagination 조건을 만든다.
    public static TimeCapsuleSearchCondition forCard(
            long childId,
            TimeCapsuleStatus status,
            TimeCapsuleCursor cursor,
            int limit
    ) {
        return new TimeCapsuleSearchCondition(
                childId,
                status,
                cursor == null ? null : cursor.getSortAt(),
                cursor == null ? null : cursor.getTimeCapsuleId(),
                limit,
                null,
                null
        );
    }

    // [JMG] CAPSULE-2 캘린더형 목록의 월 범위와 keyset pagination 조건을 만든다.
    public static TimeCapsuleSearchCondition forCalendar(
            long childId,
            TimeCapsuleStatus status,
            TimeCapsuleCursor cursor,
            int limit,
            LocalDateTime calendarStart,
            LocalDateTime calendarEnd
    ) {
        return new TimeCapsuleSearchCondition(
                childId,
                status,
                cursor == null ? null : cursor.getSortAt(),
                cursor == null ? null : cursor.getTimeCapsuleId(),
                limit,
                calendarStart,
                calendarEnd
        );
    }
}
