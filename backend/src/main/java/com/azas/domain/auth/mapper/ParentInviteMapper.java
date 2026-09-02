package com.azas.domain.auth.mapper;

import com.azas.domain.child.entity.Child;
import com.azas.domain.child.entity.RelationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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

    int insertChildParentRelations(
            @Param("childIds") List<Long> childIds,
            @Param("memberId") Long memberId,
            @Param("relationType") RelationType relationType
    );
}
