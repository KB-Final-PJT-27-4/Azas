package com.azas.domain.finance.goal.dto;

import com.azas.domain.finance.goal.entity.FinancialGoalTemplate;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class FinancialGoalTemplateListResponse {

    private final List<Item> templates;

    public FinancialGoalTemplateListResponse(List<Item> templates) {
        this.templates = List.copyOf(templates);
    }

    public static FinancialGoalTemplateListResponse from(
            List<FinancialGoalTemplate> templates
    ) {
        return new FinancialGoalTemplateListResponse(
                templates.stream()
                        .map(Item::from)
                        .collect(Collectors.toList())
        );
    }

    @Getter
    @ApiModel(value = "FinancialGoalTemplateListItemResponse")
    public static class Item {
        @JsonProperty("financial_goal_template_id")
        private final Long financialGoalTemplateId;
        private final String name;
        private final String description;
        @JsonProperty("icon_key")
        private final String iconKey;
        @JsonProperty("display_order")
        private final Integer displayOrder;

        private Item(
                Long financialGoalTemplateId,
                String name,
                String description,
                String iconKey,
                Integer displayOrder
        ) {
            this.financialGoalTemplateId = financialGoalTemplateId;
            this.name = name;
            this.description = description;
            this.iconKey = iconKey;
            this.displayOrder = displayOrder;
        }

        private static Item from(FinancialGoalTemplate template) {
            return new Item(
                    template.getFinancialGoalTemplateId(),
                    template.getGoalName(),
                    template.getDescription(),
                    template.getIconKey(),
                    template.getSortOrder()
            );
        }
    }
}
