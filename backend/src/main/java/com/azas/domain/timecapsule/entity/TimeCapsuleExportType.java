package com.azas.domain.timecapsule.entity;

import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;

import java.util.Locale;

public enum TimeCapsuleExportType {
    VIDEO,
    ARCHIVE;

    // [JMG] CAPSULE-11 요청 결과물 유형을 ERD 저장값으로 안전하게 변환한다.
    public static TimeCapsuleExportType from(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.BADREQUEST);
        }

        try {
            return TimeCapsuleExportType.valueOf(
                    value.trim().toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.BADREQUEST);
        }
    }
}
