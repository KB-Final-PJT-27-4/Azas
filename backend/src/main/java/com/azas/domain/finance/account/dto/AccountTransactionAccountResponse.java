package com.azas.domain.finance.account.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@ApiModel(description = "거래 입금·출금 계좌 정보")
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AccountTransactionAccountResponse {

    @ApiModelProperty(value = "서비스 내부 금융 계좌 ID. 외부 상대 계좌는 null",
            example = "3")
    @JsonProperty("account_id")
    private final Long accountId;

    @ApiModelProperty(value = "은행명. 확인할 수 없으면 null", example = "KB국민은행")
    @JsonProperty("bank_name")
    private final String bankName;

    @ApiModelProperty(value = "계좌명 또는 저장된 상대방명. 확인할 수 없으면 null",
            example = "아이사랑적금1")
    @JsonProperty("account_name")
    private final String accountName;

    @ApiModelProperty(value = "복호화된 전체 계좌번호. 확인할 수 없으면 null",
            example = "952-17362605-43")
    @JsonProperty("account_number")
    private final String accountNumber;

    public static AccountTransactionAccountResponse from(
            AccountTransactionAccountResult result
    ) {
        return new AccountTransactionAccountResponse(
                result.getAccountId(),
                result.getBankName(),
                result.getAccountName(),
                result.getAccountNumber()
        );
    }
}
