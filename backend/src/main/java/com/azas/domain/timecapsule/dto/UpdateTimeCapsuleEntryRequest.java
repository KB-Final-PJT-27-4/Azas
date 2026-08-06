package com.azas.domain.timecapsule.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;

@Getter
@NoArgsConstructor
public class UpdateTimeCapsuleEntryRequest {

    @Size(max = 200)
    private String title;

    @Size(max = 5000)
    private String message;

    // [JMG] CAPSULE-12 제목 또는 편지 중 하나라도 수정값으로 전달됐는지 확인한다.
    public boolean hasUpdateField() {
        return title != null || message != null;
    }

    // [JMG] CAPSULE-12 전달된 제목과 편지가 빈 문자열만으로 구성되지 않았는지 검증한다.
    public boolean hasOnlyValidText() {
        return (title == null || !title.isBlank())
                && (message == null || !message.isBlank());
    }

    // [JMG] CAPSULE-12 제목 수정값을 공백 제거 후 반환하고 미전달 값은 null로 유지한다.
    public String getTrimmedTitle() {
        return title == null ? null : title.trim();
    }

    // [JMG] CAPSULE-12 편지 수정값을 공백 제거 후 반환하고 미전달 값은 null로 유지한다.
    public String getTrimmedMessage() {
        return message == null ? null : message.trim();
    }
}
