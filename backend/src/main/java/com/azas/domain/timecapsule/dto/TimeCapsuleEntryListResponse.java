package com.azas.domain.timecapsule.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.Getter;

import java.util.List;

@ApiModel(description = "타임캡슐 기록 목록 응답")
@Getter
public class TimeCapsuleEntryListResponse {

    @JsonProperty("time_capsule")
    private final TimeCapsuleSummaryResponse timeCapsule;

    @JsonProperty("entries")
    private final List<TimeCapsuleEntrySummaryResponse> entries;

    @JsonProperty("total_count")
    private final int totalCount;

    public TimeCapsuleEntryListResponse(
            TimeCapsuleSummaryResponse timeCapsule,
            List<TimeCapsuleEntrySummaryResponse> entries
    ) {
        this.timeCapsule = timeCapsule;
        this.entries = List.copyOf(entries);
        this.totalCount = entries.size();
    }
}
