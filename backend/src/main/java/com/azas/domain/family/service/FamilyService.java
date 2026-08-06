package com.azas.domain.family.service;

import com.azas.domain.family.dto.AllowanceRequestResponse;
import com.azas.domain.family.dto.ChildMemberLinkResponse;
import com.azas.domain.family.dto.FamilyGuardianListResponse;
import com.azas.domain.family.dto.FamilyInvitationAcceptRequest;
import com.azas.domain.family.dto.FamilyInvitationAcceptResponse;
import com.azas.domain.family.dto.FamilyInvitationCreateRequest;
import com.azas.domain.family.dto.FamilyInvitationCreateResponse;
import com.azas.domain.family.dto.FamilyInvitationInfoResponse;

public interface FamilyService {

    FamilyGuardianListResponse getFamilyMembers(Long memberId, Long childId);

    ChildMemberLinkResponse getChildMemberLink(Long memberId, Long childId);

    AllowanceRequestResponse requestAllowance(Long memberId, Long childId);

    FamilyInvitationCreateResponse createFamilyInvitation(
            Long memberId,
            Long childId,
            FamilyInvitationCreateRequest request
    );

    FamilyInvitationInfoResponse getFamilyInvitationInfo(
            String inviteToken
    );

    FamilyInvitationAcceptResponse acceptFamilyInvitation(
            Long memberId,
            String inviteToken,
            FamilyInvitationAcceptRequest request
    );
}