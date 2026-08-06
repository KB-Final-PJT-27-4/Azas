package com.azas.domain.timecapsule.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@ApiModel(description = "타임캡슐 보관함 생성 요청")
@Getter
@NoArgsConstructor
public class CreateTimeCapsuleRequest {

    @ApiModelProperty(
            value = "보관함 제목",
            required = true,
            example = "깨비의 첫 대학자금 저축"
    )
    @NotBlank
    @Size(max = 200)
    @JsonProperty("title")
    private String title;
}
