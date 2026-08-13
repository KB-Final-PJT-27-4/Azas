package com.azas.domain.member.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface MemberWithdrawalMapper {

    int countChildrenWithNoOtherActiveGuardian(
            @Param("memberId") long memberId
    );

    int cancelPendingInvitationsByInviter(
            @Param("memberId") long memberId,
            @Param("withdrawnAt") LocalDateTime withdrawnAt
    );

    int revokeActiveFinancialConnections(
            @Param("memberId") long memberId,
            @Param("withdrawnAt") LocalDateTime withdrawnAt
    );

    int unlinkChildMember(
            @Param("memberId") long memberId
    );

    int deleteChildParentRelations(
            @Param("memberId") long memberId
    );

    int deletePhoneVerifications(
            @Param("memberId") long memberId
    );

    int deleteSocialAccounts(
            @Param("memberId") long memberId
    );
}