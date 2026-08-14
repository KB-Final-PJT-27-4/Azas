package com.azas.domain.finance.product.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class FinancialProduct {

    private Long financialProductId;
    private Long financialProductBookmarkId;
    private String bankName;
    private String externalProductId;
    private String productType;
    private String targetOwnerType;
    private String productSubtype;
    private String name;
    private String summary;
    private String detailUrl;
    private String productImageKey;
    private BigDecimal baseInterestRate;
    private BigDecimal maxInterestRate;
    private Integer minAge;
    private Integer maxAge;
    private BigDecimal minMonthlyAmount;
    private BigDecimal maxMonthlyAmount;
    private Integer contractPeriodMonths;
    private String renewalDescription;
    private String interestPaymentMethod;
    private String eligibilityConditionsJson;
    private String depositConditionsJson;
    private String preferentialConditionsJson;
    private String additionalBenefitsJson;
    private String cautionsJson;
    private Boolean active;
    private LocalDate sourceBaseDate;
    private LocalDateTime bookmarkedAt;
    private LocalDateTime updatedAt;
}
