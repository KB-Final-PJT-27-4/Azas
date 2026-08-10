package com.azas.domain.finance.account.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@ApiModel(description = "계좌 상세 조회 응답")
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AccountDetailResponse {

    @ApiModelProperty(value = "금융 계좌 ID", required = true, example = "2")
    @JsonProperty("account_id")
    private final Long accountId;

    @ApiModelProperty(value = "계좌 소유 유형", required = true,
            allowableValues = "PARENT,CHILD", example = "CHILD")
    @JsonProperty("owner_type")
    private final String ownerType;

    @ApiModelProperty(value = "자녀 명의 계좌의 자녀 정보. 부모 명의 계좌는 null")
    private final AccountDetailChildResponse child;

    @ApiModelProperty(value = "금융기관 코드", required = true, example = "004")
    @JsonProperty("organization_code")
    private final String organizationCode;

    @ApiModelProperty(value = "은행명", required = true, example = "KB국민은행")
    @JsonProperty("bank_name")
    private final String bankName;

    @ApiModelProperty(value = "계좌명", required = true, example = "KB Young Youth 입출금통장")
    @JsonProperty("account_name")
    private final String accountName;

    @ApiModelProperty(value = "복호화된 전체 계좌번호", required = true,
            example = "123-4567-8901")
    @JsonProperty("account_number")
    private final String accountNumber;

    @ApiModelProperty(value = "계좌 상품 유형", required = true,
            allowableValues = "DEMAND_DEPOSIT,SAVINGS,SUBSCRIPTION",
            example = "DEMAND_DEPOSIT")
    @JsonProperty("account_product_type")
    private final String accountProductType;

    @ApiModelProperty(value = "마지막 동기화 기준 잔액", required = true,
            example = "1250000.00")
    private final BigDecimal balance;

    @ApiModelProperty(value = "잔액 기준 시각", example = "2026-08-09T05:30:00Z")
    @JsonProperty("balance_updated_at")
    private final Instant balanceUpdatedAt;

    @ApiModelProperty(value = "계좌 상태", required = true,
            allowableValues = "ACTIVE,MATURED,CLOSED", example = "ACTIVE")
    @JsonProperty("account_status")
    private final String accountStatus;

    @ApiModelProperty(value = "대표 계좌 여부", required = true, example = "true")
    @JsonProperty("is_primary")
    private final boolean primary;

    @ApiModelProperty(value = "계좌 개설 시각", example = "2024-03-01T00:00:00Z")
    @JsonProperty("opened_at")
    private final Instant openedAt;

    @ApiModelProperty(value = "계좌 만기일", example = "2027-03-01")
    @JsonProperty("maturity_date")
    private final LocalDate maturityDate;

    @ApiModelProperty(value = "서비스 연결 시각", required = true,
            example = "2026-08-04T07:29:20Z")
    @JsonProperty("linked_at")
    private final Instant linkedAt;

    @ApiModelProperty(value = "계좌 금융 목표. 설정되지 않은 경우 null")
    @JsonProperty("financial_goal")
    private final AccountFinancialGoalResponse financialGoal;

    public static AccountDetailResponse from(AccountDetailResult result) {
        return new AccountDetailResponse(
                result.getAccountId(),
                result.getOwnerType(),
                result.getChild() == null
                        ? null
                        : AccountDetailChildResponse.from(result.getChild()),
                result.getOrganizationCode(),
                result.getBankName(),
                result.getAccountName(),
                result.getAccountNumber(),
                result.getAccountProductType(),
                result.getBalance(),
                toInstant(result.getBalanceUpdatedAt()),
                result.getAccountStatus(),
                result.isPrimary(),
                toInstant(result.getOpenedAt()),
                result.getMaturityDate(),
                toInstant(result.getLinkedAt()),
                result.getFinancialGoal() == null
                        ? null
                        : AccountFinancialGoalResponse.from(
                                result.getFinancialGoal()
                        )
        );
    }

    private static Instant toInstant(LocalDateTime dateTime) {
        return dateTime == null
                ? null
                : dateTime.toInstant(ZoneOffset.UTC);
    }
}
