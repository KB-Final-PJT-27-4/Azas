package com.azas.domain.mission.dto;

import com.azas.domain.mission.entity.MissionStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Getter
@AllArgsConstructor
public class MissionDetailResponse {

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

    @JsonProperty("updated_at")
    private final Instant updatedAt;

    public static MissionDetailResponse from(
            MissionDetailRow row
    ) {
        return new MissionDetailResponse(
                row.getMissionId(),
                row.getChildId(),
                row.getTitle(),
                row.getDescription(),
                row.getRewardAmount(),
                row.getStatus(),
                toInstant(row.getCreatedAt()),
                toInstant(row.getUpdatedAt())
        );
    }

    private static Instant toInstant(
            LocalDateTime value
    ) {
        return value == null
                ? null
                : value.toInstant(ZoneOffset.UTC);
    }
}