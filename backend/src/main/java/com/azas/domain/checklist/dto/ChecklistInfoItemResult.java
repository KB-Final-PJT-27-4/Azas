package com.azas.domain.checklist.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ChecklistInfoItemResult {

    private final Long checklistItemDetailId;
    private final String title;
    private final String description;
    private final String actionLabel;
    private final String url;
    private final String content;

    public static ChecklistInfoItemResult from(
            ChecklistInfoItemRow row
    ) {
        return new ChecklistInfoItemResult(
                row.getChecklistItemDetailId(),
                row.getTitle(),
                row.getDescription(),
                row.getActionLabel(),
                row.getUrl(),
                row.getContent()
        );
    }
}