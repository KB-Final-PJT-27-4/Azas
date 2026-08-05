package com.azas.domain.family.service;

import com.azas.domain.family.dto.FamilyGuardianListResponse;

public interface FamilyService {

    FamilyGuardianListResponse getFamilyMembers(Long memberId, Long childId);
}