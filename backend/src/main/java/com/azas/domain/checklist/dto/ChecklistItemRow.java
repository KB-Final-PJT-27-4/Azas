package com.azas.domain.checklist.dto;

import com.azas.domain.checklist.entity.ChecklistItemStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ChecklistItemRow {

    private Long checklistItemId;
    private Long checklistItemTemplateId;
    private String templateKey;
    private String category;
    private String title;
    private String description;
    private String content;
    private String actionType;
    private String url;
    private String infoTitle;
    private String infoNotice;
    private ChecklistItemStatus status;
    private LocalDateTime completedAt;
    private Integer itemOrder;

    private List<ChecklistInfoItemRow> infoItems =
            new ArrayList<>();
}