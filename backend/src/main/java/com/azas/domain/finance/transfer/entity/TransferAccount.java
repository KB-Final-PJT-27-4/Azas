package com.azas.domain.finance.transfer.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class TransferAccount {

    private Long financialAccountId;
    private Long childId;
    private Long financialGoalId;
    private String ownerType;
    private Long ownerMemberId;
    private String bankName;

    // 테스트나 Mock 응답에서 사용할 평문 계좌번호
    private String accountNumber;

    // DB에서 조회하는 암호화 계좌번호
    private byte[] accountNumberCiphertext;

    private String accountName;
    private String accountProductType;
    private String accountStatus;
    private String linkStatus;
    private BigDecimal balance;

}
