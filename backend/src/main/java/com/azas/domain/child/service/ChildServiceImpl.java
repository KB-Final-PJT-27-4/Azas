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
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
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
    public ChildResponse updateChild(
            Long memberId,
            Long childId,
            ChildUpdateRequest request
    ) {
        if (request == null) {
            throw new BusinessException(
                    ErrorCode.BADREQUEST
            );
        }

        validateChildAccess(memberId, childId);

        ChildResponse current =
                childMapper.findChildByIdForMember(
                        childId,
                        memberId
                );

        if (current == null) {
            throw new BusinessException(
                    ErrorCode.CHILD_NOT_FOUND
            );
        }

        Child mergedChild =
                mergeChild(
                        childId,
                        current,
                        request
                );

        validateUpdateState(mergedChild);

        childMapper.updateChild(mergedChild);

        return childMapper.findChildByIdForMember(
                childId,
                memberId
        );
    }

    // 병합 메서드
    private Child mergeChild(
            Long childId,
            ChildResponse current,
            ChildUpdateRequest request
    ) {
        Child child = new Child();

        child.setChildId(childId);

        child.setName(
                request.getName() != null
                        ? request.getName()
                        : current.getName()
        );

        child.setBirthStatus(
                request.getBirthStatus() != null
                        ? request.getBirthStatus()
                        : current.getBirthStatus()
        );

        child.setExpectedBirthDate(
                request.getExpectedBirthDate() != null
                        ? request.getExpectedBirthDate()
                        : current.getExpectedBirthDate()
        );

        child.setBirthDate(
                request.getBirthDate() != null
                        ? request.getBirthDate()
                        : current.getBirthDate()
        );

        child.setGender(
                request.getGender() != null
                        ? request.getGender()
                        : current.getGender()
        );

        child.setProfileImageUrl(
                request.getProfileImageUrl() != null
                        ? request.getProfileImageUrl()
                        : current.getProfileImageUrl()
        );

        return child;
    }


    // 검증
    private void validateUpdateState(Child child) {
        if (
                child.getName() == null
                        || child.getName().trim().isEmpty()
        ) {
            throw new BusinessException(
                    ErrorCode.CHILD_INVALID_NAME
            );
        }

        if (child.getBirthStatus() == null) {
            throw new BusinessException(
                    ErrorCode.CHILD_INVALID_BIRTH_STATUS
            );
        }

        if (
                child.getBirthStatus() == BirthStatus.EXPECTED
                        && child.getExpectedBirthDate() == null
        ) {
            throw new BusinessException(
                    ErrorCode.CHILD_EXPECTED_BIRTH_DATE_REQUIRED
            );
        }

        if (
                child.getBirthStatus() == BirthStatus.BORN
                        && child.getBirthDate() == null
        ) {
            throw new BusinessException(
                    ErrorCode.CHILD_BIRTH_DATE_REQUIRED
            );
        }
    }


    // 자녀 프로필 삭제
    @Override
    @Transactional
    public void deleteChild(Long memberId, Long childId) {
        validateChildAccess(memberId, childId);

        int financialHistoryCount = childMapper.countFinancialHistory(childId);
        if (financialHistoryCount > 0) {
            throw new BusinessException(ErrorCode.CHILD_HAS_FINANCIAL_HISTORY);
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
            throw new BusinessException(ErrorCode.CHILD_NOT_FOUND);
        }
    }

    private void validateCreateRequest(ChildCreateRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.CHILD_INVALID_NAME);
        }

        if (request.getBirthStatus() == null) {
            throw new BusinessException(ErrorCode.CHILD_INVALID_BIRTH_STATUS);
        }

        if (request.getBirthStatus() == BirthStatus.EXPECTED
                && request.getExpectedBirthDate() == null) {
            throw new BusinessException(ErrorCode.CHILD_EXPECTED_BIRTH_DATE_REQUIRED);
        }

        if (request.getBirthStatus() == BirthStatus.BORN
                && request.getBirthDate() == null) {
            throw new BusinessException(ErrorCode.CHILD_BIRTH_DATE_REQUIRED);
        }
    }
}