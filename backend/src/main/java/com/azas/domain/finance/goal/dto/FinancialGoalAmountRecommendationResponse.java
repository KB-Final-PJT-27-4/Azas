package com.azas.domain.finance.goal.dto;

import com.azas.domain.finance.goal.entity.FinancialGoalAmountRecommendation;
import com.azas.domain.finance.goal.entity.FinancialGoalRecommendationBasis;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@ApiModel(value = "FinancialGoalAmountRecommendationResponse")
public class FinancialGoalAmountRecommendationResponse {

    @JsonProperty("financial_goal_template_id")
    private final Long financialGoalTemplateId;

    @JsonProperty("goal_name")
    private final String goalName;

    @JsonProperty("recommendation_method")
    private final String recommendationMethod;

    @JsonProperty("reference_data")
    private final ReferenceData referenceData;

    private final String description;
    private final String disclaimer;
    private final List<Item> recommendations;

    private FinancialGoalAmountRecommendationResponse(
            Long financialGoalTemplateId,
            String goalName,
            String recommendationMethod,
            ReferenceData referenceData,
            String description,
            String disclaimer,
            List<Item> recommendations
    ) {
        this.financialGoalTemplateId = financialGoalTemplateId;
        this.goalName = goalName;
        this.recommendationMethod = recommendationMethod;
        this.referenceData = referenceData;
        this.description = description;
        this.disclaimer = disclaimer;
        this.recommendations = List.copyOf(recommendations);
    }

    public static FinancialGoalAmountRecommendationResponse from(
            FinancialGoalAmountRecommendationResult result
    ) {
        FinancialGoalRecommendationBasis basis = result.getBasis();
        return new FinancialGoalAmountRecommendationResponse(
                result.getTemplate().getFinancialGoalTemplateId(),
                result.getTemplate().getGoalName(),
                basis.getRecommendationMethod(),
                ReferenceData.from(basis),
                basis.getDescription(),
                basis.getDisclaimer(),
                result.getRecommendations().stream()
                        .map(Item::from)
                        .collect(Collectors.toList())
        );
    }

    @Getter
    @ApiModel(value = "FinancialGoalAmountRecommendationReferenceData")
    public static class ReferenceData {
        private final String organization;

        @JsonProperty("dataset_name")
        private final String datasetName;

        @JsonProperty("reference_year")
        private final Integer referenceYear;

        @JsonProperty("metric_name")
        private final String metricName;

        @JsonProperty("metric_value")
        private final BigDecimal metricValue;

        @JsonProperty("metric_unit")
        private final String metricUnit;

        @JsonProperty("source_url")
        private final String sourceUrl;

        private ReferenceData(
                String organization,
                String datasetName,
                Integer referenceYear,
                String metricName,
                BigDecimal metricValue,
                String metricUnit,
                String sourceUrl
        ) {
            this.organization = organization;
            this.datasetName = datasetName;
            this.referenceYear = referenceYear;
            this.metricName = metricName;
            this.metricValue = metricValue;
            this.metricUnit = metricUnit;
            this.sourceUrl = sourceUrl;
        }

        private static ReferenceData from(
                FinancialGoalRecommendationBasis basis
        ) {
            return new ReferenceData(
                    basis.getOrganization(),
                    basis.getDatasetName(),
                    basis.getReferenceYear(),
                    basis.getMetricName(),
                    basis.getMetricValue(),
                    basis.getMetricUnit(),
                    basis.getSourceUrl()
            );
        }
    }

    @Getter
    @ApiModel(value = "FinancialGoalAmountRecommendationItem")
    public static class Item {
        @JsonProperty("recommendation_code")
        private final String recommendationCode;

        private final String title;

        @JsonProperty("target_amount")
        private final BigDecimal targetAmount;

        @JsonProperty("coverage_items")
        private final List<String> coverageItems;

        @JsonProperty("display_order")
        private final Integer displayOrder;

        private Item(
                String recommendationCode,
                String title,
                BigDecimal targetAmount,
                List<String> coverageItems,
                Integer displayOrder
        ) {
            this.recommendationCode = recommendationCode;
            this.title = title;
            this.targetAmount = targetAmount;
            this.coverageItems = List.copyOf(coverageItems);
            this.displayOrder = displayOrder;
        }

        private static Item from(
                FinancialGoalAmountRecommendation recommendation
        ) {
            return new Item(
                    recommendation.getRecommendationCode(),
                    recommendation.getTitle(),
                    recommendation.getTargetAmount(),
                    splitCoverageItems(recommendation.getCoverageItems()),
                    recommendation.getDisplayOrder()
            );
        }

        private static List<String> splitCoverageItems(String value) {
            if (value == null || value.isBlank()) {
                return List.of();
            }
            return Arrays.stream(value.split("\\|"))
                    .map(String::trim)
                    .filter(item -> !item.isEmpty())
                    .collect(Collectors.toList());
        }
    }
}
