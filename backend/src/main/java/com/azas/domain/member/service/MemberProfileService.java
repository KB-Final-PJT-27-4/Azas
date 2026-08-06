package com.azas.domain.member.service;

import com.azas.domain.auth.entity.SocialAccount;
import com.azas.domain.auth.mapper.SocialAccountMapper;
import com.azas.domain.member.dto.MemberProfileResult;
import com.azas.domain.member.entity.Member;
import com.azas.domain.member.entity.MemberStatus;
import com.azas.domain.member.mapper.MemberMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberProfileService {

    private final MemberMapper memberMapper;
    private final SocialAccountMapper socialAccountMapper;

    @Transactional(readOnly = true)
    public MemberProfileResult getMyProfile(
            long memberId
    ) {
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

        List<SocialAccount> socialAccounts =
                socialAccountMapper.findAllByMemberId(
                        memberId
                );

        return new MemberProfileResult(
                member,
                socialAccounts
        );
    }
}