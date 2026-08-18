package com.azas.domain.finance.autotransfer.dto;

import com.azas.domain.finance.transfer.entity.TransferStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class AutoTransferRetryResponse {

    @JsonProperty("financial_transfer_id")
    private final Long financialTransferId;

    @JsonProperty("auto_transfer_schedule_id")
    private final Long autoTransferScheduleId;

    @JsonProperty("retry_of_transfer_id")
    private final Long retryOfTransferId;

    private final TransferStatus status;

    @JsonProperty("failure_code")
    private final String failureCode;

    @JsonProperty("failure_message")
    private final String failureMessage;

    @JsonProperty("requested_at")
    private final Instant requestedAt;

    @JsonProperty("completed_at")
    private final Instant completedAt;
}