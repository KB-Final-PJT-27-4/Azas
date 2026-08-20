package com.azas.domain.report.service;

import com.azas.domain.report.mapper.AssetReportMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonthlyAssetReportGenerator {
    // 모든 활성 자녀를 순회하는 배치 담당

    private static final ZoneId SEOUL_ZONE =
            ZoneId.of("Asia/Seoul");

    private final AssetReportMapper assetReportMapper;
    private final AssetReportSnapshotService snapshotService;

    private final Clock clock =
            Clock.system(SEOUL_ZONE);

    /*
     * 매월 1일 오전 00시 10분에 지난달 리포트를 생성합니다.
     *
     * 예:
     * 2026-08-01 실행
     * -> 2026년 7월 리포트 생성
     */
    @Scheduled(
            cron = "0 10 0 1 * *",
            zone = "Asia/Seoul"
    )
    public void generatePreviousMonth() {
        YearMonth previousMonth =
                YearMonth.now(clock)
                        .minusMonths(1);

        generate(previousMonth);
    }

    public void generate(YearMonth targetMonth) {
        List<Long> childIds =
                assetReportMapper.findAllActiveChildIds();

        for (Long childId : childIds) {
            try {
                snapshotService.generateForChild(
                        childId,
                        targetMonth
                );
            } catch (Exception exception) {
                log.error(
                        "자산 리포트 생성 실패: childId={}, reportMonth={}",
                        childId,
                        targetMonth,
                        exception
                );
            }
        }
    }
}