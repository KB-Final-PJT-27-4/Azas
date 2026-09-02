package com.azas.domain.checklist.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChecklistInfoItemRow {

    private Long checklistItemDetailId;
    private String title;
    private String description;
    private String actionLabel;
    private String url;
    private String content;
    private Integer itemOrder;
}