package com.azas.domain.finance.autotransfer.dto;

import com.azas.domain.finance.autotransfer.entity.AutoTransferFrequency;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class CreateAutoTransferScheduleRequest {

    @NotNull
    @Positive
    @JsonProperty("child_id")
    private Long childId;

    @NotNull
    @Positive
    @JsonProperty("source_account_id")
    private Long sourceAccountId;

    @NotNull
    @Positive
    @JsonProperty("destination_account_id")
    private Long destinationAccountId;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotNull
    private AutoTransferFrequency frequency;

    @NotNull
    @JsonProperty("transfer_day")
    private Integer transferDay;

    @NotNull
    @JsonProperty("start_date")
    private LocalDate startDate;

    @JsonProperty("end_date")
    private LocalDate endDate;
}