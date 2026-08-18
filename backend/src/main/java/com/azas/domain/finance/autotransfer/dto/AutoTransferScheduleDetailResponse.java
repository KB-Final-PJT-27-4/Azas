package com.azas.domain.finance.autotransfer.dto;

import com.azas.domain.finance.autotransfer.entity.AutoTransferFrequency;
import com.azas.domain.finance.autotransfer.entity.AutoTransferScheduleStatus;
import com.azas.domain.finance.transfer.entity.TransferStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class AutoTransferScheduleDetailResponse {

    @JsonProperty("auto_transfer_schedule_id")
    private final Long autoTransferScheduleId;

    @JsonProperty("child_id")
    private final Long childId;

    @JsonProperty("financial_goal_id")
    private final Long financialGoalId;

    @JsonProperty("goal_title")
    private final String goalTitle;

    @JsonProperty("source_account_id")
    private final Long sourceAccountId;

    @JsonProperty("source_account_name")
    private final String sourceAccountName;

    @JsonProperty("destination_account_id")
    private final Long destinationAccountId;

    @JsonProperty("destination_account_name")
    private final String destinationAccountName;

    private final BigDecimal amount;
    private final AutoTransferFrequency frequency;

    @JsonProperty("transfer_day")
    private final Integer transferDay;

    @JsonProperty("start_date")
    private final LocalDate startDate;

    @JsonProperty("end_date")
    private final LocalDate endDate;

    @JsonProperty("next_transfer_at")
    private final Instant nextTransferAt;

    @JsonProperty("last_transfer_id")
    private final Long lastTransferId;

    @JsonProperty("last_transfer_status")
    private final TransferStatus lastTransferStatus;

    @JsonProperty("last_failure_code")
    private final String lastFailureCode;

    @JsonProperty("last_failure_message")
    private final String lastFailureMessage;

    @JsonProperty("last_transferred_at")
    private final Instant lastTransferredAt;

    private final AutoTransferScheduleStatus status;

    @JsonProperty("created_at")
    private final Instant createdAt;

    @JsonProperty("updated_at")
    private final Instant updatedAt;
}