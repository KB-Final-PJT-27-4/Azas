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
