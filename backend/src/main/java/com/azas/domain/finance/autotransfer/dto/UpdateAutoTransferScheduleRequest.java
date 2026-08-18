package com.azas.domain.finance.autotransfer.dto;

import com.azas.domain.finance.autotransfer.entity.AutoTransferAction;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class UpdateAutoTransferScheduleRequest {

    @NotNull
    private AutoTransferAction action;

    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    @Min(1)
    @Max(28)
    @JsonProperty("transfer_day")
    private Integer transferDay;

    @JsonProperty("end_date")
    private LocalDate endDate;

    /*
     * end_date 미전달과 end_date:null을 구분한다.
     */
    @JsonIgnore
    private boolean endDatePresent;

    @JsonSetter("end_date")
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
        this.endDatePresent = true;
    }
}