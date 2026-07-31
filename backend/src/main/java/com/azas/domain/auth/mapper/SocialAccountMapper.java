package com.azas.domain.member.mapper;

import com.azas.domain.auth.entity.OAuthProvider;
import com.azas.domain.auth.entity.SocialAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SocialAccountMapper {

    SocialAccount findByProviderAndSubject(
            @Param("provider") OAuthProvider provider,
            @Param("providerSubject") String providerSubject
    );

    int insert(SocialAccount socialAccount);
}
