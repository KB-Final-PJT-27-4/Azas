package com.azas.domain.timecapsule.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class TimeCapsuleListResponse {

    private final List<TimeCapsuleSummaryResponse> items;

    @JsonProperty("next_cursor")
    private final String nextCursor;

    @JsonProperty("has_next")
    private final boolean hasNext;

    public TimeCapsuleListResponse(
            List<TimeCapsuleSummaryResponse> items,
            String nextCursor,
            boolean hasNext
    ) {
        this.items = items;
        this.nextCursor = nextCursor;
        this.hasNext = hasNext;
    }
}
