package com.azas.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    BADREQUEST(
            HttpStatus.BAD_REQUEST,
            "요청 값이 올바르지 않습니다."
    ),
    INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "서버 오류가 발생했습니다."
    ),
    INVALID_REQUEST(
            HttpStatus.BAD_REQUEST,
            "잘못된 요청입니다."
    ),

    // Auth
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
    ACCESS_TOKEN_REQUIRED(
            HttpStatus.UNAUTHORIZED,
            "Access Token이 필요합니다."
    ),
    INVALID_ACCESS_TOKEN(
            HttpStatus.UNAUTHORIZED,
            "Access Token이 유효하지 않거나 만료되었습니다."
    ),
    INVALID_FAMILY_INVITATION(
            HttpStatus.BAD_REQUEST,
            "유효하지 않거나 사용할 수 없는 가족 초대입니다."
    ),
    FAMILY_MEMBER_ALREADY_LINKED(
            HttpStatus.CONFLICT,
            "이미 다른 회원 또는 자녀 정보와 연결되어 있습니다."
    ),
    MEMBER_TYPE_CONFLICT(
            HttpStatus.CONFLICT,
            "초대 유형과 회원 유형이 일치하지 않습니다."
    ),

    // Member
    INVALID_PHONE_NUMBER(
            HttpStatus.BAD_REQUEST,
            "올바른 휴대폰번호 형식이 아닙니다."
    ),
    PHONE_VERIFICATION_RESEND_NOT_ALLOWED(
            HttpStatus.TOO_MANY_REQUESTS,
            "인증번호 재발송 대기 시간이 지나지 않았습니다."
    ),
    SMS_DELIVERY_FAILED(
            HttpStatus.BAD_GATEWAY,
            "SMS 인증번호를 발송하지 못했습니다."
    ),
    PHONE_VERIFICATION_NOT_AVAILABLE(
            HttpStatus.BAD_REQUEST,
            "휴대폰 인증 요청이 만료되었거나 사용할 수 없습니다."
    ),
    INVALID_PHONE_VERIFICATION_CODE(
            HttpStatus.BAD_REQUEST,
            "휴대폰 인증번호가 올바르지 않습니다."
    ),
    PHONE_VERIFICATION_ATTEMPT_LIMIT_EXCEEDED(
            HttpStatus.TOO_MANY_REQUESTS,
            "휴대폰 인증번호 확인 가능 횟수를 초과했습니다."
    ),
    INVALID_PHONE_VERIFICATION_TOKEN(
            HttpStatus.BAD_REQUEST,
            "휴대폰 인증 토큰이 만료되었거나 사용할 수 없습니다."
    ),
    PHONE_NUMBER_ALREADY_IN_USE(
            HttpStatus.CONFLICT,
            "이미 다른 회원이 사용 중인 휴대폰번호입니다."
    ),

    // Finance
    INVALID_CHILD_USAGE_POLICY(
            HttpStatus.BAD_REQUEST,
            "자녀 계좌 사용 관리 정책 값이 올바르지 않습니다."
    ),
    PARENT_ACCESS_REQUIRED(
            HttpStatus.FORBIDDEN,
            "해당 자녀의 부모 권한이 필요합니다."
    ),
    INELIGIBLE_CHILD_USAGE_POLICY_ACCOUNT(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "자녀 명의의 활성 입출금 계좌에만 사용 관리 정책을 설정할 수 있습니다."
    ),
    FINANCIAL_ACCOUNT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "금융 계좌를 찾을 수 없습니다."
    ),
    FINANCIAL_PRODUCT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "금융상품을 찾을 수 없습니다."
    ),
    INVALID_QUERY_PARAMETER(
            HttpStatus.BAD_REQUEST,
            "조회 조건이 올바르지 않습니다."
    ),

    // Time Capsule
    TIME_CAPSULE_ACCESS_DENIED(
            HttpStatus.FORBIDDEN,
            "해당 타임캡슐에 접근할 권한이 없습니다."
    ),
    TIME_CAPSULE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "타임캡슐을 찾을 수 없습니다."
    ),
    ACCOUNT_TRANSACTION_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "계좌 거래내역을 찾을 수 없습니다."
    ),
    DUPLICATE_TIME_CAPSULE(
            HttpStatus.CONFLICT,
            "해당 적금 계좌에는 이미 타임캡슐이 존재합니다."
    ),
    DUPLICATE_TIME_CAPSULE_ENTRY(
            HttpStatus.CONFLICT,
            "해당 거래는 이미 타임캡슐 기록으로 등록되어 있습니다."
    ),
    TIME_CAPSULE_ENTRY_CREATION_NOT_ALLOWED(
            HttpStatus.CONFLICT,
            "공개되었거나 보관 처리된 타임캡슐에는 기록을 생성할 수 없습니다."
    ),
    INELIGIBLE_TIME_CAPSULE_ACCOUNT(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "자녀 명의의 활성 적금 계좌만 타임캡슐로 등록할 수 있습니다."
    ),
    INELIGIBLE_TIME_CAPSULE_TRANSACTION(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "타임캡슐 기록에는 적금 계좌의 입금 거래만 연결할 수 있습니다."
    ),
    TIME_CAPSULE_ENTRY_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "타임캡슐 엔트리를 찾을 수 없습니다."
    ),
    TIME_CAPSULE_ENTRY_MODIFICATION_NOT_ALLOWED(
            HttpStatus.CONFLICT,
            "봉인·삭제되었거나 수정 가능 횟수를 초과한 타임캡슐 엔트리는 수정할 수 없습니다."
    ),
    TIME_CAPSULE_ENTRY_MEDIA_REQUIREMENT_NOT_MET(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "엔트리 봉인에 필요한 미디어 업로드 또는 미디어 개수 조건을 충족하지 못했습니다."
    ),
    TIME_CAPSULE_MEDIA_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "타임캡슐 미디어를 찾을 수 없습니다."
    ),
    TIME_CAPSULE_MEDIA_UPLOAD_NOT_ALLOWED(
            HttpStatus.CONFLICT,
            "현재 엔트리 상태 또는 미디어 슬롯에서는 업로드할 수 없습니다."
    ),
    TIME_CAPSULE_MEDIA_OBJECT_INVALID(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "업로드된 미디어 객체의 MIME 타입 또는 파일 크기가 요청값과 다릅니다."
    ),
    TIME_CAPSULE_STORAGE_UNAVAILABLE(
            HttpStatus.BAD_GATEWAY,
            "타임캡슐 미디어 저장소에 연결할 수 없습니다."
    ),
    TIME_CAPSULE_EXPORT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "타임캡슐 결과물 생성 작업을 찾을 수 없습니다."
    ),
    TIME_CAPSULE_EXPORT_CREATION_NOT_ALLOWED(
            HttpStatus.CONFLICT,
            "공개되었고 봉인된 기록이 있는 타임캡슐에서만 결과물을 생성할 수 있습니다."
    ),
    TIME_CAPSULE_EXPORT_NOT_READY(
            HttpStatus.CONFLICT,
            "타임캡슐 결과물 생성 작업이 아직 완료되지 않았습니다."
    ),
    TIME_CAPSULE_EXPORT_EXPIRED(
            HttpStatus.GONE,
            "타임캡슐 결과물의 보관 기간이 만료되었습니다."
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
    ),

    //Family
    ALLOWANCE_REQUEST_ALREADY_EXISTS(
            HttpStatus.CONFLICT,
            "이번 달에는 이미 용돈을 요청했습니다."
    ),
    FAMILY_INVITATION_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "가족 초대를 찾을 수 없습니다."
    ),
    FAMILY_INVITATION_ALREADY_EXISTS(
            HttpStatus.CONFLICT,
            "같은 유형의 사용 가능한 가족 초대가 이미 존재합니다."
    ),
    FAMILY_INVITATION_GONE(
            HttpStatus.GONE,
            "만료되었거나 이미 처리된 가족 초대입니다."
    ),
    FAMILY_INVITATION_RELATION_TYPE_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "부모 초대 수락에는 관계 유형이 필요합니다."
    ),

    // Transfer
    TRANSFER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "이체 정보를 찾을 수 없습니다."
    ),
    TRANSFER_ACCESS_DENIED(
            HttpStatus.FORBIDDEN,
            "해당 이체 정보에 접근할 권한이 없습니다."
    ),
    INVALID_TRANSFER_REQUEST(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "출금·입금 계좌 또는 이체 금액 조건이 올바르지 않습니다."
    ),
    INSUFFICIENT_ACCOUNT_BALANCE(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "출금 계좌의 잔액이 부족합니다."
    ),
    DUPLICATE_TRANSFER_REQUEST(
            HttpStatus.CONFLICT,
            "이미 처리되었거나 처리 중인 이체 요청입니다."
    ),
    TRANSFER_PROCESSING_FAILED(
            HttpStatus.BAD_GATEWAY,
            "이체 처리 중 오류가 발생했습니다."
    );

    private final HttpStatus httpStatus;
    private final String message;
}
