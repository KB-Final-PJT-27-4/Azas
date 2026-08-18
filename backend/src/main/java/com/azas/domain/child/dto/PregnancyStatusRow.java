package com.azas.domain.child.dto;

import com.azas.domain.child.entity.BirthStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PregnancyStatusRow {

    private Long childId;

    private String childName;

    private BirthStatus birthStatus;

    private LocalDate expectedBirthDate;
}