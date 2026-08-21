package com.azas.domain.checklist.dto;

import com.azas.domain.child.entity.BirthStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class ChecklistChildLifecycleRow {

    private Long childId;
    private BirthStatus birthStatus;
    private LocalDate expectedBirthDate;
    private LocalDate birthDate;
}