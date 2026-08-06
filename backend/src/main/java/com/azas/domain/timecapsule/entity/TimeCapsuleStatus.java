package com.azas.domain.timecapsule.entity;

import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;

import java.util.Locale;

public enum TimeCapsuleStatus {
    COLLECTING,
    RELEASED,
    ARCHIVED;

    // [JMG] CAPSULE-2 목록 조회 상태 파라미터를 ERD 상태값으로 변환한다.
    public static TimeCapsuleStatus from(String value) {
        try {
            return TimeCapsuleStatus.valueOf(
                    value.toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.BADREQUEST);
        }
    }
}
