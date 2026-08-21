package com.azas.domain.checklist.service;

import com.azas.domain.checklist.dto.ChecklistItemCompletionResult;
import com.azas.domain.checklist.dto.ChecklistItemCompletionTargetRow;
import com.azas.domain.checklist.entity.ChecklistItemStatus;
import com.azas.domain.checklist.mapper.ChecklistMapper;
import com.azas.domain.member.entity.Member;
import com.azas.domain.member.entity.MemberStatus;
import com.azas.domain.member.entity.MemberType;
import com.azas.domain.member.mapper.MemberMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChecklistItemCompletionService {

    private final MemberMapper memberMapper;
    private final ChecklistMapper checklistMapper;

    @Transactional
    public ChecklistItemCompletionResult updateCompletion(
            Long memberId,
            Long checklistItemId,
            boolean completed
    ) {
        validateActiveParent(memberId);

        if (checklistItemId == null || checklistItemId <= 0) {
            throw new BusinessException(
                    ErrorCode.CHECKLIST_ITEM_NOT_FOUND
            );
        }

        ChecklistItemCompletionTargetRow target =
                checklistMapper.findCompletionTargetForUpdate(
                        checklistItemId
                );

        if (target == null) {
            throw new BusinessException(
                    ErrorCode.CHECKLIST_ITEM_NOT_FOUND
            );
        }

        int parentAccessCount =
                checklistMapper.countActiveParentAccess(
                        memberId,
                        target.getChildId()
                );

        if (parentAccessCount <= 0) {
            throw new BusinessException(
                    ErrorCode.CHECKLIST_ITEM_ACCESS_DENIED
            );
        }

        ChecklistItemStatus requestedStatus = completed
                ? ChecklistItemStatus.COMPLETED
                : ChecklistItemStatus.PENDING;

        /*
         * 동일 상태 재요청은 성공으로 처리합니다.
         * 이미 완료된 항목을 다시 완료해도 completed_at은 갱신하지 않습니다.
         */
        if (target.getStatus() != requestedStatus) {
            checklistMapper.updateCompletion(
                    checklistItemId,
                    requestedStatus.name(),
                    completed ? memberId : null
            );
        }

        ChecklistItemCompletionTargetRow updated =
                checklistMapper.findCompletionTargetById(
                        checklistItemId
                );

        if (updated == null) {
            throw new BusinessException(
                    ErrorCode.CHECKLIST_ITEM_NOT_FOUND
            );
        }

        return new ChecklistItemCompletionResult(
                updated.getChecklistItemId(),
                updated.getStatus(),
                updated.getStatus() == ChecklistItemStatus.COMPLETED,
                updated.getCompletedAt()
        );
    }

    private void validateActiveParent(Long memberId) {
        Member member = memberMapper.findById(memberId);

        if (member == null ||
                member.getStatus() != MemberStatus.ACTIVE) {
            throw new BusinessException(
                    ErrorCode.INVALID_ACCESS_TOKEN
            );
        }

        if (member.getMemberType() != MemberType.PARENT) {
            throw new BusinessException(
                    ErrorCode.PARENT_ACCESS_REQUIRED
            );
        }
    }
}