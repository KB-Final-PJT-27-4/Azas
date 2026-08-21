package com.azas.domain.checklist.dto;

import com.azas.domain.checklist.entity.ChecklistLifecycleStage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class ChecklistItemListResult {

    private final Long childId;
    private final ChecklistLifecycleStage lifecycleStage;
    private final String stageTitle;
    private final String stageDescription;
    private final int totalCount;
    private final int completedCount;
    private final int progressPercent;
    private final boolean stageCompleted;
    private final List<ChecklistItemResult> items;
}