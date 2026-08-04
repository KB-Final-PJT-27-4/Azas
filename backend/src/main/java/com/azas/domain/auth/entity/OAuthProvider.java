package com.azas.domain.auth.entity;

import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;

import java.util.Locale;

public enum OAuthProvider {
    GOOGLE,
    KAKAO;

    public static OAuthProvider from(String value) {
        if (value == null) {
            throw new BusinessException(
                    ErrorCode.UNSUPPORTED_OAUTH_PROVIDER
            );
        }

        try {
            return valueOf(
                    value.trim().toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(
                    ErrorCode.UNSUPPORTED_OAUTH_PROVIDER,
                    exception
            );
        }
    }
}
