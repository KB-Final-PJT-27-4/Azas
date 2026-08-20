package com.azas.domain.report.service;

import com.azas.domain.report.dto.AssetReportUpsertCommand;
import com.azas.domain.report.mapper.AssetReportMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AssetReportSnapshotService {

    private static final BigDecimal ONE_HUNDRED =
            new BigDecimal("100");

    private final AssetReportMapper assetReportMapper;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void generateForChild(
            Long childId,
            YearMonth targetMonth
    ) {
        LocalDate reportMonth =
                targetMonth.atDay(1);

        LocalDateTime startAt =
                targetMonth.atDay(1).atStartOfDay();

        LocalDateTime endExclusive =
                targetMonth.plusMonths(1)
                        .atDay(1)
                        .atStartOfDay();

        BigDecimal totalAssetAmount = zeroIfNull(
                assetReportMapper.findTotalAssetAmountAt(
                        childId,
                        endExclusive
                )
        );

        BigDecimal previousTotalAssetAmount = zeroIfNull(
                assetReportMapper.findPreviousTotalAssetAmount(
                        childId,
                        targetMonth.minusMonths(1).atDay(1)
                )
        );

        BigDecimal totalAssetChangeAmount =
                totalAssetAmount.subtract(
                        previousTotalAssetAmount
                );

        BigDecimal monthlySavedAmount = zeroIfNull(
                assetReportMapper.findMonthlySavedAmount(
                        childId,
                        startAt,
                        endExclusive
                )
        );

        /*
         * 여기에는 다음 단계에서 목표별 조회 결과를 넣습니다.
         *
         * totalGoalTargetAmount:
         *     활성 목표의 target_amount 합계
         *
         * totalGoalSavedAmount:
         *     목표 연결 계좌의 월말 잔액 합계
         */
        BigDecimal totalGoalTargetAmount = BigDecimal.ZERO;
        BigDecimal totalGoalSavedAmount = BigDecimal.ZERO;

        BigDecimal achievementRate =
                calculateRate(
                        totalGoalSavedAmount,
                        totalGoalTargetAmount
                );

        List<Map<String, Object>> insights =
                createInsights(
                        monthlySavedAmount,
                        totalAssetChangeAmount,
                        achievementRate
                );

        AssetReportUpsertCommand command =
                AssetReportUpsertCommand.builder()
                        .childId(childId)
                        .reportMonth(reportMonth)
                        .totalAssetAmount(totalAssetAmount)
                        .totalAssetChangeAmount(
                                totalAssetChangeAmount
                        )
                        .monthlySavedAmount(
                                monthlySavedAmount
                        )
                        .totalGoalTargetAmount(
                                totalGoalTargetAmount
                        )
                        .totalGoalSavedAmount(
                                totalGoalSavedAmount
                        )
                        .goalAchievementRate(
                                achievementRate
                        )
                        .sixMonthFlowJson("[]")
                        .savingsGoalSummaryJson("[]")
                        .insightItemsJson(
                                writeJson(insights)
                        )
                        .build();

        assetReportMapper.upsertAssetReport(command);
    }

    private List<Map<String, Object>> createInsights(
            BigDecimal monthlySavedAmount,
            BigDecimal totalAssetChangeAmount,
            BigDecimal achievementRate
    ) {
        List<Map<String, Object>> items =
                new ArrayList<>();

        if (totalAssetChangeAmount.signum() > 0) {
            items.add(Map.of(
                    "type", "MONTH_COMPARISON",
                    "title",
                    "지난달보다 "
                            + totalAssetChangeAmount
                            .toPlainString()
                            + "원을 더 모았어요.",
                    "description",
                    "꾸준한 저축 흐름이 아주 좋아요."
            ));
        }

        if (achievementRate.compareTo(
                new BigDecimal("50")
        ) >= 0) {
            items.add(Map.of(
                    "type", "GOAL_PROGRESS",
                    "title", "목표의 절반에 가까워졌어요.",
                    "description",
                    "현재 속도대로 저축을 이어가 보세요."
            ));
        }

        if (monthlySavedAmount.signum() > 0) {
            items.add(Map.of(
                    "type", "MONTHLY_SAVING",
                    "title",
                    "이번 달 "
                            + monthlySavedAmount
                            .toPlainString()
                            + "원을 저축했어요.",
                    "description",
                    "목표 계좌 저축액을 기준으로 계산했어요."
            ));
        }

        return items;
    }

    private BigDecimal calculateRate(
            BigDecimal currentAmount,
            BigDecimal targetAmount
    ) {
        if (targetAmount == null
                || targetAmount.signum() <= 0) {
            return BigDecimal.ZERO;
        }

        return currentAmount
                .multiply(ONE_HUNDRED)
                .divide(
                        targetAmount,
                        4,
                        RoundingMode.HALF_UP
                )
                .min(ONE_HUNDRED);
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null
                ? BigDecimal.ZERO
                : value;
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "자산 리포트 JSON 생성에 실패했습니다.",
                    exception
            );
        }
    }
}