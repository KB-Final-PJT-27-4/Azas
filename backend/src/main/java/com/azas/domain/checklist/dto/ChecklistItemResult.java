package com.azas.domain.checklist.dto;

import com.azas.domain.checklist.entity.ChecklistItemStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class ChecklistItemResult {

    private final Long checklistItemId;
    private final Long checklistItemTemplateId;
    private final String title;
    private final String description;
    private final ChecklistItemStatus status;
    private final boolean completed;
    private final LocalDateTime completedAt;

    public static ChecklistItemResult from(ChecklistItemRow row) {
        boolean completed =
                row.getStatus() == ChecklistItemStatus.COMPLETED;

        return new ChecklistItemResult(
                row.getChecklistItemId(),
                row.getChecklistItemTemplateId(),
                row.getTitle(),
                row.getDescription(),
                row.getStatus(),
                completed,
                row.getCompletedAt()
        );
    }
}