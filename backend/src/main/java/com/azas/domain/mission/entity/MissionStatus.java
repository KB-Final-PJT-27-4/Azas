package com.azas.domain.mission.entity;

public enum MissionStatus {

    // 부모가 생성했고 자녀가 수행 중
    ASSIGNED,

    // 자녀가 완료 요청, 부모 확인 필요
    SUBMITTED,

    // 부모 승인 및 보상 완료
    APPROVED,

    // 부모 반려, 자녀 재수행 가능
    REJECTED,

    // 부모가 미션 취소
    CANCELED
}