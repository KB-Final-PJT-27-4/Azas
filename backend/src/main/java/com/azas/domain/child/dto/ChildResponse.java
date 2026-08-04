package com.azas.domain.child.dto;

import com.azas.domain.child.entity.BirthStatus;
import com.azas.domain.child.entity.Gender;
import com.azas.domain.child.entity.RelationType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

// 자녀 등록 응답, 자녀 상세 조회 응답, 자녀 수정 응답 DTO
@Getter
@NoArgsConstructor
public class ChildResponse {

    @JsonProperty("child_id")
    private Long childId;

    private String name;

    @JsonProperty("birth_status")
    private BirthStatus birthStatus;

    @JsonProperty("expected_birth_date")
    private LocalDate expectedBirthDate;

    @JsonProperty("birth_date")
    private LocalDate birthDate;

    private Gender gender;

    @JsonProperty("profile_image_url")
    private String profileImageUrl;

    private Integer age;

    @JsonProperty("relation_type")
    private RelationType relationType;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;


}
