package com.azas.domain.timecapsule.mapper;

import com.azas.domain.timecapsule.dto.TimeCapsuleSearchCondition;
import com.azas.domain.timecapsule.entity.TimeCapsule;
import com.azas.domain.timecapsule.entity.TimeCapsuleAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TimeCapsuleMapper {

    // [JMG] CAPSULE-1 보관함 생성 대상 계좌의 자녀·상품·만기 정보를 조회한다.
    TimeCapsuleAccount findAccountById(
            @Param("financialAccountId") long financialAccountId
    );

    // [JMG] CAPSULE-1~3 자녀 존재 여부를 확인한다.
    boolean existsChildById(@Param("childId") long childId);

    // [JMG] CAPSULE-1~3 요청 회원의 부모-자녀 관계를 확인한다.
    boolean existsActiveParentRelation(
            @Param("memberId") long memberId,
            @Param("childId") long childId
    );

    // [JMG] CAPSULE-1 적금 계좌에 연결된 기존 보관함을 조회한다.
    TimeCapsule findByFinancialAccountId(
            @Param("financialAccountId") long financialAccountId
    );

    // [JMG] CAPSULE-1 새 보관함을 저장하고 생성된 ID를 채운다.
    int insert(TimeCapsule timeCapsule);

    // [JMG] CAPSULE-1~3 보관함 상세 정보를 조회한다.
    TimeCapsule findById(
            @Param("timeCapsuleId") long timeCapsuleId
    );

    // [JMG] CAPSULE-2 카드형 보관함 목록을 keyset pagination으로 조회한다.
    List<TimeCapsule> findCardSummaries(
            TimeCapsuleSearchCondition condition
    );

    // [JMG] CAPSULE-2 캘린더형 보관함 목록을 keyset pagination으로 조회한다.
    List<TimeCapsule> findCalendarSummaries(
            TimeCapsuleSearchCondition condition
    );
}
