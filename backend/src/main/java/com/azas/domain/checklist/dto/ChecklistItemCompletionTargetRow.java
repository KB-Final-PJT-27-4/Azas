package com.azas.domain.checklist.dto;

import com.azas.domain.checklist.entity.ChecklistItemStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChecklistItemCompletionTargetRow {

    private Long checklistItemId;
    private Long childId;
    private ChecklistItemStatus status;
    private LocalDateTime completedAt;
}