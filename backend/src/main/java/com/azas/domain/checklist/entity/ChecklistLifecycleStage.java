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
            "출생~1세 · 첫 금융 시작",
            "아이 이름으로 시작하는 금융생활의 기초를 준비해요."
    ),
    AGE_2_TO_4(
            "2~4세 · 자산 기반 형성",
            "목표와 저축 습관의 씨앗을 만들어보세요."
    ),
    AGE_5_TO_7(
            "5~7세 · 금융 습관 형성",
            "소비와 저축의 차이를 함께 배워볼 차례예요."
    ),
    AGE_8_TO_10(
            "8~10세 · 금융 이해 확장",
            "돈의 흐름과 계획을 아이 눈높이에 맞춰 알려줘요."
    ),
    AGE_11_TO_13(
            "11~13세 · 금융 경험 시작",
            "용돈과 미션으로 직접 관리하는 경험을 시작해요."
    ),
    AGE_14_TO_16(
            "14~16세 · 자산 성장",
            "장기 목표와 금융상품을 스스로 이해하도록 도와요."
    ),
    AGE_17_TO_19(
            "17~19세 · 미래 자산 완성",
            "독립 전 필요한 자산 준비를 마무리해요."
    );

    private final String title;
    private final String description;

    public static ChecklistLifecycleStage from(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_CHECKLIST_STAGE
            );
        }

        try {
            return valueOf(
                    value.trim().toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(
                    ErrorCode.INVALID_CHECKLIST_STAGE
            );
        }
    }
}