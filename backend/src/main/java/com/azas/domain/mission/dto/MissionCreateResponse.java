package com.azas.domain.mission.dto;

import com.azas.domain.mission.entity.MissionStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@AllArgsConstructor
public class MissionCreateResponse {

    @JsonProperty("mission_id")
    private final Long missionId;

    @JsonProperty("child_id")
    private final Long childId;

    private final String title;
    private final String description;

    @JsonProperty("reward_amount")
    private final BigDecimal rewardAmount;

    private final MissionStatus status;

    @JsonProperty("created_at")
    private final Instant createdAt;
}