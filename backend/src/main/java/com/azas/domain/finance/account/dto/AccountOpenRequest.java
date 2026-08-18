package com.azas.domain.finance.account.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class AccountOpenRequest {

    @JsonProperty("owner_type")
    private String ownerType;

    @JsonProperty("child_id")
    private Long childId;

    @JsonProperty("financial_product_id")
    private Long financialProductId;

    @JsonProperty("initial_deposit_amount")
    private BigDecimal initialDepositAmount;
}
