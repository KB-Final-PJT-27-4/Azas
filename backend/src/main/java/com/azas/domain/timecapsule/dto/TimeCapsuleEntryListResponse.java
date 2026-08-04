package com.azas.domain.timecapsule.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.Getter;

import java.util.List;

@ApiModel(description = "타임캡슐 기록 목록 응답")
@Getter
public class TimeCapsuleEntryListResponse {

    @JsonProperty("entries")
    private final List<TimeCapsuleEntrySummaryResponse> entries;

    // [JMG] CAPSULE-4 조회된 기록 목록을 API 응답 형태로 구성한다.
    public TimeCapsuleEntryListResponse(
            List<TimeCapsuleEntrySummaryResponse> entries
    ) {
        this.entries = List.copyOf(entries);
    }
}
