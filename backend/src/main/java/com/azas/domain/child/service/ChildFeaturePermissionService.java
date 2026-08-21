package com.azas.domain.child.service;

import com.azas.domain.child.dto.ChildFeaturePermissionRequest;
import com.azas.domain.child.entity.ChildFeaturePermission;
import com.azas.domain.child.mapper.ChildMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChildFeaturePermissionService {

    private final ChildMapper childMapper;

    @Transactional(readOnly = true)
    public ChildFeaturePermission getPermission(
            long requesterMemberId,
            long childId
    ) {
        validateParentAccess(requesterMemberId, childId);
        return findPermission(childId);
    }

    @Transactional
    public ChildFeaturePermission updatePermission(
            long requesterMemberId,
            long childId,
            ChildFeaturePermissionRequest request
    ) {
        validateParentAccess(requesterMemberId, childId);

        int updatedCount = childMapper.updateFeaturePermission(
                childId,
                request.getAllowanceRequestEnabled(),
                request.getUsageLimitViewEnabled()
        );

        if (updatedCount != 1) {
            throw new BusinessException(ErrorCode.CHILD_NOT_FOUND);
        }

        return findPermission(childId);
    }

    @Transactional(readOnly = true)
    public void validateAllowanceRequestEnabled(long childId) {
        if (!findPermission(childId).isAllowanceRequestEnabled()) {
            throw new BusinessException(ErrorCode.ALLOWANCE_REQUEST_DISABLED);
        }
    }

    @Transactional(readOnly = true)
    public void validateUsageLimitViewEnabled(long childId) {
        if (!findPermission(childId).isUsageLimitViewEnabled()) {
            throw new BusinessException(
                    ErrorCode.CHILD_USAGE_LIMIT_VIEW_DISABLED
            );
        }
    }

    private void validateParentAccess(long memberId, long childId) {
        if (childMapper.countChildAccess(childId, memberId) == 0) {
            throw new BusinessException(ErrorCode.CHILD_ACCESS_DENIED);
        }
    }

    private ChildFeaturePermission findPermission(long childId) {
        ChildFeaturePermission permission = childMapper
                .findFeaturePermissionByChildId(childId);

        if (permission == null) {
            throw new BusinessException(ErrorCode.CHILD_NOT_FOUND);
        }

        return permission;
    }
}
