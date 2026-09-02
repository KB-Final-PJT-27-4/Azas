package com.azas.domain.finance.transfer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class TransferListResponse<T> {

    private final List<T> items;

    @JsonProperty("next_cursor")
    private final String nextCursor;

    @JsonProperty("has_next")
    private final boolean hasNext;
}