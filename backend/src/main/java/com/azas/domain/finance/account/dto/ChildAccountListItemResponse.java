package com.azas.domain.finance.account.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@ApiModel(description = "자녀 계좌 목록 항목")
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ChildAccountListItemResponse {

    @ApiModelProperty(value = "금융 계좌 ID", required = true, example = "5")
    @JsonProperty("account_id")
    private final Long accountId;

    @ApiModelProperty(value = "계좌명", required = true,
            example = "아이사랑적금1")
    @JsonProperty("account_name")
    private final String accountName;

    @ApiModelProperty(value = "복호화한 전체 계좌번호", required = true,
            example = "952-17362605-43")
    @JsonProperty("account_number")
    private final String accountNumber;

    @ApiModelProperty(value = "계좌 상품 유형", required = true,
            allowableValues = "DEMAND_DEPOSIT,SAVINGS,SUBSCRIPTION",
            example = "SAVINGS")
    @JsonProperty("account_product_type")
    private final String accountProductType;

    @ApiModelProperty(value = "현재 잔액", required = true,
            example = "9600000.00")
    private final BigDecimal balance;

    public static ChildAccountListItemResponse from(
            ChildAccountListItemResult result
    ) {
        return new ChildAccountListItemResponse(
                result.getAccountId(),
                result.getAccountName(),
                result.getAccountNumber(),
                result.getAccountProductType(),
                result.getBalance()
        );
    }
}
