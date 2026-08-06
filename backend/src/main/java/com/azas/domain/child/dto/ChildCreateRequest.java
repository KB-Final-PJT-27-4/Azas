package com.azas.domain.child.dto;

// 자녀 프로필 설정 요청 DTO

import com.azas.domain.child.entity.BirthStatus;
import com.azas.domain.child.entity.Gender;
import com.azas.domain.child.entity.RelationType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class ChildCreateRequest {

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

    @JsonProperty("relation_type")
    private RelationType relationType;



}
