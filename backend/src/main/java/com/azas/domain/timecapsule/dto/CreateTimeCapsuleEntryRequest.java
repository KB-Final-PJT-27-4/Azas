package com.azas.domain.timecapsule.dto;

import com.azas.domain.timecapsule.entity.TimeCapsuleEntryMediaMode;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

@ApiModel(description = "타임캡슐 기록 생성 요청")
@Getter
@NoArgsConstructor
public class CreateTimeCapsuleEntryRequest {

    @ApiModelProperty(value = "연결할 적금 입금 거래 ID", required = true, example = "901")
    @NotNull
    @Positive
    @JsonProperty("account_transaction_id")
    private Long accountTransactionId;

    @ApiModelProperty(value = "기록 제목", required = true, example = "첫 생일 축하")
    @NotBlank
    @Size(max = 200)
    private String title;

    @ApiModelProperty(value = "부모 메시지", required = true, example = "오늘도 너를 위해 한 걸음 더 저축했어.")
    @NotBlank
    @Size(max = 5000)
    private String message;

    @ApiModelProperty(
            value = "첨부할 미디어 유형",
            required = true,
            allowableValues = "NONE,IMAGE,VIDEO",
            example = "IMAGE"
    )
    @NotBlank
    @JsonProperty("media_mode")
    private String mediaMode;

    // [JMG] CAPSULE-5 요청 미디어 문자열을 검증 가능한 기록 미디어 유형으로 변환한다.
    public TimeCapsuleEntryMediaMode toMediaMode() {
        return TimeCapsuleEntryMediaMode.from(mediaMode);
    }
}
