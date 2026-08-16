package com.azas.domain.timecapsule.dto;

import com.azas.domain.timecapsule.entity.TimeCapsule;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Getter
public class TimeCapsuleSummaryResponse {

    @JsonProperty("time_capsule_id")
    private final Long timeCapsuleId;

    @JsonProperty("account_id")
    private final Long accountId;

    private final String title;
    private final String status;

    @JsonProperty("release_date")
    private final LocalDate releaseDate;

    private final Long dDay;

    @JsonProperty("total_saved_amount")
    private final BigDecimal totalSavedAmount;

    private TimeCapsuleSummaryResponse(
            TimeCapsule timeCapsule,
            LocalDate today
    ) {
        this.timeCapsuleId = timeCapsule.getTimeCapsuleId();
        this.accountId = timeCapsule.getFinancialAccountId();
        this.title = timeCapsule.getTitle();
        this.status = timeCapsule.getStatus().name();
        this.releaseDate = timeCapsule.getExpectedReleaseAt() == null
                ? null
                : timeCapsule.getExpectedReleaseAt().toLocalDate();
        this.dDay = calculateDDay(timeCapsule, today);
        this.totalSavedAmount = timeCapsule.getTotalContributionAmount() == null
                ? BigDecimal.ZERO
                : timeCapsule.getTotalContributionAmount();
    }

    // [JMG] CAPSULE-2 보관함 목록 항목을 카드·캘린더 화면 공용 응답으로 변환한다.
    public static TimeCapsuleSummaryResponse from(
            TimeCapsule timeCapsule,
            LocalDate today
    ) {
        return new TimeCapsuleSummaryResponse(timeCapsule, today);
    }

    @JsonProperty("d_day")
    public Long getDDay() {
        return dDay;
    }

    // [JMG] CAPSULE-2 예상 공개일까지 남은 일수를 계산한다.
    private static Long calculateDDay(
            TimeCapsule timeCapsule,
            LocalDate today
    ) {
        if (timeCapsule.getExpectedReleaseAt() == null) {
            return null;
        }

        long remainingDays = ChronoUnit.DAYS.between(
                today,
                timeCapsule.getExpectedReleaseAt().toLocalDate()
        );
        return Math.max(remainingDays, 0L);
    }
}
