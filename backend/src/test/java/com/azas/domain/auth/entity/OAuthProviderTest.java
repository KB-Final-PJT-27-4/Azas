package com.azas.domain.auth.entity;

import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OAuthProviderTest {

    @ParameterizedTest
    @ValueSource(strings = {"google", "GOOGLE", " google "})
    void convertsGoogleProvider(String value) {
        OAuthProvider provider = OAuthProvider.from(value);

        assertEquals(OAuthProvider.GOOGLE, provider);
    }

    @ParameterizedTest
    @ValueSource(strings = {"kakao", "KAKAO", " kakao "})
    void convertsKakaoProvider(String value) {
        OAuthProvider provider = OAuthProvider.from(value);

        assertEquals(OAuthProvider.KAKAO, provider);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"naver", " "})
    void rejectsUnsupportedProvider(String value) {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> OAuthProvider.from(value)
        );

        assertEquals(
                ErrorCode.UNSUPPORTED_OAUTH_PROVIDER,
                exception.getErrorCode()
        );
    }
}
