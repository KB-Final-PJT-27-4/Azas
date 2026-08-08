package com.azas.domain.auth.mapper;

import com.azas.domain.child.entity.Child;
import com.azas.domain.child.entity.RelationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ParentInviteMapper {

    Child findActiveChildById(
            @Param("childId") Long childId
    );

    int countChildParentRelation(
            @Param("childId") Long childId,
            @Param("memberId") Long memberId
    );

    int insertChildParentRelation(
            @Param("childId") Long childId,
            @Param("memberId") Long memberId,
            @Param("relationType") RelationType relationType
    );
}