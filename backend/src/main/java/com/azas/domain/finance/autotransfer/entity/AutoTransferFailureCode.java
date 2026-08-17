package com.azas.domain.finance.autotransfer.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AutoTransferFailureCode {

    INSUFFICIENT_BALANCE(
            "출금 계좌 잔액이 부족합니다.",
            "출금 계좌에 잔액을 채운 후 다시 시도해 주세요."
    ),

    SOURCE_ACCOUNT_INACTIVE(
            "출금 계좌를 사용할 수 없습니다.",
            "출금 계좌의 연결 및 상태를 확인해 주세요."
    ),

    DESTINATION_ACCOUNT_INACTIVE(
            "입금 계좌를 사용할 수 없습니다.",
            "자녀 적금계좌의 연결 및 상태를 확인해 주세요."
    ),

    GOAL_INACTIVE(
            "연결된 저축 목표를 사용할 수 없습니다.",
            "자녀의 저축 목표 상태를 확인해 주세요."
    ),

    PROCESSING_ERROR(
            "자동이체 처리 중 오류가 발생했습니다.",
            "잠시 후 수동 이체를 이용하거나 다시 시도해 주세요."
    );

    private final String message;
    private final String guide;
}