package com.azas.domain.family.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@ApiModel(value = "FamilyAllowanceRequestResponse")
public class AllowanceRequestResponse {

    @JsonProperty("child_id")
    private Long childId;

    private Boolean requested;

    @JsonProperty("request_month")
    private LocalDate requestMonth;

    @JsonProperty("requested_at")
    private LocalDateTime requestedAt;

    @JsonProperty("child_available_amount")
    private BigDecimal childAvailableAmount;

    private String message;
}
