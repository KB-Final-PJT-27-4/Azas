package com.azas.domain.finance.account.service;

import com.azas.domain.finance.account.dto.AccountPrimaryTargetRow;
import com.azas.domain.finance.account.mapper.FinancialAccountMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountPrimaryService {

    private static final String PARENT = "PARENT";
    private static final String CHILD = "CHILD";
    private static final String ACTIVE = "ACTIVE";

    private final FinancialAccountMapper financialAccountMapper;

    @Transactional
    public void setPrimaryAccount(
            long requesterMemberId,
            long financialAccountId
    ) {
        validateFinancialAccountId(financialAccountId);

        AccountPrimaryTargetRow target = financialAccountMapper
                .findAccountPrimaryTargetByIdForUpdate(
                        financialAccountId
                );

        if (target == null) {
            throw new BusinessException(
                    ErrorCode.FINANCIAL_ACCOUNT_NOT_FOUND
            );
        }

        validateActiveTarget(target);
        validateAccess(requesterMemberId, target);

        if (Boolean.TRUE.equals(target.getPrimaryAccount())) {
            return;
        }

        int updatedRows;
        if (PARENT.equals(target.getOwnerType())) {
            updatedRows = financialAccountMapper
                    .setPrimaryAccountForParentScope(
                            target.getOwnerMemberId(),
                            financialAccountId
                    );
        } else if (CHILD.equals(target.getOwnerType())) {
            updatedRows = financialAccountMapper
                    .setPrimaryAccountForChildScope(
                            target.getChildId(),
                            financialAccountId
                    );
        } else {
            throw new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR
            );
        }

        if (updatedRows < 1) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR
            );
        }
    }

    private void validateFinancialAccountId(long financialAccountId) {
        if (financialAccountId < 1) {
            throw new BusinessException(ErrorCode.BADREQUEST);
        }
    }

    private void validateActiveTarget(AccountPrimaryTargetRow target) {
        if (!ACTIVE.equals(target.getAccountStatus())
                || !ACTIVE.equals(target.getLinkStatus())) {
            throw new BusinessException(
                    ErrorCode.FINANCIAL_ACCOUNT_NOT_FOUND
            );
        }
    }

    private void validateAccess(
            long requesterMemberId,
            AccountPrimaryTargetRow target
    ) {
        if (PARENT.equals(target.getOwnerType())) {
            if (target.getOwnerMemberId() == null
                    || target.getOwnerMemberId()
                    != requesterMemberId) {
                throw new BusinessException(
                        ErrorCode.FINANCIAL_ACCOUNT_ACCESS_DENIED
                );
            }
            return;
        }

        if (!CHILD.equals(target.getOwnerType())
                || target.getChildId() == null) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR
            );
        }

        boolean parentAccess = financialAccountMapper
                .countActiveParentAccess(
                        requesterMemberId,
                        target.getChildId()
                ) > 0;
        boolean childAccess = financialAccountMapper
                .countActiveChildMemberAccess(
                        requesterMemberId,
                        target.getChildId()
                ) > 0;

        if (!parentAccess && !childAccess) {
            throw new BusinessException(
                    ErrorCode.FINANCIAL_ACCOUNT_ACCESS_DENIED
            );
        }
    }
}
