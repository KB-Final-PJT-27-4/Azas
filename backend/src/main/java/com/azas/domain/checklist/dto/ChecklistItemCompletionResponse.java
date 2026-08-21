package com.azas.domain.checklist.dto;

import com.azas.domain.checklist.entity.ChecklistItemStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.format.DateTimeFormatter;

@Getter
@RequiredArgsConstructor
public class ChecklistItemCompletionResponse {

    @JsonProperty("checklist_item_id")
    private final Long checklistItemId;

    private final ChecklistItemStatus status;

    @JsonProperty("is_completed")
    private final boolean completed;

    @JsonProperty("completed_at")
    private final String completedAt;

    public static ChecklistItemCompletionResponse from(
            ChecklistItemCompletionResult result
    ) {
        String completedAt = result.getCompletedAt() == null
                ? null
                : result.getCompletedAt().format(
                DateTimeFormatter.ISO_LOCAL_DATE_TIME
        );

        return new ChecklistItemCompletionResponse(
                result.getChecklistItemId(),
                result.getStatus(),
                result.isCompleted(),
                completedAt
        );
    }
}