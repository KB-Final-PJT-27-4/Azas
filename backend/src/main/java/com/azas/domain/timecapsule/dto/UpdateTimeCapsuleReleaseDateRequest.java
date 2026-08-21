package com.azas.domain.timecapsule.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@ApiModel(description = "타임캡슐 공개일 변경 요청")
@Getter
@NoArgsConstructor
public class UpdateTimeCapsuleReleaseDateRequest {

    @ApiModelProperty(
            value = "변경할 공개일. 오늘 이후 날짜만 설정할 수 있습니다.",
            required = true,
            example = "2027-08-08"
    )
    @NotNull
    @JsonProperty("release_date")
    private LocalDate releaseDate;
}
