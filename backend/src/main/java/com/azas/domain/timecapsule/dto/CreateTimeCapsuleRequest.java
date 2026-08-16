package com.azas.domain.timecapsule.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import java.time.LocalDate;

@ApiModel(description = "타임캡슐 보관함 생성 요청")
@Getter
@NoArgsConstructor
public class CreateTimeCapsuleRequest {

    @ApiModelProperty(
            value = "타임캡슐과 연결할 금융 계좌 ID",
            required = true,
            example = "5"
    )
    @NotNull
    @Positive
    @JsonProperty("financial_account_id")
    private Long financialAccountId;

    @ApiModelProperty(
            value = "공개 예정일. 생략하면 나중에 설정할 수 있습니다.",
            example = "2027-08-08"
    )
    @JsonProperty("release_date")
    private LocalDate releaseDate;
}
