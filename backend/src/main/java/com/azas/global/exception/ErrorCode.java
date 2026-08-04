package com.azas.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    BADREQUEST(
            HttpStatus.BAD_REQUEST,
            "요청 값이 올바르지 않습니다."
    ),
    UNSUPPORTED_OAUTH_PROVIDER(
            HttpStatus.BAD_REQUEST,
            "지원하지 않는 소셜 로그인 제공자입니다."
    ),
    INVALID_AUTHORIZATION_CODE(
            HttpStatus.UNAUTHORIZED,
            "인가 코드가 만료되었거나 유효하지 않습니다."
    ),
    INVALID_REFRESH_TOKEN(
            HttpStatus.UNAUTHORIZED,
            "유효하지 않거나 만료된 Refresh Token입니다."
    ),
    WITHDRAWN_MEMBER(
            HttpStatus.UNAUTHORIZED,
            "탈퇴한 회원은 로그인할 수 없습니다."
    ),
    OAUTH_PROVIDER_ERROR(
            HttpStatus.BAD_GATEWAY,
            "소셜 제공자와 통신하는 중 오류가 발생했습니다."
    ),
    INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "서버 오류가 발생했습니다."
    );

    private final HttpStatus httpStatus;
    private final String message;
}
