package com.azas.domain.timecapsule.dto;

import com.azas.domain.timecapsule.entity.TimeCapsuleAccount;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.Getter;

@ApiModel(description = "타임캡슐 연결 계좌 요약")
@Getter
public class TimeCapsuleAccountResponse {

    @JsonProperty("account_id")
    private final Long accountId;

    @JsonProperty("owner_type")
    private final String ownerType;

    @JsonProperty("account_name")
    private final String accountName;

    @JsonProperty("account_product_type")
    private final String accountProductType;

    private TimeCapsuleAccountResponse(TimeCapsuleAccount account) {
        this.accountId = account.getFinancialAccountId();
        this.ownerType = account.getOwnerType();
        this.accountName = account.getAccountName();
        this.accountProductType = account.getAccountProductType();
    }

    public static TimeCapsuleAccountResponse from(
            TimeCapsuleAccount account
    ) {
        return new TimeCapsuleAccountResponse(account);
    }
}
