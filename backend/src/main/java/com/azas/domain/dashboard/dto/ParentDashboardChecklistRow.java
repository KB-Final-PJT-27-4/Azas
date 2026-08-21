package com.azas.domain.dashboard.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParentDashboardChecklistRow {

    private Long checklistItemId;
    private String title;
    private String status;
}