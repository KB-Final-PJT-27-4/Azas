package com.azas.domain.timecapsule.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Locale;

@Getter
@NoArgsConstructor
public class CreateTimeCapsuleMediaUploadUrlsRequest {

    @NotEmpty
    @Size(max = 3)
    @Valid
    private List<FileRequest> files;

    @Getter
    @NoArgsConstructor
    public static class FileRequest {

        @JsonProperty("mime_type")
        private String mimeType;

        @NotNull
        @Positive
        @JsonProperty("file_size")
        private Long fileSize;

        @NotNull
        @Positive
        @JsonProperty("slot_no")
        private Integer slotNo;

        // [JMG] CAPSULE-7 파일 요청의 필수 문자열·숫자 값이 유효한지 서비스 검증 전에 확인한다.
        public boolean hasRequiredValue() {
            return mimeType != null
                    && !mimeType.isBlank()
                    && fileSize != null
                    && slotNo != null;
        }

        // [JMG] CAPSULE-7 MIME 타입을 대소문자·공백 차이 없이 정책 검증에 사용할 형태로 반환한다.
        public String normalizedMimeType() {
            return mimeType == null
                    ? null
                    : mimeType.trim().toLowerCase(Locale.ROOT);
        }
    }
}
