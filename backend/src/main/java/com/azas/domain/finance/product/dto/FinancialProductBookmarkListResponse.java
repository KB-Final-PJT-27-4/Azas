package com.azas.domain.finance.product.dto;

import com.azas.domain.finance.product.entity.FinancialProduct;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
public class FinancialProductBookmarkListResponse {

    private final List<Item> content;
    private final int page;
    private final int size;

    @JsonProperty("total_elements")
    private final long totalElements;

    @JsonProperty("total_pages")
    private final int totalPages;

    @JsonProperty("has_next")
    private final boolean hasNext;

    public FinancialProductBookmarkListResponse(
            List<FinancialProduct> products,
            int page,
            int size,
            long totalElements
    ) {
        this.content = new ArrayList<>();
        for (FinancialProduct product : products) {
            this.content.add(Item.from(product));
        }
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalElements == 0
                ? 0
                : (int) Math.ceil((double) totalElements / size);
        this.hasNext = page + 1 < totalPages;
    }

    @Getter
    @ApiModel(value = "FinancialProductBookmarkListItemResponse")
    public static class Item {

        @JsonProperty("financial_product_bookmark_id")
        private final Long financialProductBookmarkId;
        @JsonProperty("financial_product_id")
        private final Long financialProductId;
        @JsonProperty("bank_name")
        private final String bankName;
        private final String name;
        @JsonProperty("product_type")
        private final String productType;
        @JsonProperty("product_subtype")
        private final String productSubtype;
        private final String summary;
        @JsonProperty("base_interest_rate")
        private final BigDecimal baseInterestRate;
        @JsonProperty("max_interest_rate")
        private final BigDecimal maxInterestRate;
        @JsonProperty("contract_period_months")
        private final Integer contractPeriodMonths;
        @JsonProperty("max_monthly_amount")
        private final BigDecimal maxMonthlyAmount;
        @JsonProperty("display_badges")
        private final List<String> displayBadges;
        @JsonProperty("product_image_key")
        private final String productImageKey;
        @JsonProperty("detail_url")
        private final String detailUrl;
        @JsonProperty("source_base_date")
        private final LocalDate sourceBaseDate;
        @JsonProperty("bookmarked_at")
        private final LocalDateTime bookmarkedAt;

        private Item(FinancialProduct product) {
            this.financialProductBookmarkId = product.getFinancialProductBookmarkId();
            this.financialProductId = product.getFinancialProductId();
            this.bankName = product.getBankName();
            this.name = product.getName();
            this.productType = product.getProductType();
            this.productSubtype = product.getProductSubtype();
            this.summary = product.getSummary();
            this.baseInterestRate = product.getBaseInterestRate();
            this.maxInterestRate = product.getMaxInterestRate();
            this.contractPeriodMonths = product.getContractPeriodMonths();
            this.maxMonthlyAmount = product.getMaxMonthlyAmount();
            this.displayBadges = displayBadges(product);
            this.productImageKey = product.getProductImageKey();
            this.detailUrl = product.getDetailUrl();
            this.sourceBaseDate = product.getSourceBaseDate();
            this.bookmarkedAt = product.getBookmarkedAt();
        }

        static Item from(FinancialProduct product) {
            return new Item(product);
        }

        private static List<String> displayBadges(FinancialProduct product) {
            List<String> badges = new ArrayList<>();
            if ("SAVING".equals(product.getProductType())) {
                badges.add("적금");
            } else if (product.getProductType() != null) {
                badges.add(product.getProductType());
            }
            if (product.getMaxAge() != null && product.getMaxAge() <= 19) {
                badges.add("어린이·청소년 전용");
            }
            return badges;
        }
    }
}
