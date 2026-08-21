package com.azas.domain.finance.account.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;

@Getter
@RequiredArgsConstructor
@ApiModel(value = "FinancialAccountLinkedAccountResponse")
public class LinkedAccountResponse {
    @JsonProperty("account_id") private final Long accountId;
    @JsonProperty("owner_type") private final String ownerType;
    @JsonProperty("child_id") private final Long childId;
    @JsonProperty("bank_name") private final String bankName;
    @JsonProperty("account_name") private final String accountName;
    @JsonProperty("account_number") private final String accountNumber;
    @JsonProperty("account_product_type") private final String accountProductType;
    private final BigDecimal balance;
    @JsonProperty("account_status") private final String accountStatus;
    @JsonProperty("link_status") private final String linkStatus;
    @JsonProperty("is_primary") private final boolean primary;
    @JsonProperty("requires_goal_setup") private final boolean requiresGoalSetup;
    @JsonProperty("linked_at") private final Instant linkedAt;

    public static LinkedAccountResponse from(LinkedAccountResult result) {
        return new LinkedAccountResponse(
                result.getAccountId(), result.getOwnerType(),
                result.getChildId(), result.getBankName(),
                result.getAccountName(), result.getAccountNumber(),
                result.getAccountProductType(), result.getBalance(),
                result.getAccountStatus(), result.getLinkStatus(),
                result.isPrimary(), result.isRequiresGoalSetup(),
                result.getLinkedAt().toInstant(ZoneOffset.UTC)
        );
    }
}
