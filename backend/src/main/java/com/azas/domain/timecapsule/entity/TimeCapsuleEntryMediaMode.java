package com.azas.domain.timecapsule.entity;

import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;

import java.util.Locale;

public enum TimeCapsuleEntryMediaMode {
    NONE,
    IMAGE,
    VIDEO;

    // [JMG] CAPSULE-5 요청 미디어 유형을 ERD의 기록 미디어 유형으로 안전하게 변환한다.
    public static TimeCapsuleEntryMediaMode from(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.BADREQUEST);
        }

        try {
            return TimeCapsuleEntryMediaMode.valueOf(
                    value.toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.BADREQUEST);
        }
    }
}
