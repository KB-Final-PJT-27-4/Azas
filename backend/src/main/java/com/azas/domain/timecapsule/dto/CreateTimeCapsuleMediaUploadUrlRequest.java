package com.azas.domain.timecapsule.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.Locale;

@ApiModel(description = "타임캡슐 대표 이미지 업로드 URL 발급 요청")
@Getter
@NoArgsConstructor
public class CreateTimeCapsuleMediaUploadUrlRequest {

    @ApiModelProperty(
            value = "대표 이미지 MIME 타입",
            required = true,
            example = "image/jpeg"
    )
    @NotBlank
    @JsonProperty("mime_type")
    private String mimeType;

    @ApiModelProperty(
            value = "대표 이미지 파일 크기(byte, 최대 10MiB)",
            required = true,
            example = "1048576"
    )
    @NotNull
    @Positive
    @JsonProperty("file_size")
    private Long fileSize;

    public String normalizedMimeType() {
        return mimeType == null
                ? null
                : mimeType.trim().toLowerCase(Locale.ROOT);
    }
}
