package com.azas.domain.family.service;

import com.azas.domain.family.dto.FamilyGuardianListResponse;
import com.azas.domain.family.dto.FamilyGuardianResponse;
import com.azas.domain.family.mapper.FamilyMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FamilyServiceImpl implements FamilyService {

    private final FamilyMapper familyMapper;

    @Override
    @Transactional(readOnly = true)
    public FamilyGuardianListResponse getFamilyMembers(Long memberId, Long childId) {
        validateChildAccess(memberId, childId);

        List<FamilyGuardianResponse> items = familyMapper.findFamilyMembers(
                childId,
                memberId
        );

        return new FamilyGuardianListResponse(items);
    }

    private void validateChildAccess(Long memberId, Long childId) {
        int count = familyMapper.countChildAccess(childId, memberId);

        if (count == 0) {
            throw new BusinessException(ErrorCode.CHILD_NOT_FOUND);
        }
    }
}