package com.azas.domain.finance.autotransfer.dto;

import com.azas.domain.finance.autotransfer.entity.AutoTransferFrequency;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@ApiModel(description = "자동이체 일정 등록 요청")
public class CreateAutoTransferScheduleRequest {

    @ApiModelProperty(value = "받는 계좌 소유 자녀 ID", example = "6")
    @Positive
    @JsonProperty("child_id")
    private Long childId;

    @ApiModelProperty(value = "로그인 부모의 활성 입출금 계좌 ID", required = true, example = "101")
    @NotNull
    @Positive
    @JsonProperty("source_account_id")
    private Long sourceAccountId;

    @ApiModelProperty(value = "부모 또는 현재 자녀의 활성 입출금·적금 계좌 ID", required = true, example = "206")
    @NotNull
    @Positive
    @JsonProperty("destination_account_id")
    private Long destinationAccountId;

    @ApiModelProperty(value = "회차별 이체 금액", required = true, example = "100000")
    @NotNull
    @Positive
    private BigDecimal amount;

    @ApiModelProperty(value = "이체 주기", required = true, allowableValues = "MONTHLY", example = "MONTHLY")
    @NotNull
    private AutoTransferFrequency frequency;

    @ApiModelProperty(value = "매월 이체일(1~28)", required = true, example = "28")
    @NotNull
    @JsonProperty("transfer_day")
    private Integer transferDay;

    @ApiModelProperty(value = "최초 이체 가능일", required = true, example = "2026-08-28")
    @NotNull
    @JsonProperty("start_date")
    private LocalDate startDate;

    @ApiModelProperty(value = "종료일(생략 가능)", example = "2027-08-28")
    @JsonProperty("end_date")
    private LocalDate endDate;
}
