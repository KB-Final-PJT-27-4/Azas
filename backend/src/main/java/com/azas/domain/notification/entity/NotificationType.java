package com.azas.domain.notification.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {

    // 저축일 알림
    SAVINGS_SCHEDULE_REMINDER(NotificationCategory.SAVINGS),
    // 자동이체 성공 알림
    AUTO_TRANSFER_SUCCEEDED(NotificationCategory.SAVINGS),
    // 자동이체 실패 알림
    AUTO_TRANSFER_FAILED(NotificationCategory.SAVINGS),
    // 저축 목표 중간 알림
    GOAL_CHECKPOINT_REACHED(NotificationCategory.SAVINGS),

    // 타임캡슐 공개 임박 알림
    TIME_CAPSULE_RELEASE_SOON(NotificationCategory.TIME_CAPSULE),
    // 타임 캡슐 공개 알림
    TIME_CAPSULE_RELEASED(NotificationCategory.TIME_CAPSULE),

    // 용돈 요청 알림
    ALLOWANCE_REQUESTED(NotificationCategory.ALLOWANCE),
    // 용돈 승인 알림
    ALLOWANCE_APPROVED(NotificationCategory.ALLOWANCE),
    // 용돈 거절 알림
    ALLOWANCE_REJECTED(NotificationCategory.ALLOWANCE),

    // 임신 주차별 캐릭터 변경 알림
    PREGNANCY_CHARACTER_CHANGED(NotificationCategory.PREGNANCY),

    // 소비 한도 금액 도달 알림
    USAGE_GUIDE_AMOUNT_REACHED(NotificationCategory.USAGE_LIMIT),
    // 소비 한도 금액 초과 알림
    USAGE_GUIDE_AMOUNT_EXCEEDED(NotificationCategory.USAGE_LIMIT),

    // 미션 부여 알림
    MISSION_ASSIGNED(NotificationCategory.MISSION),
    // 미션 제출 알림
    MISSION_SUBMITTED(NotificationCategory.MISSION),
    // 미션 승인 알림
    MISSION_APPROVED(NotificationCategory.MISSION),
    // 미션 거절 알림
    MISSION_REJECTED(NotificationCategory.MISSION),
    // 미션 취소 알림
    MISSION_CANCELED(NotificationCategory.MISSION);

    private final NotificationCategory category;
}