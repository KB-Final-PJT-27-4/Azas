package com.azas.domain.family.mapper;

import com.azas.domain.family.dto.FamilyGuardianResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FamilyMapper {

    int countChildAccess(
            @Param("childId") Long childId,
            @Param("memberId") Long memberId
    );

    List<FamilyGuardianResponse> findFamilyMembers(
            @Param("childId") Long childId,
            @Param("memberId") Long memberId
    );
}