package com.azas.domain.auth.mapper;

import com.azas.domain.child.entity.Child;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ChildInviteMapper {

    Child findActiveById(
            @Param("childId") Long childId
    );

    Child findByMemberId(
            @Param("memberId") Long memberId
    );

    int linkMemberIfUnlinked(
            @Param("childId") Long childId,
            @Param("memberId") Long memberId
    );
}