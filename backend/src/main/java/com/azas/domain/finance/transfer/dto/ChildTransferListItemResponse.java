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
public class ChildTransferListItemResponse {

    @JsonProperty("financial_transfer_id")
    private Long financialTransferId;

    @JsonProperty("financial_goal_id")
    private Long financialGoalId;

    @JsonProperty("goal_title")
    private String goalTitle;

    private BigDecimal amount;

    @JsonProperty("transfer_type")
    private TransferType transferType;

    private TransferStatus status;

    @JsonProperty("requested_at")
    private LocalDateTime requestedAt;

    @JsonProperty("completed_at")
    private LocalDateTime completedAt;
}