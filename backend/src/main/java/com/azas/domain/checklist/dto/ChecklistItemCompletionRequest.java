package com.azas.domain.checklist.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Getter
@NoArgsConstructor
public class ChecklistItemCompletionRequest {

    @NotNull
    @JsonProperty("completed")
    private Boolean completed;
}