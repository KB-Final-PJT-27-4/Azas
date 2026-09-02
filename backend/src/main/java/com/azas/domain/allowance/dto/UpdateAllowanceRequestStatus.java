package com.azas.domain.allowance.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAllowanceRequestStatus {

    private String action;

    @JsonIgnore
    private Long sourceAccountId;

    @JsonIgnore
    private Long destinationAccountId;
}
