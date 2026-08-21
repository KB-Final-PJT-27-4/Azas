package com.azas.domain.mission.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class MissionListResponse {

    private final MissionListSummaryResponse summary;
    private final List<MissionListItemResponse> items;

    @JsonProperty("next_cursor")
    private final Long nextCursor;

    @JsonProperty("has_next")
    private final boolean hasNext;
}