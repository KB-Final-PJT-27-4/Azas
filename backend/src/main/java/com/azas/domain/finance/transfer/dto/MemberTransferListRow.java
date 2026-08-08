package com.azas.domain.finance.transfer.dto;

import com.azas.domain.finance.transfer.entity.TransferStatus;
import com.azas.domain.finance.transfer.entity.TransferType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class MemberTransferListRow {

    private Long financialTransferId;
    private Long childId;
    private Long requestedByMemberId;
    private TransferType transferType;
    private BigDecimal amount;
    private String memo;
    private TransferStatus status;
    private String failureCode;
    private String failureMessage;
    private LocalDateTime requestedAt;
    private LocalDateTime completedAt;

    private Long sourceAccountId;
    private String sourceBankName;
    private String sourceAccountName;
    private byte[] sourceAccountNumberCiphertext;

    private Long destinationAccountId;
    private String destinationBankName;
    private String destinationAccountName;
    private byte[] destinationAccountNumberCiphertext;
}