package com.azas.domain.mission.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MissionSummaryRow {

    private int totalCount;
    private int inProgressCount;
    private int needsReviewCount;
    private int completedCount;
}