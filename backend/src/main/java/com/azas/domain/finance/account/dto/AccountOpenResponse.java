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
    @JsonProperty("financial_goal") private final OpenedFinancialGoalResult financialGoal;
    @JsonProperty("created_at") private final Instant createdAt;

    public static AccountOpenResponse from(AccountOpenResult r) {
        return new AccountOpenResponse(r.getAccountId(), r.getOwnerType(),
                r.getChildId(), r.getFinancialProductId(), r.getBankName(),
                r.getAccountName(), r.getAccountNumber(), r.getAccountProductType(),
                r.getBalance(), "ACTIVE", "ACTIVE", r.isPrimary(),
                r.getFinancialGoal(), r.getCreatedAt().toInstant(ZoneOffset.UTC));
    }
}
