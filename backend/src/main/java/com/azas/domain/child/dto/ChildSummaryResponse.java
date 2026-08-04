package com.azas.domain.child.dto;

import com.azas.domain.child.entity.BirthStatus;
import com.azas.domain.child.entity.RelationType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

// 자녀 프로필 요약 응답 DTO
@Getter
@NoArgsConstructor
public class ChildSummaryResponse {

    @JsonProperty("child_id")
    private Long childId;

    private String name;

    @JsonProperty("birth_status")
    private BirthStatus birthStatus;

    @JsonProperty("birth_date")
    private LocalDate birthDate;

    @JsonProperty("expected_birth_date")
    private LocalDate expectedBirthDate;

    private Integer age;

    @JsonProperty("profile_image_url")
    private String profileImageUrl;

    @JsonProperty("relateion_type")
    private RelationType relationType;

}
