package com.azas.domain.checklist.entity;

import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Locale;

@Getter
@RequiredArgsConstructor
public enum ChecklistLifecycleStage {

    PREGNANCY(
            "임신 중~출산 전 · 미래 준비",
            "출산 전에 아이의 첫 금융 준비를 시작해보세요."
    ),
    AGE_0_TO_1(
            "출생~1세 · 금융 기반 만들기",
            "아이 명의 금융 기반을 차근차근 준비해요."
    ),
    AGE_2_TO_4(
            "2~4세 · 돈과 친해지기",
            "생활 속에서 돈의 개념을 자연스럽게 알려주세요."
    ),
    AGE_5_TO_7(
            "5~7세 · 금융 습관 형성",
            "소비와 저축의 차이를 함께 배워볼 차례예요."
    );

    private final String title;
    private final String description;

    public static ChecklistLifecycleStage from(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_CHECKLIST_STAGE);
        }

        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.INVALID_CHECKLIST_STAGE);
        }
    }
}