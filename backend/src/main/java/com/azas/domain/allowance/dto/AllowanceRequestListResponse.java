package com.azas.domain.allowance.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AllowanceRequestListResponse {

    private final List<AllowanceRequestListItemResponse> items;

    @JsonProperty("next_cursor")
    private final Long nextCursor;

    @JsonProperty("has_next")
    private final boolean hasNext;
}