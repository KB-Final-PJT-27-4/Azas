package com.azas.domain.timecapsule.dto;

import com.azas.domain.timecapsule.entity.TimeCapsule;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
public class TimeCapsuleGoalResponse {

    @JsonProperty("financial_goal_template_id")
    private final Long financialGoalTemplateId;

    @JsonProperty("goal_name")
    private final String goalName;

    @JsonProperty("goal_target_amount")
    private final BigDecimal goalTargetAmount;

    @JsonProperty("goal_target_date")
    private final LocalDate goalTargetDate;

    private TimeCapsuleGoalResponse(TimeCapsule timeCapsule) {
        this.financialGoalTemplateId = timeCapsule.getFinancialGoalTemplateId();
        this.goalName = timeCapsule.getGoalName();
        this.goalTargetAmount = timeCapsule.getGoalTargetAmount();
        this.goalTargetDate = timeCapsule.getGoalTargetDate();
    }

    // [JMG] CAPSULE-1~3 적금 계좌에 설정된 목표 정보를 프런트 공통 응답 형태로 변환한다.
    public static TimeCapsuleGoalResponse fromOrNull(TimeCapsule timeCapsule) {
        if (timeCapsule.getFinancialGoalTemplateId() == null) {
            return null;
        }

        return new TimeCapsuleGoalResponse(timeCapsule);
    }
}
