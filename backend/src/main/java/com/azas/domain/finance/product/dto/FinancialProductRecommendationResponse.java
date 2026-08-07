package com.azas.domain.finance.product.dto;

import com.azas.domain.finance.product.entity.FinancialProduct;
import com.azas.domain.finance.product.entity.RecommendationAccountBasis;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
public class FinancialProductRecommendationResponse {

    @JsonProperty("child_id")
    private final long childId;
    private final Basis basis;
    private final List<Item> items;
    @JsonProperty("next_cursor")
    private final String nextCursor;

    public FinancialProductRecommendationResponse(
            long childId,
            RecommendationAccountBasis accountBasis,
            BigDecimal monthlyAmount,
            List<FinancialProduct> products,
            Set<Long> bookmarkedProductIds,
            Integer childAge,
            String nextCursor
    ) {
        this.childId = childId;
        this.basis = Basis.from(accountBasis, monthlyAmount);
        this.items = new ArrayList<>();
        for (FinancialProduct product : products) {
            boolean bookmarked = bookmarkedProductIds.contains(
                    product.getFinancialProductId()
            );
            this.items.add(Item.from(
                    product,
                    childAge,
                    monthlyAmount,
                    bookmarked
            ));
        }
        this.nextCursor = nextCursor;
    }

    @Getter
    public static class Basis {
        @JsonProperty("account_id")
        private final Long accountId;
        @JsonProperty("goal_name")
        private final String goalName;
        @JsonProperty("goal_target_amount")
        private final BigDecimal goalTargetAmount;
        @JsonProperty("goal_target_date")
        private final LocalDate goalTargetDate;
        @JsonProperty("monthly_amount")
        private final BigDecimal monthlyAmount;

        private Basis(
                Long accountId,
                String goalName,
                BigDecimal goalTargetAmount,
                LocalDate goalTargetDate,
                BigDecimal monthlyAmount
        ) {
            this.accountId = accountId;
            this.goalName = goalName;
            this.goalTargetAmount = goalTargetAmount;
            this.goalTargetDate = goalTargetDate;
            this.monthlyAmount = monthlyAmount;
        }

        static Basis from(
                RecommendationAccountBasis accountBasis,
                BigDecimal monthlyAmount
        ) {
            if (accountBasis == null) {
                return new Basis(null, null, null, null, monthlyAmount);
            }
            return new Basis(
                    accountBasis.getFinancialAccountId(),
                    accountBasis.getGoalName(),
                    accountBasis.getGoalTargetAmount(),
                    accountBasis.getGoalTargetDate(),
                    monthlyAmount
            );
        }
    }

    @Getter
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
        @JsonProperty("product_image_key")
        private final String productImageKey;
        @JsonProperty("base_interest_rate")
        private final BigDecimal baseInterestRate;
        @JsonProperty("max_interest_rate")
        private final BigDecimal maxInterestRate;
        @JsonProperty("contract_period_months")
        private final Integer contractPeriodMonths;
        @JsonProperty("max_monthly_amount")
        private final BigDecimal maxMonthlyAmount;
        @JsonProperty("match_score")
        private final int matchScore;
        @JsonProperty("recommendation_reason")
        private final String recommendationReason;
        @JsonProperty("is_bookmarked")
        private final boolean bookmarked;

        private Item(
                FinancialProduct product,
                int matchScore,
                String recommendationReason,
                boolean bookmarked
        ) {
            this.financialProductId = product.getFinancialProductId();
            this.bankName = product.getBankName();
            this.name = product.getName();
            this.productType = product.getProductType();
            this.productSubtype = product.getProductSubtype();
            this.productImageKey = product.getProductImageKey();
            this.baseInterestRate = product.getBaseInterestRate();
            this.maxInterestRate = product.getMaxInterestRate();
            this.contractPeriodMonths = product.getContractPeriodMonths();
            this.maxMonthlyAmount = product.getMaxMonthlyAmount();
            this.matchScore = matchScore;
            this.recommendationReason = recommendationReason;
            this.bookmarked = bookmarked;
        }

        static Item from(
                FinancialProduct product,
                Integer childAge,
                BigDecimal monthlyAmount,
                boolean bookmarked
        ) {
            int score = score(product, childAge, monthlyAmount);
            return new Item(
                    product,
                    score,
                    "자녀 연령과 월 납입 가능 금액에 적합합니다.",
                    bookmarked
            );
        }

        private static int score(
                FinancialProduct product,
                Integer childAge,
                BigDecimal monthlyAmount
        ) {
            int score = "SAVING".equals(product.getProductType()) ? 55 : 50;
            if (childAge != null) {
                boolean aboveMinimum = product.getMinAge() == null
                        || childAge >= product.getMinAge();
                boolean belowMaximum = product.getMaxAge() == null
                        || childAge <= product.getMaxAge();
                score += aboveMinimum && belowMaximum ? 25 : -25;
            }
            if (monthlyAmount != null) {
                boolean aboveMinimum = product.getMinMonthlyAmount() == null
                        || monthlyAmount.compareTo(product.getMinMonthlyAmount()) >= 0;
                boolean belowMaximum = product.getMaxMonthlyAmount() == null
                        || monthlyAmount.compareTo(product.getMaxMonthlyAmount()) <= 0;
                score += aboveMinimum && belowMaximum ? 20 : 5;
            }
            return Math.max(0, Math.min(100, score));
        }
    }
}
