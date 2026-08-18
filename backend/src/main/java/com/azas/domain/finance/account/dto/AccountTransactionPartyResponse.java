package com.azas.domain.finance.account.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@ApiModel(description = "거래 입금처 또는 출금처")
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AccountTransactionPartyResponse {

    @ApiModelProperty(value = "은행명. 확인할 수 없으면 null",
            example = "KB국민은행")
    @JsonProperty("bank_name")
    private final String bankName;

    @ApiModelProperty(value = "계좌명 또는 거래 상대명. 확인할 수 없으면 null",
            example = "KB국민 5678")
    @JsonProperty("account_name")
    private final String accountName;

    @ApiModelProperty(value = "전체 계좌번호. 확인할 수 없으면 null",
            example = "123-456-789")
    @JsonProperty("account_number")
    private final String accountNumber;

    public static AccountTransactionPartyResponse from(
            AccountTransactionPartyResult result
    ) {
        return new AccountTransactionPartyResponse(
                result.getBankName(),
                result.getAccountName(),
                result.getAccountNumber()
        );
    }
}
