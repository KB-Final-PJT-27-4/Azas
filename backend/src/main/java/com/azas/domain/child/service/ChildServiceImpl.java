package com.azas.domain.child.service;

import com.azas.domain.child.dto.ChildCreateRequest;
import com.azas.domain.child.dto.ChildListResponse;
import com.azas.domain.child.dto.ChildResponse;
import com.azas.domain.child.dto.ChildSummaryResponse;
import com.azas.domain.child.dto.ChildUpdateRequest;
import com.azas.domain.child.entity.BirthStatus;
import com.azas.domain.child.entity.Child;
import com.azas.domain.child.entity.RelationType;
import com.azas.domain.child.mapper.ChildMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChildServiceImpl implements ChildService {

    private final ChildMapper childMapper;

    // 자녀 프로필 생성
    @Override
    @Transactional
    public ChildResponse createChild(Long memberId, ChildCreateRequest request) {
        validateCreateRequest(request);

        Child child = Child.from(request);
        childMapper.insertChild(child);

        RelationType relationType = getRelationTypeOrDefault(request);
        childMapper.insertChildParent(child.getChildId(), memberId, relationType);

        return childMapper.findChildByIdForMember(child.getChildId(), memberId);
    }

    // 접근 가능한 자녀 목록 조회
    @Override
    @Transactional(readOnly = true)
    public ChildListResponse getChildren(Long memberId) {
        List<ChildSummaryResponse> items = childMapper.findChildrenByMemberId(memberId);
        return new ChildListResponse(items);
    }

    // 자녀 상세 조회
    @Override
    @Transactional(readOnly = true)
    public ChildResponse getChild(Long memberId, Long childId) {
        validateChildAccess(memberId, childId);
        return childMapper.findChildByIdForMember(childId, memberId);
    }

    // 자녀 프로필 수정
    @Override
    @Transactional
    public ChildResponse updateChild(Long memberId, Long childId, ChildUpdateRequest request) {
        validateChildAccess(memberId, childId);

        Child child = new Child();
        child.setChildId(childId);
        child.update(request);

        childMapper.updateChild(child);

        return childMapper.findChildByIdForMember(childId, memberId);
    }

    // 자녀 프로필 삭제
    @Override
    @Transactional
    public void deleteChild(Long memberId, Long childId) {
        validateChildAccess(memberId, childId);

        int financialHistoryCount = childMapper.countFinancialHistory(childId);
        if (financialHistoryCount > 0) {
            throw new IllegalArgumentException("CHILD_HAS_FINANCIAL_HISTORY");
        }

        childMapper.softDeleteChild(childId);
    }

    private RelationType getRelationTypeOrDefault(ChildCreateRequest request) {
        if (request.getRelationType() == null) {
            return RelationType.GUARDIAN;
        }

        return request.getRelationType();
    }

    private void validateChildAccess(Long memberId, Long childId) {
        int count = childMapper.countChildAccess(childId, memberId);

        if (count == 0) {
            // TODO: 공통 예외 처리 구현 후 BusinessException으로 교체
            throw new IllegalArgumentException("자녀 정보에 접근할 권한이 없습니다.");
        }
    }

    private void validateCreateRequest(ChildCreateRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            // TODO: 공통 예외 처리 구현 후 BusinessException으로 교체
            throw new IllegalArgumentException("자녀 이름은 필수입니다.");
        }

        if (request.getBirthStatus() == null) {
            // TODO: 공통 예외 처리 구현 후 BusinessException으로 교체
            throw new IllegalArgumentException("출생 상태는 필수입니다.");
        }

        if (request.getBirthStatus() == BirthStatus.EXPECTED
                && request.getExpectedBirthDate() == null) {
            throw new IllegalArgumentException("출생 예정일은 필수입니다.");
        }

        if (request.getBirthStatus() == BirthStatus.BORN
                && request.getBirthDate() == null) {
            throw new IllegalArgumentException("생년월일은 필수입니다.");
        }
    }
}