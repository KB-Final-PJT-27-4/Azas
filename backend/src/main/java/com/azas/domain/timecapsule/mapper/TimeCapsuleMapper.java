package com.azas.domain.timecapsule.mapper;

import com.azas.domain.timecapsule.entity.TimeCapsule;
import com.azas.domain.timecapsule.entity.TimeCapsuleAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TimeCapsuleMapper {

    TimeCapsuleAccount findAccountById(
            @Param("financialAccountId") long financialAccountId
    );

    boolean existsChildById(@Param("childId") long childId);

    boolean existsActiveParentRelation(
            @Param("memberId") long memberId,
            @Param("childId") long childId
    );

    TimeCapsule findByChildIdAndFinancialAccountId(
            @Param("childId") long childId,
            @Param("financialAccountId") long financialAccountId
    );

    TimeCapsule findByFinancialAccountId(
            @Param("financialAccountId") long financialAccountId
    );

    int insert(TimeCapsule timeCapsule);

    TimeCapsule findAccessibleById(
            @Param("timeCapsuleId") long timeCapsuleId,
            @Param("memberId") long memberId
    );

    TimeCapsule findAccessibleByIdForUpdate(
            @Param("timeCapsuleId") long timeCapsuleId,
            @Param("memberId") long memberId
    );

    int deleteById(@Param("timeCapsuleId") long timeCapsuleId);

    List<TimeCapsule> findSummariesByChildId(
            @Param("childId") long childId
    );
}
