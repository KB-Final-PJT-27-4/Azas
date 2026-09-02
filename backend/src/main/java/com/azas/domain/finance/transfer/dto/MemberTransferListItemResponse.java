package com.azas.domain.finance.transfer.dto;

import com.azas.domain.finance.transfer.entity.TransferStatus;
import com.azas.domain.finance.transfer.entity.TransferType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberTransferListItemResponse {

    @JsonProperty("financial_transfer_id")
    private Long financialTransferId;

    @JsonProperty("child_id")
    private Long childId;

    @JsonProperty("requested_by_member_id")
    private Long requestedByMemberId;

    @JsonProperty("transfer_type")
    private TransferType transferType;

    @JsonProperty("source_account")
    private TransferAccountResponse sourceAccount;

    @JsonProperty("destination_account")
    private TransferAccountResponse destinationAccount;

    private BigDecimal amount;
    private String memo;
    private TransferStatus status;

    @JsonProperty("failure_code")
    private String failureCode;

    @JsonProperty("failure_message")
    private String failureMessage;

    @JsonProperty("requested_at")
    private LocalDateTime requestedAt;

    @JsonProperty("completed_at")
    private LocalDateTime completedAt;
}