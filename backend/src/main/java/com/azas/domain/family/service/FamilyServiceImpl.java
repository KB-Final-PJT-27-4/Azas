package com.azas.domain.family.service;

import com.azas.domain.family.dto.AllowanceRequestResponse;
import com.azas.domain.family.dto.ChildMemberLinkResponse;
import com.azas.domain.family.dto.FamilyGuardianListResponse;
import com.azas.domain.family.dto.FamilyGuardianResponse;
import com.azas.domain.family.mapper.FamilyMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @Override
    @Transactional
    public ChildMemberLinkResponse getChildMemberLink(Long memberId, Long childId) {
        validateChildAccess(memberId, childId);

        ChildMemberLinkResponse response = familyMapper.findChildMemberLinkByChildId(childId);

        if (response == null) {
            throw new BusinessException(ErrorCode.CHILD_NOT_FOUND);
        }
        return response;
    }

    @Override
    @Transactional
    public AllowanceRequestResponse requestAllowance(Long memberId, Long childId) {
        validateChildMemberAccess(memberId, childId);

        LocalDate requestMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate lastRequestMonth = familyMapper.findLastAllowanceRequestMonth(childId);

        if (requestMonth.equals(lastRequestMonth)) {
            throw new BusinessException(ErrorCode.ALLOWANCE_REQUEST_ALREADY_EXISTS);
        }

        int updatedCount = familyMapper.updateAllowanceRequest(childId, requestMonth);

        if (updatedCount == 0) {
            throw new BusinessException(ErrorCode.CHILD_NOT_FOUND);
        }

        BigDecimal childAvailableAmount = familyMapper.findChildAvailableAmount(childId);

        return new AllowanceRequestResponse(
                childId,
                true,
                requestMonth,
                LocalDateTime.now(),
                childAvailableAmount,
                "용돈 요청이 등록되었습니다."
        );
    }

    private void validateChildMemberAccess(Long memberId, Long childId) {
        int count = familyMapper.countChildMemberAccess(childId, memberId);
        if (count == 0) {
            throw new BusinessException(ErrorCode.CHILD_ACCESS_DENIED);
        }
    }
}