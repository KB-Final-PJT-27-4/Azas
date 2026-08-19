package com.azas.domain.checklist.dto;

import com.azas.domain.checklist.entity.ChecklistItemStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ChecklistItemRow {

    private Long checklistItemId;
    private Long checklistItemTemplateId;
    private String title;
    private String description;
    private ChecklistItemStatus status;
    private LocalDateTime completedAt;
    private Integer itemOrder;
}