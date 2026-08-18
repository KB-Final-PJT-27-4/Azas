package com.azas.domain.finance.account.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;

@Getter
@RequiredArgsConstructor
public class AccountOpenResponse {
    @JsonProperty("account_id") private final Long accountId;
    @JsonProperty("owner_type") private final String ownerType;
    @JsonProperty("child_id") private final Long childId;
    @JsonProperty("financial_product_id") private final Long financialProductId;
    @JsonProperty("bank_name") private final String bankName;
    @JsonProperty("account_name") private final String accountName;
    @JsonProperty("account_number") private final String accountNumber;
    @JsonProperty("account_product_type") private final String accountProductType;
    private final BigDecimal balance;
    @JsonProperty("account_status") private final String accountStatus;
    @JsonProperty("link_status") private final String linkStatus;
    @JsonProperty("is_primary") private final boolean primary;
    @JsonProperty("created_at") private final Instant createdAt;

    public static AccountOpenResponse from(AccountOpenResult result) {
        return new AccountOpenResponse(
                result.getAccountId(),
                result.getOwnerType(),
                result.getChildId(),
                result.getFinancialProductId(),
                result.getBankName(),
                result.getAccountName(),
                result.getAccountNumber(),
                result.getAccountProductType(),
                result.getBalance(),
                "ACTIVE",
                "ACTIVE",
                result.isPrimary(),
                result.getCreatedAt().toInstant(ZoneOffset.UTC)
        );
    }
}
