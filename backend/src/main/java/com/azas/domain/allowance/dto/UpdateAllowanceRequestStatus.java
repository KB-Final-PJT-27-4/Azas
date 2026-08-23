package com.azas.domain.allowance.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAllowanceRequestStatus {

    private String action;

    @JsonProperty("source_account_id")
    private Long sourceAccountId;

    @JsonProperty("destination_account_id")
    private Long destinationAccountId;
}
