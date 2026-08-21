package com.azas.domain.mission.dto;

import com.azas.domain.mission.entity.MissionStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class MissionDetailRow {

    private Long missionId;
    private Long childId;
    private String title;
    private String description;
    private BigDecimal rewardAmount;
    private MissionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}