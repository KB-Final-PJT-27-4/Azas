package com.azas.domain.child.mapper;

import com.azas.domain.child.dto.ChildResponse;
import com.azas.domain.child.dto.ChildSummaryResponse;
import com.azas.domain.child.entity.Child;
import com.azas.domain.child.entity.ChildFeaturePermission;
import com.azas.domain.child.entity.RelationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.azas.domain.child.dto.PregnancyStatusRow;

import java.util.List;

@Mapper
public interface ChildMapper {

    // 자녀 프로필을 child 테이블에 등록
    void insertChild(Child child);

    // 자녀 등록 후 현재 로그인한 부모 - 자녀 관계를 child_parent 테이블에 등록
    void insertChildParent(
            @Param("childId") Long childId,
            @Param("memberId") Long memberId,
            @Param("relationType") RelationType relationType
    );

    int insertExistingGuardiansForNewChild(
            @Param("childId") Long childId,
            @Param("memberId") Long memberId
    );

    // 특정 부모가 접근 가능한 활성 자녀 목록 조회
    List<ChildSummaryResponse> findChildrenByMemberId(@Param("memberId") Long memberId);

    // 특정 부모가 접근 가능한 자녀의 상세 프로필 조회 - 이 부모가 이 자녀와 연결되어 있는지?
    ChildResponse findChildByIdForMember(
            @Param("childId") Long childId,
            @Param("memberId") Long memberId
    );

    //특정 부모가 해당 자녀에 접근할 권한이 있는지 확인
    int countChildAccess(
            @Param("childId") Long childId,
            @Param("memberId") Long memberId
    );

    ChildFeaturePermission findFeaturePermissionByChildId(
            @Param("childId") Long childId
    );

    int updateFeaturePermission(
            @Param("childId") Long childId,
            @Param("allowanceRequestEnabled") boolean allowanceRequestEnabled,
            @Param("usageLimitViewEnabled") boolean usageLimitViewEnabled
    );

    // 자녀에게 연결된 계좌 등 금융 기록이 있는지 확인 - 자녀 삭제 전에 금융 기록 있는지 확인
    int countFinancialHistory(@Param("childId") Long childId);

    // 자녀 프로필 수정
    void updateChild(Child child);

    // 자녀 삭제
    void softDeleteChild(@Param("childId") Long childId);

    // 임신 주차 조회
    PregnancyStatusRow findPregnancyStatus(
            @Param("childId") Long childId,
            @Param("memberId") Long memberId
    );
}

