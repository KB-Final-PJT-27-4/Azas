package com.azas.domain.finance.product.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class FinancialProductBookmarkResponse {

    @JsonProperty("child_id")
    private final long childId;

    @JsonProperty("financial_product_id")
    private final long financialProductId;

    @JsonProperty("is_bookmarked")
    private final boolean bookmarked;

    public FinancialProductBookmarkResponse(
            long childId,
            long financialProductId,
            boolean bookmarked
    ) {
        this.childId = childId;
        this.financialProductId = financialProductId;
        this.bookmarked = bookmarked;
    }
}
