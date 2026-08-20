package com.azas.domain.checklist.dto;

import com.azas.domain.checklist.entity.ChecklistItemStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class ChecklistItemResult {

    private final Long checklistItemId;
    private final Long checklistItemTemplateId;
    private final String templateKey;
    private final String category;
    private final String title;
    private final String description;
    private final String content;
    private final String actionType;
    private final String url;
    private final String infoTitle;
    private final String infoNotice;
    private final ChecklistItemStatus status;
    private final boolean completed;
    private final LocalDateTime completedAt;
    private final List<ChecklistInfoItemResult> infoItems;

    public static ChecklistItemResult from(ChecklistItemRow row) {
        boolean completed =
                row.getStatus()
                        == ChecklistItemStatus.COMPLETED;

        List<ChecklistInfoItemResult> infoItems =
                row.getInfoItems() == null
                        ? List.of()
                        : row.getInfoItems().stream()
                        .map(ChecklistInfoItemResult::from)
                        .toList();

        return new ChecklistItemResult(
                row.getChecklistItemId(),
                row.getChecklistItemTemplateId(),
                row.getTemplateKey(),
                row.getCategory(),
                row.getTitle(),
                row.getDescription(),
                row.getContent(),
                row.getActionType(),
                row.getUrl(),
                row.getInfoTitle(),
                row.getInfoNotice(),
                row.getStatus(),
                completed,
                row.getCompletedAt(),
                infoItems
        );
    }
}