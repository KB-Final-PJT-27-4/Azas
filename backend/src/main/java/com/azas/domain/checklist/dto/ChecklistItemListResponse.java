package com.azas.domain.checklist.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
@ApiModel(description = "생애주기 체크리스트 목록 응답")
public class ChecklistItemListResponse {

    @JsonProperty("child_id")
    private final Long childId;

    @JsonProperty("lifecycle_stage")
    private final String lifecycleStage;

    @JsonProperty("stage_title")
    private final String stageTitle;

    @JsonProperty("stage_description")
    private final String stageDescription;

    @JsonProperty("total_count")
    private final int totalCount;

    @JsonProperty("completed_count")
    private final int completedCount;

    @JsonProperty("progress_percent")
    private final int progressPercent;

    @JsonProperty("stage_completed")
    private final boolean stageCompleted;

    private final List<Item> items;

    public static ChecklistItemListResponse from(
            ChecklistItemListResult result
    ) {
        return new ChecklistItemListResponse(
                result.getChildId(),
                result.getLifecycleStage().name(),
                result.getStageTitle(),
                result.getStageDescription(),
                result.getTotalCount(),
                result.getCompletedCount(),
                result.getProgressPercent(),
                result.isStageCompleted(),
                result.getItems()
                        .stream()
                        .map(Item::from)
                        .collect(Collectors.toList())
        );
    }

    @Getter
    @RequiredArgsConstructor
    @ApiModel(
            value = "ChecklistItemListItemResponse",
            description = "체크리스트 항목"
    )
    public static class Item {

        @JsonProperty("checklist_item_id")
        private final Long checklistItemId;

        @JsonProperty("checklist_item_template_id")
        private final Long checklistItemTemplateId;

        @JsonProperty("template_key")
        private final String templateKey;

        private final String category;

        private final String title;

        private final String description;

        private final String content;

        @JsonProperty("action_type")
        private final String actionType;

        private final String url;

        @JsonProperty("info_title")
        private final String infoTitle;

        @JsonProperty("info_notice")
        private final String infoNotice;

        private final String status;

        private final boolean completed;

        @JsonProperty("completed_at")
        private final LocalDateTime completedAt;

        @JsonProperty("info_items")
        private final List<InfoItem> infoItems;

        private static Item from(ChecklistItemResult result) {
            return new Item(
                    result.getChecklistItemId(),
                    result.getChecklistItemTemplateId(),
                    result.getTemplateKey(),
                    result.getCategory(),
                    result.getTitle(),
                    result.getDescription(),
                    result.getContent(),
                    result.getActionType(),
                    result.getUrl(),
                    result.getInfoTitle(),
                    result.getInfoNotice(),
                    result.getStatus().name(),
                    result.isCompleted(),
                    result.getCompletedAt(),
                    result.getInfoItems()
                            .stream()
                            .map(InfoItem::from)
                            .toList()
            );
        }
    }

    @Getter
    @RequiredArgsConstructor
    @ApiModel(value = "ChecklistInfoItemResponse")
    public static class InfoItem {

        @JsonProperty("checklist_item_detail_id")
        private final Long checklistItemDetailId;

        private final String title;

        private final String description;

        @JsonProperty("action_label")
        private final String actionLabel;

        private final String url;

        private final String content;

        private static InfoItem from(
                ChecklistInfoItemResult result
        ) {
            return new InfoItem(
                    result.getChecklistItemDetailId(),
                    result.getTitle(),
                    result.getDescription(),
                    result.getActionLabel(),
                    result.getUrl(),
                    result.getContent()
            );
        }
    }
}
