package com.azas.domain.finance.product.dto;

import com.azas.domain.finance.product.entity.FinancialProduct;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiModel;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
public class FinancialProductDetailResponse {

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
    private final String summary;
    private final List<Badge> badges;
    @JsonProperty("curation_reason")
    private final String curationReason;
    @JsonProperty("interest_rate")
    private final InterestRate interestRate;
    @JsonProperty("contract_period")
    private final ContractPeriod contractPeriod;
    @JsonProperty("monthly_deposit")
    private final MonthlyDeposit monthlyDeposit;
    @JsonProperty("eligibility_conditions")
    private final JsonNode eligibilityConditions;
    @JsonProperty("interest_payment_method")
    private final InterestPaymentMethod interestPaymentMethod;
    @JsonProperty("join_termination_method")
    private final String joinTerminationMethod;
    @JsonProperty("preferential_conditions")
    private final JsonNode preferentialConditions;
    @JsonProperty("additional_benefits")
    private final JsonNode additionalBenefits;
    private final JsonNode cautions;
    @JsonProperty("is_bookmarked")
    private final boolean bookmarked;
    @JsonProperty("detail_url")
    private final String detailUrl;
    @JsonProperty("source_base_date")
    private final LocalDate sourceBaseDate;

    private FinancialProductDetailResponse(
            FinancialProduct product,
            boolean bookmarked,
            ObjectMapper objectMapper
    ) {
        this.financialProductId = product.getFinancialProductId();
        this.bankName = product.getBankName();
        this.name = product.getName();
        this.productType = product.getProductType();
        this.productSubtype = product.getProductSubtype();
        this.targetOwnerType = product.getTargetOwnerType();
        this.summary = product.getSummary();
        this.badges = badges(product);
        this.curationReason = product.getCurationReason();
        this.interestRate = new InterestRate(
                product.getBaseInterestRate(),
                product.getMaxInterestRate(),
                product.getInterestRateReference()
        );
        this.contractPeriod = ContractPeriod.from(product);
        this.monthlyDeposit = new MonthlyDeposit(
                product.getMinMonthlyAmount(),
                product.getMaxMonthlyAmount()
        );
        this.eligibilityConditions = readArray(
                objectMapper,
                product.getEligibilityConditionsJson()
        );
        this.interestPaymentMethod = interestPaymentMethod(
                product.getInterestPaymentMethod()
        );
        this.joinTerminationMethod = product.getJoinTerminationMethod();
        this.preferentialConditions = readArray(
                objectMapper,
                product.getPreferentialConditionsJson()
        );
        this.additionalBenefits = readArray(
                objectMapper,
                product.getAdditionalBenefitsJson()
        );
        this.cautions = readArray(objectMapper, product.getCautionsJson());
        this.bookmarked = bookmarked;
        this.detailUrl = product.getDetailUrl();
        this.sourceBaseDate = product.getSourceBaseDate();
    }

    public static FinancialProductDetailResponse from(
            FinancialProduct product,
            boolean bookmarked,
            ObjectMapper objectMapper
    ) {
        return new FinancialProductDetailResponse(
                product,
                bookmarked,
                objectMapper
        );
    }

    private static JsonNode readArray(ObjectMapper objectMapper, String json) {
        if (json == null || json.isBlank()) {
            return objectMapper.createArrayNode();
        }
        try {
            JsonNode node = objectMapper.readTree(json);
            if (!node.isArray()) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
            }
            return node;
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private static InterestPaymentMethod interestPaymentMethod(String value) {
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

    private static List<Badge> badges(FinancialProduct product) {
        List<Badge> badges = new ArrayList<>();
        if (product.getHighlightLabel() != null
                && !product.getHighlightLabel().isBlank()) {
            badges.add(new Badge("CURATION", product.getHighlightLabel()));
        }
        if (product.getMaxAge() != null && product.getMaxAge() <= 19) {
            badges.add(new Badge(
                    "CHILD_YOUTH_ONLY",
                    "어린이·청소년 전용"
            ));
        }
        if (product.getProductSubtype() != null
                && !product.getProductSubtype().isBlank()) {
            badges.add(new Badge(
                    "PRODUCT_SUBTYPE",
                    productSubtypeLabel(product.getProductSubtype())
            ));
        }
        return List.copyOf(badges);
    }

    private static String productSubtypeLabel(String productSubtype) {
        if ("자유적립식 예금".equals(productSubtype)) {
            return "자유적립식";
        }
        return productSubtype;
    }

    @Getter
    public static class Badge {
        private final String code;
        private final String label;

        private Badge(String code, String label) {
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
        private final String reference;

        private InterestRate(
                BigDecimal baseRate,
                BigDecimal maxRate,
                String reference
        ) {
            this.baseRate = baseRate;
            this.maxRate = maxRate;
            this.reference = reference;
        }
    }

    @Getter
    @ApiModel(value = "FinancialProductDetailContractPeriodResponse")
    public static class ContractPeriod {
        @JsonProperty("min_months")
        private final Integer minMonths;
        @JsonProperty("max_months")
        private final Integer maxMonths;
        @JsonProperty("renewal_description")
        private final String renewalDescription;

        private ContractPeriod(
                Integer minMonths,
                Integer maxMonths,
                String renewalDescription
        ) {
            this.minMonths = minMonths;
            this.maxMonths = maxMonths;
            this.renewalDescription = renewalDescription;
        }

        private static ContractPeriod from(FinancialProduct product) {
            Integer minMonths = product.getMinContractPeriodMonths();
            Integer maxMonths = product.getMaxContractPeriodMonths();
            if (minMonths == null && maxMonths == null
                    && product.getContractPeriodMonths() != null) {
                minMonths = product.getContractPeriodMonths();
                maxMonths = product.getContractPeriodMonths();
            }
            return new ContractPeriod(
                    minMonths,
                    maxMonths,
                    product.getRenewalDescription()
            );
        }
    }

    @Getter
    public static class MonthlyDeposit {
        @JsonProperty("min_amount")
        private final BigDecimal minAmount;
        @JsonProperty("max_amount")
        private final BigDecimal maxAmount;

        private MonthlyDeposit(
                BigDecimal minAmount,
                BigDecimal maxAmount
        ) {
            this.minAmount = minAmount;
            this.maxAmount = maxAmount;
        }
    }

    @Getter
    public static class InterestPaymentMethod {
        private final String code;
        private final String label;

        private InterestPaymentMethod(String code, String label) {
            this.code = code;
            this.label = label;
        }
    }
}
