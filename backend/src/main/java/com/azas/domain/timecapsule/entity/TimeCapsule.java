package com.azas.domain.timecapsule.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TimeCapsule {

    private Long timeCapsuleId;
    private Long childId;
    private Long financialAccountId;
    private String title;
    private TimeCapsuleStatus status;
    private LocalDateTime expectedReleaseAt;
    private LocalDateTime releasedAt;
    private int entryCount;
    private BigDecimal totalContributionAmount;
    private LocalDateTime latestEntryAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long financialGoalTemplateId;
    private String goalName;
    private BigDecimal goalTargetAmount;
    private LocalDate goalTargetDate;

    // [JMG] CAPSULE-1 적금 계좌의 만기일을 예상 공개일로 갖는 보관함을 생성한다.
    public static TimeCapsule create(
            long childId,
            long financialAccountId,
            String title,
            LocalDate releaseDate
    ) {
        TimeCapsule timeCapsule = new TimeCapsule();
        timeCapsule.childId = childId;
        timeCapsule.financialAccountId = financialAccountId;
        timeCapsule.title = title;
        timeCapsule.status = TimeCapsuleStatus.COLLECTING;
        timeCapsule.expectedReleaseAt = releaseDate == null
                ? null
                : releaseDate.atStartOfDay();
        timeCapsule.totalContributionAmount = BigDecimal.ZERO;
        return timeCapsule;
    }
}
