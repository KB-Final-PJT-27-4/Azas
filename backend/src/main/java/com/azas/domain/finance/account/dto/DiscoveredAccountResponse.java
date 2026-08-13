package com.azas.domain.finance.account.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@ApiModel(description = "연결 가능한 Mock 계좌")
@Getter
@RequiredArgsConstructor
public class DiscoveredAccountResponse {

    @ApiModelProperty(value = "계좌 ID", required = true, example = "101")
    @JsonProperty("account_id")
    private final Long accountId;

    @ApiModelProperty(value = "은행명", required = true, example = "KB국민은행")
    @JsonProperty("bank_name")
    private final String bankName;

    @ApiModelProperty(value = "전체 계좌번호", required = true, example = "1234-567-890123")
    @JsonProperty("account_number")
    private final String accountNumber;

    @ApiModelProperty(value = "계좌 유형", required = true, allowableValues = "DEMAND_DEPOSIT,SAVINGS")
    @JsonProperty("account_product_type")
    private final String accountProductType;

    @ApiModelProperty(value = "Mock 현재 잔액", required = true, example = "12450000")
    private final BigDecimal balance;

    public static DiscoveredAccountResponse from(DiscoveredAccountResult result) {
        return new DiscoveredAccountResponse(
                result.getAccountId(),
                result.getBankName(),
                result.getAccountNumber(),
                result.getAccountProductType(),
                result.getBalance()
        );
    }
}
