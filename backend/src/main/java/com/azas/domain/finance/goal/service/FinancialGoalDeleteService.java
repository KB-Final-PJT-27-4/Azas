package com.azas.domain.finance.goal.service;

import com.azas.domain.finance.goal.dto.FinancialGoalUpdateTargetRow;
import com.azas.domain.finance.goal.mapper.FinancialGoalMapper;
import com.azas.domain.member.entity.Member;
import com.azas.domain.member.entity.MemberStatus;
import com.azas.domain.member.entity.MemberType;
import com.azas.domain.member.mapper.MemberMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FinancialGoalDeleteService {

    private final MemberMapper memberMapper;
    private final FinancialGoalMapper goalMapper;

    @Transactional
    public void delete(long requesterMemberId, long financialGoalId) {
        validateParent(requesterMemberId);
        if (financialGoalId < 1) {
            throw new BusinessException(ErrorCode.BADREQUEST);
        }

        FinancialGoalUpdateTargetRow goal = goalMapper.findAccessibleGoalForUpdate(
                financialGoalId, requesterMemberId);
        if (goal == null) {
            throw new BusinessException(ErrorCode.FINANCIAL_GOAL_NOT_FOUND);
        }

        List<Long> accountIds = safeList(
                goalMapper.findGoalAccountIds(financialGoalId));
        lockAndValidateAccounts(accountIds);

        for (Long accountId : accountIds) {
            if (goalMapper.deleteFinancialGoalAccount(
                    financialGoalId, accountId) != 1) {
                throw internalError();
            }
            if (goalMapper.clearAccountGoalSnapshot(accountId) != 1) {
                throw internalError();
            }
        }

        if (goalMapper.archiveFinancialGoal(financialGoalId) != 1) {
            throw internalError();
        }
    }

    private void validateParent(long memberId) {
        Member member = memberMapper.findById(memberId);
        if (member == null || member.getStatus() != MemberStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.INVALID_ACCESS_TOKEN);
        }
        if (member.getMemberType() != MemberType.PARENT) {
            throw new BusinessException(ErrorCode.PARENT_ACCESS_REQUIRED);
        }
    }

    private void lockAndValidateAccounts(List<Long> accountIds) {
        if (accountIds.isEmpty()) {
            return;
        }
        if (goalMapper.findAccountTargetsForUpdate(accountIds).size()
                != accountIds.size()) {
            throw internalError();
        }
    }

    private static List<Long> safeList(List<Long> values) {
        return values == null ? List.of() : List.copyOf(values);
    }

    private BusinessException internalError() {
        return new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
    }
}
