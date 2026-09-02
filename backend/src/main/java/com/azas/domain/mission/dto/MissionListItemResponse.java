package com.azas.domain.mission.dto;

import com.azas.domain.mission.entity.MissionStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;

@Getter
@AllArgsConstructor
public class MissionListItemResponse {

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

    public static MissionListItemResponse from(
            MissionListRow row
    ) {
        return new MissionListItemResponse(
                row.getMissionId(),
                row.getChildId(),
                row.getTitle(),
                row.getDescription(),
                row.getRewardAmount(),
                row.getStatus(),
                row.getCreatedAt() == null
                        ? null
                        : row.getCreatedAt()
                        .toInstant(ZoneOffset.UTC)
        );
    }
}