package com.azas.domain.auth.service;

import com.azas.domain.auth.dto.OAuthMemberResult;
import com.azas.domain.auth.dto.OAuthProfile;
import com.azas.domain.auth.entity.SocialAccount;
import com.azas.domain.auth.mapper.SocialAccountMapper;
import com.azas.domain.member.entity.Member;
import com.azas.domain.member.entity.MemberStatus;
import com.azas.domain.member.entity.MemberType;
import com.azas.domain.member.mapper.MemberMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OAuthMemberService {

    private final MemberMapper memberMapper;
    private final SocialAccountMapper socialAccountMapper;

    @Transactional
    public OAuthMemberResult findOrCreate(
            OAuthProfile profile
    ) {
        SocialAccount socialAccount =
                socialAccountMapper.findByProviderAndSubject(
                        profile.getProvider(),
                        profile.getProviderSubject()
                );

        if (socialAccount != null) {
            Member member = findActiveMember(
                    socialAccount.getMemberId()
            );

            return new OAuthMemberResult(
                    member,
                    false
            );
        }

        Member member =
                memberMapper.findByEmail(profile.getEmail());

        if (member != null) {
            validateActive(member);

            // 동일 이메일의 다른 소셜 계정은 기존 회원에 연결해 중복 생성을 막는다.
            linkSocialAccount(member.getMemberId(), profile);

            return new OAuthMemberResult(
                    member,
                    false
            );
        }

        Member newMember = Member.createParent(
                profile.getEmail(),
                profile.getName(),
                profile.getProfileImageUrl()
        );

        memberMapper.insert(newMember);

        Member savedMember =
                findActiveMember(newMember.getMemberId());

        linkSocialAccount(
                savedMember.getMemberId(),
                profile
        );

        return new OAuthMemberResult(
                savedMember,
                true
        );
    }

    @Transactional
    public OAuthMemberResult findOrCreateChild(
            OAuthProfile profile
    ) {
        SocialAccount socialAccount =
                socialAccountMapper.findByProviderAndSubject(
                        profile.getProvider(),
                        profile.getProviderSubject()
                );

        if (socialAccount != null) {
            Member member = findActiveMember(
                    socialAccount.getMemberId()
            );

            validateChildMember(member);

            return new OAuthMemberResult(
                    member,
                    false
            );
        }

        Member member =
                memberMapper.findByEmail(profile.getEmail());

        if (member != null) {
            validateActive(member);
            validateChildMember(member);

            linkSocialAccount(
                    member.getMemberId(),
                    profile
            );

            return new OAuthMemberResult(
                    member,
                    false
            );
        }

        Member newMember = Member.createChild(
                profile.getEmail(),
                profile.getName(),
                profile.getProfileImageUrl()
        );

        memberMapper.insert(newMember);

        Member savedMember =
                findActiveMember(newMember.getMemberId());

        linkSocialAccount(
                savedMember.getMemberId(),
                profile
        );

        return new OAuthMemberResult(
                savedMember,
                true
        );
    }

    @Transactional
    public OAuthMemberResult findOrCreateParent(
            OAuthProfile profile
    ) {
        SocialAccount socialAccount =
                socialAccountMapper.findByProviderAndSubject(
                        profile.getProvider(),
                        profile.getProviderSubject()
                );

        if (socialAccount != null) {
            Member member = findActiveMember(
                    socialAccount.getMemberId()
            );

            validateParentMember(member);

            return new OAuthMemberResult(
                    member,
                    false
            );
        }

        Member member =
                memberMapper.findByEmail(profile.getEmail());

        if (member != null) {
            validateActive(member);
            validateParentMember(member);

            linkSocialAccount(
                    member.getMemberId(),
                    profile
            );

            return new OAuthMemberResult(
                    member,
                    false
            );
        }

        Member newMember = Member.createParent(
                profile.getEmail(),
                profile.getName(),
                profile.getProfileImageUrl()
        );

        memberMapper.insert(newMember);

        Member savedMember =
                findActiveMember(newMember.getMemberId());

        linkSocialAccount(
                savedMember.getMemberId(),
                profile
        );

        return new OAuthMemberResult(
                savedMember,
                true
        );
    }

    private Member findActiveMember(long memberId) {
        Member member = memberMapper.findById(memberId);

        if (member == null) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR
            );
        }

        validateActive(member);
        return member;
    }

    private void validateActive(Member member) {
        if (member.getStatus() == MemberStatus.WITHDRAWN) {
            throw new BusinessException(
                    ErrorCode.WITHDRAWN_MEMBER
            );
        }
    }

    private void validateChildMember(Member member) {
        if (member.getMemberType() != MemberType.CHILD) {
            throw new BusinessException(
                    ErrorCode.MEMBER_TYPE_CONFLICT
            );
        }
    }

    private void validateParentMember(Member member) {
        if (member.getMemberType() != MemberType.PARENT) {
            throw new BusinessException(
                    ErrorCode.MEMBER_TYPE_CONFLICT
            );
        }
    }

    private void linkSocialAccount(
            long memberId,
            OAuthProfile profile
    ) {
        SocialAccount socialAccount =
                SocialAccount.create(
                        memberId,
                        profile.getProvider(),
                        profile.getProviderSubject()
                );

        socialAccountMapper.insert(socialAccount);
    }
}