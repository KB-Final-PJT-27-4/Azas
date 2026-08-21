package com.azas.domain.checklist.dto;

import com.azas.domain.checklist.entity.ChecklistItemStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class ChecklistItemCompletionResult {

    private final Long checklistItemId;
    private final ChecklistItemStatus status;
    private final boolean completed;
    private final LocalDateTime completedAt;
}