package com.azas.domain.timecapsule.entity;

import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;

import java.util.Locale;

public enum TimeCapsuleView {
    CARD,
    CALENDAR;

    // [JMG] CAPSULE-2 목록 조회 화면 유형을 검증·변환한다.
    public static TimeCapsuleView from(String value) {
        if (value == null || value.isBlank()) {
            return CARD;
        }

        try {
            return TimeCapsuleView.valueOf(
                    value.toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.BADREQUEST);
        }
    }
}
