package com.azas.domain.timecapsule.mapper;

import com.azas.domain.timecapsule.dto.TimeCapsuleSearchCondition;
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

    // [JMG] CAPSULE-1~3 요청 회원의 부모-자녀 관계를 확인한다.
    boolean existsActiveParentRelation(
            @Param("memberId") long memberId,
            @Param("childId") long childId
    );

    TimeCapsule findByChildIdAndFinancialAccountId(
            @Param("childId") long childId,
            @Param("financialAccountId") long financialAccountId
    );

    // [JMG] CAPSULE-1 적금 계좌에 연결된 기존 보관함을 조회한다.
    TimeCapsule findByFinancialAccountId(
            @Param("financialAccountId") long financialAccountId
    );

    // [JMG] CAPSULE-1 새 보관함을 저장하고 생성된 ID를 채운다.
    int insert(TimeCapsule timeCapsule);

    // [JMG] CAPSULE-3 요청 부모가 접근 가능한 보관함 상세 정보를 조회한다.
    TimeCapsule findAccessibleById(
            @Param("timeCapsuleId") long timeCapsuleId,
            @Param("memberId") long memberId
    );

    // [JMG] CAPSULE-5 기록 생성 중 공개 상태 변경과 집계값 경합을 막기 위해 보관함 행을 잠근다.
    TimeCapsule findAccessibleByIdForUpdate(
            @Param("timeCapsuleId") long timeCapsuleId,
            @Param("memberId") long memberId
    );

    // [JMG] CAPSULE-5 이체 성공 이벤트 처리 중 적금 계좌의 보관함을 잠가 중복 엔트리 생성을 막는다.
    TimeCapsule findByFinancialAccountIdForUpdate(
            @Param("financialAccountId") long financialAccountId
    );

    // [JMG] CAPSULE-13 삭제된 엔트리를 보관함 집계에서 제외하고 최신 기록 시각을 다시 계산한다.
    int decreaseEntryCountAndRefreshLatestEntry(
            @Param("timeCapsuleId") long timeCapsuleId
    );

    // [JMG] CAPSULE-6 하위 데이터를 정리한 뒤 타임캡슐 보관함 행을 영구 삭제한다.
    int deleteById(@Param("timeCapsuleId") long timeCapsuleId);

    // [JMG] CAPSULE-2 카드형 보관함 목록을 keyset pagination으로 조회한다.
    List<TimeCapsule> findCardSummaries(
            TimeCapsuleSearchCondition condition
    );

    // [JMG] CAPSULE-2 캘린더형 보관함 목록을 keyset pagination으로 조회한다.
    List<TimeCapsule> findCalendarSummaries(
            TimeCapsuleSearchCondition condition
    );
}
