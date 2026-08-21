package com.azas.domain.timecapsule.entity;

import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;

import java.util.Locale;

public enum TimeCapsuleEntryMediaMode {
    NONE,
    IMAGE;

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
