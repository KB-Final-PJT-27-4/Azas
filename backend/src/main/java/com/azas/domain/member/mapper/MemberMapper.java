package com.azas.domain.member.mapper;

import com.azas.domain.member.entity.Member;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MemberMapper {

    // XML 파라미터 이름을 컴파일 옵션과 무관하게 고정한다.
    Member findById(@Param("memberId") long memberId);

    Member findByEmail(@Param("email") String email);

    int insert(Member member);

    int updateProfile(Member member);

    int withdrawIfActive(
            @Param("memberId") long memberId
    );
}