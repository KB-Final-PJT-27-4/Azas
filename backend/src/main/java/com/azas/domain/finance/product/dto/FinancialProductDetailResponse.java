package com.azas.domain.finance.product.dto;

import com.azas.domain.finance.product.entity.FinancialProduct;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
public class FinancialProductDetailResponse {

    @JsonProperty("financial_product_id")
    private final Long financialProductId;
    @JsonProperty("external_product_id")
    private final String externalProductId;
    @JsonProperty("bank_name")
    private final String bankName;
    @JsonProperty("product_type")
    private final String productType;
    @JsonProperty("product_subtype")
    private final String productSubtype;
    private final String name;
    private final String summary;
    @JsonProperty("detail_url")
    private final String detailUrl;
    @JsonProperty("product_image_key")
    private final String productImageKey;
    private final List<Badge> badges;
    @JsonProperty("interest_rate")
    private final InterestRate interestRate;
    @JsonProperty("available_age")
    private final AvailableAge availableAge;
    private final Contract contract;
    @JsonProperty("deposit_conditions")
    private final JsonNode depositConditions;
    @JsonProperty("interest_payment_method")
    private final InterestPaymentMethod interestPaymentMethod;
    @JsonProperty("eligibility_conditions")
    private final JsonNode eligibilityConditions;
    @JsonProperty("preferential_conditions")
    private final JsonNode preferentialConditions;
    @JsonProperty("additional_benefits")
    private final JsonNode additionalBenefits;
    private final JsonNode cautions;
    private final Recommendation recommendation;
    @JsonProperty("is_bookmarked")
    private final boolean bookmarked;
    @JsonProperty("source_base_date")
    private final LocalDate sourceBaseDate;
    @JsonProperty("updated_at")
    private final LocalDateTime updatedAt;

    private FinancialProductDetailResponse(
            FinancialProduct product,
            boolean bookmarked,
            Recommendation recommendation,
            ObjectMapper objectMapper
    ) {
        this.financialProductId = product.getFinancialProductId();
        this.externalProductId = product.getExternalProductId();
        this.bankName = product.getBankName();
        this.productType = product.getProductType();
        this.productSubtype = product.getProductSubtype();
        this.name = product.getName();
        this.summary = product.getSummary();
        this.detailUrl = product.getDetailUrl();
        this.productImageKey = product.getProductImageKey();
        this.badges = badges(product, recommendation != null);
        this.interestRate = new InterestRate(
                product.getBaseInterestRate(),
                product.getMaxInterestRate()
        );
        this.availableAge = new AvailableAge(
                product.getMinAge(),
                product.getMaxAge()
        );
        this.contract = new Contract(
                product.getContractPeriodMonths(),
                product.getRenewalDescription()
        );
        this.depositConditions = readArray(
                objectMapper,
                product.getDepositConditionsJson()
        );
        this.interestPaymentMethod = interestPaymentMethod(
                product.getInterestPaymentMethod()
        );
        this.eligibilityConditions = readArray(
                objectMapper,
                product.getEligibilityConditionsJson()
        );
        this.preferentialConditions = readArray(
                objectMapper,
                product.getPreferentialConditionsJson()
        );
        this.additionalBenefits = readArray(
                objectMapper,
                product.getAdditionalBenefitsJson()
        );
        this.cautions = readArray(objectMapper, product.getCautionsJson());
        this.recommendation = recommendation;
        this.bookmarked = bookmarked;
        this.sourceBaseDate = product.getSourceBaseDate();
        this.updatedAt = product.getUpdatedAt();
    }

    public static FinancialProductDetailResponse from(
            FinancialProduct product,
            boolean bookmarked,
            Recommendation recommendation,
            ObjectMapper objectMapper
    ) {
        return new FinancialProductDetailResponse(
                product,
                bookmarked,
                recommendation,
                objectMapper
        );
    }

    public static Recommendation recommendation(
            FinancialProduct product,
            Integer childAge,
            BigDecimal monthlyAmount
    ) {
        int score = FinancialProductRecommendationResponse.Item.from(
                product,
                childAge,
                monthlyAmount,
                false
        ).getMatchScore();
        return new Recommendation(
                "자녀의 나이와 적금 목표에 적합한 상품입니다.",
                score
        );
    }

    private static JsonNode readArray(ObjectMapper objectMapper, String json) {
        if (json == null || json.isBlank()) {
            return objectMapper.createArrayNode();
        }
        try {
            return objectMapper.readTree(json);
        } catch (Exception exception) {
            return objectMapper.createArrayNode();
        }
    }

    private static InterestPaymentMethod interestPaymentMethod(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return new InterestPaymentMethod(null, null);
        }
        if ("MATURITY_LUMP_SUM".equals(value)
                || "만기일시지급식".equals(value)) {
            return new InterestPaymentMethod(
                    "MATURITY_LUMP_SUM",
                    "만기일시지급식"
            );
        }
        return new InterestPaymentMethod(value, value);
    }

    private static List<Badge> badges(
            FinancialProduct product,
            boolean recommended
    ) {
        List<Badge> badges = new ArrayList<>();
        if (product.getProductType() != null) {
            badges.add(new Badge(
                    "PRODUCT_TYPE",
                    productTypeLabel(product.getProductType())
            ));
        }
        if (product.getMaxAge() != null && product.getMaxAge() <= 19) {
            badges.add(new Badge("CHILD_YOUTH_ONLY", "어린이·청소년 전용"));
        }
        if (product.getProductSubtype() != null) {
            badges.add(new Badge("PRODUCT_SUBTYPE", product.getProductSubtype()));
        }
        if (recommended) {
            badges.add(new Badge("RECOMMENDED", "깨비추천"));
        }
        return badges;
    }

    private static String productTypeLabel(String productType) {
        if ("SAVING".equals(productType)) {
            return "적금";
        }
        if ("DEPOSIT".equals(productType)) {
            return "예금";
        }
        return productType;
    }

    @Getter
    public static class Badge {
        private final String code;
        private final String label;
        Badge(String code, String label) {
            this.code = code;
            this.label = label;
        }
    }

    @Getter
    public static class InterestRate {
        @JsonProperty("base_rate")
        private final BigDecimal baseRate;
        @JsonProperty("max_rate")
        private final BigDecimal maxRate;
        InterestRate(BigDecimal baseRate, BigDecimal maxRate) {
            this.baseRate = baseRate;
            this.maxRate = maxRate;
        }
    }

    @Getter
    public static class AvailableAge {
        @JsonProperty("min_age")
        private final Integer minAge;
        @JsonProperty("max_age")
        private final Integer maxAge;
        AvailableAge(Integer minAge, Integer maxAge) {
            this.minAge = minAge;
            this.maxAge = maxAge;
        }
    }

    @Getter
    public static class Contract {
        @JsonProperty("period_months")
        private final Integer periodMonths;
        @JsonProperty("renewal_description")
        private final String renewalDescription;
        Contract(Integer periodMonths, String renewalDescription) {
            this.periodMonths = periodMonths;
            this.renewalDescription = renewalDescription;
        }
    }

    @Getter
    public static class InterestPaymentMethod {
        private final String code;
        private final String label;
        InterestPaymentMethod(String code, String label) {
            this.code = code;
            this.label = label;
        }
    }

    @Getter
    public static class Recommendation {
        private final String reason;
        @JsonProperty("match_score")
        private final int matchScore;
        Recommendation(String reason, int matchScore) {
            this.reason = reason;
            this.matchScore = matchScore;
        }
    }
}
