package com.azas.domain.notification.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationCategory {

    SAVINGS(
            "저축·자동이체 알림",
            "저축 예정일과 자동이체 처리 결과를 알려드려요.",
            1
    ),

    TIME_CAPSULE(
            "타임캡슐 공개 알림",
            "타임캡슐을 열 수 있는 날을 알려드려요.",
            2
    ),

    ALLOWANCE(
            "용돈 요청 알림",
            "아이의 용돈 요청과 처리 결과를 알려드려요.",
            3
    ),

    PREGNANCY(
            "임신 주차별 알림",
            "주차별 아이의 성장 단계 변화를 알려드려요.",
            4
    ),

    USAGE_LIMIT(
            "아이 한도 초과 알림",
            "아이가 설정한 사용 한도를 넘으면 알려드려요.",
            5
    ),

    MISSION(
            "아이 미션 알림",
            "아이의 미션 진행 및 완료 결과를 알려드려요.",
            6
    );

    private final String label;
    private final String description;
    private final int displayOrder;
}