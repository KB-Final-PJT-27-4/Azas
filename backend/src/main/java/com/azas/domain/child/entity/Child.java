package com.azas.domain.child.entity;

import com.azas.domain.child.dto.ChildCreateRequest;
import com.azas.domain.child.dto.ChildUpdateRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class Child {

    private Long childId;
    private Long memberId;
    private String name;
    private BirthStatus birthStatus;
    private LocalDate expectedBirthDate;
    private LocalDate birthDate;
    private Gender gender;
    private String profileImageUrl;
    private ChildStatus childStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    public static Child from(ChildCreateRequest request){
        Child child = new Child();
        child.setName(request.getName());
        child.setBirthStatus(request.getBirthStatus());
        child.setGender(request.getGender());
        child.setProfileImageUrl(request.getProfileImageUrl());
        child.setChildStatus(ChildStatus.ACTIVE);
        child.setExpectedBirthDate(request.getExpectedBirthDate());
        child.setBirthDate(request.getBirthDate());
        return child;
    }

    public void update(ChildUpdateRequest request){
        this.name = request.getName();
        this.birthStatus = request.getBirthStatus();
        this.expectedBirthDate = request.getExpectedBirthDate();
        this.birthDate = request.getBirthDate();
        this.gender = request.getGender();
        this.profileImageUrl = request.getProfileImageUrl();
    }
}
