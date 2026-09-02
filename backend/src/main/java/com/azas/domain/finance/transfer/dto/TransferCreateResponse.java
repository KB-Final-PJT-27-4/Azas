package com.azas.domain.finance.transfer.dto;

import com.azas.domain.finance.transfer.entity.TransferStatus;
import com.azas.domain.finance.transfer.entity.TransferType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@AllArgsConstructor
public class TransferCreateResponse {

    @JsonProperty("financial_transfer_id")
    private final Long financialTransferId;

    @JsonProperty("financial_goal_id")
    private final Long financialGoalId;

    @JsonProperty("source_account")
    private final TransferAccountResponse sourceAccount;

    @JsonProperty("destination_account")
    private final TransferAccountResponse destinationAccount;

    private final BigDecimal amount;
    private final String memo;

    @JsonProperty("transfer_type")
    private final TransferType transferType;

    private final TransferStatus status;

    @JsonProperty("requested_at")
    private final Instant requestedAt;
}