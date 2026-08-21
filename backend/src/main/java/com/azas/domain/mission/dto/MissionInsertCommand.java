package com.azas.domain.mission.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class MissionInsertCommand {

    private Long missionId;

    private final Long childId;
    private final Long createdByMemberId;
    private final String title;
    private final String description;
    private final BigDecimal rewardAmount;
    private final LocalDateTime createdAt;
}