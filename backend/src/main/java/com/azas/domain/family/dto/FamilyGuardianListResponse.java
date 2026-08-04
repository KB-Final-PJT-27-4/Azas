package com.azas.domain.family.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FamilyGuardianListResponse {
    private List<FamilyGuardianResponse> items;
}
