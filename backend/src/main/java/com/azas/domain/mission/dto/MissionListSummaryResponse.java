package com.azas.domain.mission.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MissionListSummaryResponse {

    @JsonProperty("total_count")
    private final int totalCount;

    @JsonProperty("in_progress_count")
    private final int inProgressCount;

    @JsonProperty("needs_review_count")
    private final int needsReviewCount;

    @JsonProperty("completed_count")
    private final int completedCount;

    public static MissionListSummaryResponse from(
            MissionSummaryRow row
    ) {
        if (row == null) {
            return new MissionListSummaryResponse(
                    0, 0, 0, 0
            );
        }

        return new MissionListSummaryResponse(
                row.getTotalCount(),
                row.getInProgressCount(),
                row.getNeedsReviewCount(),
                row.getCompletedCount()
        );
    }
}