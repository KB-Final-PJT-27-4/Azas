package com.azas.domain.allowance.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class CreateAllowanceRequest {

    @NotNull
    @DecimalMin(value = "1", inclusive = true)
    @Digits(integer = 18, fraction = 0)
    @JsonProperty("requested_amount")
    private BigDecimal requestedAmount;

    @NotBlank
    @Size(max = 200)
    private String message;
}