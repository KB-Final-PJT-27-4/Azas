package com.azas.domain.finance.autotransfer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AutoTransferScheduleListResponse {

    private final List<AutoTransferScheduleListItemResponse> items;

    @JsonProperty("next_cursor")
    private final String nextCursor;

    @JsonProperty("has_next")
    private final boolean hasNext;
}