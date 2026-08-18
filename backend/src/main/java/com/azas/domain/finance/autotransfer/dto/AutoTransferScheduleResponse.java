package com.azas.domain.finance.autotransfer.dto;

import com.azas.domain.finance.autotransfer.entity.AutoTransferFrequency;
import com.azas.domain.finance.autotransfer.entity.AutoTransferScheduleStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class AutoTransferScheduleResponse {

    @JsonProperty("auto_transfer_schedule_id")
    private final Long autoTransferScheduleId;

    @JsonProperty("financial_goal_id")
    private final Long financialGoalId;

    @JsonProperty("source_account_id")
    private final Long sourceAccountId;

    @JsonProperty("destination_account_id")
    private final Long destinationAccountId;

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

    @JsonProperty("last_transfer_status")
    private final String lastTransferStatus;

    @JsonProperty("last_transferred_at")
    private final Instant lastTransferredAt;

    private final AutoTransferScheduleStatus status;

    @JsonProperty("created_at")
    private final Instant createdAt;
}