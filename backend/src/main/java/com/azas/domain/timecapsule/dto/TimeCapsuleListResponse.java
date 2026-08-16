package com.azas.domain.timecapsule.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class TimeCapsuleListResponse {

    @JsonProperty("time_capsules")
    private final List<TimeCapsuleSummaryResponse> timeCapsules;

    @JsonProperty("total_count")
    private final int totalCount;

    public TimeCapsuleListResponse(List<TimeCapsuleSummaryResponse> timeCapsules) {
        this.timeCapsules = List.copyOf(timeCapsules);
        this.totalCount = timeCapsules.size();
    }
}
