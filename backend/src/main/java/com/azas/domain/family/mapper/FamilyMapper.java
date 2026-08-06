package com.azas.domain.family.mapper;

import com.azas.domain.auth.entity.FamilyInviteeType;
import com.azas.domain.family.dto.ChildMemberLinkResponse;
import com.azas.domain.family.dto.FamilyGuardianResponse;
import com.azas.domain.family.dto.FamilyInvitationInfoProjection;
import com.azas.domain.family.dto.FamilyInvitationInsertCommand;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface FamilyMapper {

    int countChildAccess(
            @Param("childId") Long childId,
            @Param("memberId") Long memberId
    );

    List<FamilyGuardianResponse> findFamilyMembers(
            @Param("childId") Long childId,
            @Param("memberId") Long memberId
    );

    ChildMemberLinkResponse findChildMemberLinkByChildId(
            @Param("childId") Long childId
    );

    int countChildMemberAccess(
            @Param("childId") Long childId,
            @Param("memberId") Long memberId
    );

    LocalDate findLastAllowanceRequestMonth(
            @Param("childId") Long childId
    );

    int updateAllowanceRequest(
            @Param("childId") Long childId,
            @Param("requestMonth") LocalDate requestMonth
    );

    BigDecimal findChildAvailableAmount(
            @Param("childId") Long childId
    );

    Long lockActiveChild(
            @Param("childId") Long childId
    );

    int expirePendingFamilyInvitations(
            @Param("childId") Long childId,
            @Param("inviteeType") FamilyInviteeType inviteeType,
            @Param("now") LocalDateTime now
    );

    int countUsableFamilyInvitations(
            @Param("childId") Long childId,
            @Param("inviteeType") FamilyInviteeType inviteeType,
            @Param("now") LocalDateTime now
    );

    int insertFamilyInvitation(
            FamilyInvitationInsertCommand command
    );

    FamilyInvitationInfoProjection findFamilyInvitationInfoByTokenHash(
            @Param("inviteTokenHash") String inviteTokenHash
    );
}