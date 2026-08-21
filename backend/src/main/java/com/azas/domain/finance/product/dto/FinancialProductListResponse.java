package com.azas.domain.finance.product.dto;

import com.azas.domain.finance.product.entity.FinancialProduct;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
public class FinancialProductListResponse {

    private final List<Item> items;
    @JsonProperty("next_cursor")
    private final String nextCursor;
    @JsonProperty("has_next")
    private final boolean hasNext;

    public FinancialProductListResponse(
            List<Item> items,
            String nextCursor,
            boolean hasNext
    ) {
        this.items = items;
        this.nextCursor = nextCursor;
        this.hasNext = hasNext;
    }

    @Getter
    @ApiModel(value = "FinancialProductListItemResponse")
    public static class Item {
        @JsonProperty("financial_product_id")
        private final Long financialProductId;
        @JsonProperty("bank_name")
        private final String bankName;
        private final String name;
        @JsonProperty("product_type")
        private final String productType;
        @JsonProperty("product_subtype")
        private final String productSubtype;
        @JsonProperty("target_owner_type")
        private final String targetOwnerType;
        @JsonProperty("highlight_label")
        private final String highlightLabel;
        private final String summary;
        private final List<String> hashtags;
        @JsonProperty("max_interest_rate")
        private final BigDecimal maxInterestRate;
        @JsonProperty("contract_period")
        private final ContractPeriod contractPeriod;

        private Item(FinancialProduct product, List<String> hashtags) {
            this.financialProductId = product.getFinancialProductId();
            this.bankName = product.getBankName();
            this.name = product.getName();
            this.productType = product.getProductType();
            this.productSubtype = product.getProductSubtype();
            this.targetOwnerType = product.getTargetOwnerType();
            this.highlightLabel = product.getHighlightLabel();
            this.summary = product.getSummary();
            this.hashtags = List.copyOf(hashtags);
            this.maxInterestRate = product.getMaxInterestRate();
            this.contractPeriod = ContractPeriod.from(product);
        }

        public static Item from(
                FinancialProduct product,
                List<String> hashtags
        ) {
            return new Item(product, hashtags);
        }
    }

    @Getter
    @ApiModel(value = "FinancialProductListContractPeriodResponse")
    public static class ContractPeriod {
        @JsonProperty("min_months")
        private final Integer minMonths;
        @JsonProperty("max_months")
        private final Integer maxMonths;

        private ContractPeriod(Integer minMonths, Integer maxMonths) {
            this.minMonths = minMonths;
            this.maxMonths = maxMonths;
        }

        private static ContractPeriod from(FinancialProduct product) {
            Integer minMonths = product.getMinContractPeriodMonths();
            Integer maxMonths = product.getMaxContractPeriodMonths();
            if (minMonths == null && maxMonths == null
                    && product.getContractPeriodMonths() != null) {
                minMonths = product.getContractPeriodMonths();
                maxMonths = product.getContractPeriodMonths();
            }
            return new ContractPeriod(minMonths, maxMonths);
        }
    }
}
