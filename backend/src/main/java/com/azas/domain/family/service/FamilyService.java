package com.azas.domain.family.service;

import com.azas.domain.family.dto.AllowanceRequestResponse;
import com.azas.domain.family.dto.ChildMemberLinkResponse;
import com.azas.domain.family.dto.FamilyGuardianListResponse;

public interface FamilyService {

    FamilyGuardianListResponse getFamilyMembers(Long memberId, Long childId);
    ChildMemberLinkResponse getChildMemberLink(Long memberId, Long childId);
    AllowanceRequestResponse requestAllowance(Long memberId, Long childId);
}