package com.azas.domain.finance.transfer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class CreateTransferRequest {

    @NotNull
    @Positive
    @JsonProperty("source_account_id")
    private Long sourceAccountId;

    @NotNull
    @Positive
    @JsonProperty("destination_account_id")
    private Long destinationAccountId;

    @NotNull
    @DecimalMin(value = "1", inclusive = true)
    private BigDecimal amount;

    @Size(max = 255)
    private String memo;
}