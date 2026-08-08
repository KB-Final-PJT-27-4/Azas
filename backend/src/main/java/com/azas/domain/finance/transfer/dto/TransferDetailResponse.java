package com.azas.domain.finance.transfer.dto;

import com.azas.domain.finance.transfer.entity.TransferStatus;
import com.azas.domain.finance.transfer.entity.TransferType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class TransferDetailResponse {

    @JsonProperty("financial_transfer_id")
    private Long financialTransferId;

    @JsonProperty("financial_goal_id")
    private Long financialGoalId;

    @JsonProperty("requested_by_member_id")
    private Long requestedByMemberId;

    @JsonProperty("source_account_id")
    private Long sourceAccountId;

    @JsonProperty("destination_account_id")
    private Long destinationAccountId;

    private BigDecimal amount;
    private String memo;

    @JsonProperty("transfer_type")
    private TransferType transferType;

    private TransferStatus status;

    @JsonProperty("failure_code")
    private String failureCode;

    @JsonProperty("failure_message")
    private String failureMessage;

    @JsonProperty("matched_transaction_id")
    private Long matchedTransactionId;

    @JsonProperty("requested_at")
    private LocalDateTime requestedAt;

    @JsonProperty("completed_at")
    private LocalDateTime completedAt;
}