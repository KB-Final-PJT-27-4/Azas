package com.azas.domain.finance.account.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@ApiModel(description = "계좌 상세 조회 응답")
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AccountDetailResponse {

    @ApiModelProperty(value = "금융 계좌 ID", required = true, example = "5")
    @JsonProperty("account_id")
    private final Long accountId;

    @ApiModelProperty(value = "계좌 소유 유형", required = true,
            allowableValues = "PARENT,CHILD", example = "CHILD")
    @JsonProperty("owner_type")
    private final String ownerType;

    @ApiModelProperty(value = "은행명", required = true, example = "KB국민은행")
    @JsonProperty("bank_name")
    private final String bankName;

    @ApiModelProperty(value = "계좌명", required = true,
            example = "KB Young Youth 입출금통장")
    @JsonProperty("account_name")
    private final String accountName;

    @ApiModelProperty(value = "복호화된 전체 계좌번호", required = true,
            example = "123-4567-8901")
    @JsonProperty("account_number")
    private final String accountNumber;

    @ApiModelProperty(value = "예금주명", required = true, example = "깨비")
    @JsonProperty("account_holder_name")
    private final String accountHolderName;

    @ApiModelProperty(value = "계좌 상품 유형", required = true,
            allowableValues = "DEMAND_DEPOSIT,SAVINGS,SUBSCRIPTION",
            example = "DEMAND_DEPOSIT")
    @JsonProperty("account_product_type")
    private final String accountProductType;

    @ApiModelProperty(value = "현재 잔액", required = true,
            example = "500000.00")
    private final BigDecimal balance;

    public static AccountDetailResponse from(AccountDetailResult result) {
        return new AccountDetailResponse(
                result.getAccountId(),
                result.getOwnerType(),
                result.getBankName(),
                result.getAccountName(),
                result.getAccountNumber(),
                result.getAccountHolderName(),
                result.getAccountProductType(),
                result.getBalance()
        );
    }
}
