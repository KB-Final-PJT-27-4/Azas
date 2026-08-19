package com.azas.domain.member.service;

import com.azas.domain.auth.service.RefreshTokenStore;
import com.azas.domain.member.entity.Member;
import com.azas.domain.member.entity.MemberStatus;
import com.azas.domain.member.entity.MemberType;
import com.azas.domain.member.mapper.MemberMapper;
import com.azas.domain.member.mapper.MemberWithdrawalMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class MemberWithdrawalService {

    private final MemberMapper memberMapper;
    private final RefreshTokenStore refreshTokenStore;
    private final MemberWithdrawalMapper memberWithdrawalMapper;


    @Transactional
    public void withdrawMyMembership(long memberId) {
        Member member = memberMapper.findById(memberId);

        if (member == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_ACCESS_TOKEN
            );
        }

        if (member.getStatus() == MemberStatus.WITHDRAWN) {
            throw new BusinessException(
                    ErrorCode.WITHDRAWN_MEMBER
            );
        }

        if (member.getMemberType() == MemberType.PARENT
                && memberWithdrawalMapper
                .countChildrenWithNoOtherActiveGuardian(memberId) > 0) {
            throw new BusinessException(
                    ErrorCode.LAST_GUARDIAN_WITHDRAWAL_NOT_ALLOWED
            );
        }

        LocalDateTime withdrawnAt =
                LocalDateTime.now(ZoneOffset.UTC);

        refreshTokenStore.revokeAllActiveByMemberId(
                memberId,
                withdrawnAt
        );

        memberWithdrawalMapper.cancelPendingInvitationsByInviter(
                memberId,
                withdrawnAt
        );

        memberWithdrawalMapper.unlinkChildMember(memberId);
        memberWithdrawalMapper.deleteChildParentRelations(memberId);
        memberWithdrawalMapper.deletePhoneVerifications(memberId);
        memberWithdrawalMapper.deleteSocialAccounts(memberId);

        int updatedCount =
                memberMapper.anonymizeAndWithdrawIfActive(
                        memberId,
                        withdrawnAt
                );

        if (updatedCount != 1) {
            throw new BusinessException(
                    ErrorCode.WITHDRAWN_MEMBER
            );
        }
    }
}
