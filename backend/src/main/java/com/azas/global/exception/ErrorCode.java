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
    WITHDRAWN_MEMBER(
            HttpStatus.UNAUTHORIZED,
            "탈퇴한 회원은 로그인할 수 없습니다."
    ),
    OAUTH_PROVIDER_ERROR(
            HttpStatus.BAD_GATEWAY,
            "소셜 제공자와 통신하는 중 오류가 발생했습니다."
    ),
    ACCESS_TOKEN_REQUIRED(
            HttpStatus.UNAUTHORIZED,
            "Access Token이 필요합니다."
    ),
    INVALID_ACCESS_TOKEN(
            HttpStatus.UNAUTHORIZED,
            "Access Token이 유효하지 않거나 만료되었습니다."
    ),
    TIME_CAPSULE_ACCESS_DENIED(
            HttpStatus.FORBIDDEN,
            "해당 타임캡슐에 접근할 권한이 없습니다."
    ),

    FINANCIAL_ACCOUNT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "금융 계좌를 찾을 수 없습니다."
    ),
    TIME_CAPSULE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "타임캡슐을 찾을 수 없습니다."
    ),
    DUPLICATE_TIME_CAPSULE(
            HttpStatus.CONFLICT,
            "해당 적금 계좌에는 이미 타임캡슐이 존재합니다."
    ),
    INELIGIBLE_TIME_CAPSULE_ACCOUNT(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "자녀 명의의 활성 적금 계좌만 타임캡슐로 등록할 수 있습니다."
    ),
    INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "서버 오류가 발생했습니다."
    ),

    // Child
    CHILD_NOT_FOUND(
            HttpStatus.NOT_FOUND,
        "자녀 정보를 찾을 수 없습니다."
    ),
    CHILD_ACCESS_DENIED(
            HttpStatus.FORBIDDEN,
        "해당 자녀 정보에 접근할 권한이 없습니다."
    ),
    CHILD_HAS_FINANCIAL_HISTORY(
            HttpStatus.CONFLICT,
        "금융 기록이 있는 자녀는 삭제할 수 없습니다."
    ),
    CHILD_INVALID_NAME(
            HttpStatus.BAD_REQUEST,
        "자녀 이름은 필수입니다."
    ),
    CHILD_INVALID_BIRTH_STATUS(
            HttpStatus.BAD_REQUEST,
        "출생 상태는 필수입니다."
    ),
    CHILD_EXPECTED_BIRTH_DATE_REQUIRED(
            HttpStatus.BAD_REQUEST,
        "출생 예정일은 필수입니다."
    ),
    CHILD_BIRTH_DATE_REQUIRED(
            HttpStatus.BAD_REQUEST,
        "생년월일은 필수입니다."
    );

    private final HttpStatus httpStatus;
    private final String message;
}
