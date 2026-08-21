package com.azas.domain.dashboard.dto;

import com.azas.domain.mission.entity.MissionStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ChildDashboardMissionRow {

    private Long missionId;
    private String title;
    private String description;
    private BigDecimal rewardAmount;
    private MissionStatus status;
}
