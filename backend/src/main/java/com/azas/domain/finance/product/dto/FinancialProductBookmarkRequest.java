package com.azas.domain.finance.product.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Getter
@NoArgsConstructor
public class FinancialProductBookmarkRequest {

    @NotNull
    @JsonProperty("is_bookmarked")
    private Boolean bookmarked;
}
