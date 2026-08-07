package com.azas.domain.member.service;

import com.azas.domain.auth.service.RefreshTokenStore;
import com.azas.domain.member.entity.Member;
import com.azas.domain.member.entity.MemberStatus;
import com.azas.domain.member.mapper.MemberMapper;
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

    @Transactional
    public void withdrawMyMembership(long memberId) {
        Member member = memberMapper.findById(memberId);

        if (member == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_ACCESS_TOKEN
            );
        }

        if (member.getStatus() == MemberStatus.WITHDRAWN) {
            throw withdrawnMember();
        }

        int withdrawnCount =
                memberMapper.withdrawIfActive(memberId);

        // 조회 이후 다른 요청이 먼저 탈퇴했는지 조건부 UPDATE 결과로 다시 확인한다.
        if (withdrawnCount != 1) {
            throw withdrawnMember();
        }

        LocalDateTime revokedAt =
                LocalDateTime.now(ZoneOffset.UTC);

        refreshTokenStore.revokeAllActiveByMemberId(
                memberId,
                revokedAt
        );
    }

    private BusinessException withdrawnMember() {
        return new BusinessException(
                ErrorCode.WITHDRAWN_MEMBER
        );
    }
}