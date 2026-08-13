package com.azas.domain.finance.account.service;

import com.azas.domain.finance.account.dto.AccountBalanceResult;
import com.azas.domain.finance.account.dto.AccountBalanceRow;
import com.azas.domain.finance.account.mapper.FinancialAccountMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountBalanceService {

    private static final String PARENT_OWNER_TYPE = "PARENT";
    private static final String CHILD_OWNER_TYPE = "CHILD";

    private final FinancialAccountMapper financialAccountMapper;

    @Transactional(readOnly = true)
    public AccountBalanceResult getLatestBalance(
            long requesterMemberId,
            long financialAccountId
    ) {
        validateFinancialAccountId(financialAccountId);

        AccountBalanceRow row = financialAccountMapper
                .findLinkedAccountBalanceById(financialAccountId);

        if (row == null) {
            throw new BusinessException(
                    ErrorCode.FINANCIAL_ACCOUNT_NOT_FOUND
            );
        }

        validateAccess(requesterMemberId, row);
        validateSynchronizedBalance(row);

        return new AccountBalanceResult(
                row.getAccountId(),
                row.getBalance(),
                row.getBalanceUpdatedAt()
        );
    }

    private void validateFinancialAccountId(long financialAccountId) {
        if (financialAccountId < 1) {
            throw new BusinessException(ErrorCode.BADREQUEST);
        }
    }

    private void validateAccess(
            long requesterMemberId,
            AccountBalanceRow row
    ) {
        if (PARENT_OWNER_TYPE.equals(row.getOwnerType())) {
            if (row.getOwnerMemberId() != null
                    && row.getOwnerMemberId()
                    == requesterMemberId) {
                return;
            }

            throw accessDenied();
        }

        if (CHILD_OWNER_TYPE.equals(row.getOwnerType())) {
            validateChildAccountAccess(requesterMemberId, row);
            return;
        }

        throw accessDenied();
    }

    private void validateChildAccountAccess(
            long requesterMemberId,
            AccountBalanceRow row
    ) {
        Long childId = row.getChildId();

        if (childId == null) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR
            );
        }

        boolean parentAccess = financialAccountMapper
                .countActiveParentAccess(
                        requesterMemberId,
                        childId
                ) > 0;

        if (parentAccess) {
            return;
        }

        boolean childAccess = financialAccountMapper
                .countActiveChildMemberAccess(
                        requesterMemberId,
                        childId
                ) > 0;

        if (!childAccess) {
            throw accessDenied();
        }
    }

    private void validateSynchronizedBalance(AccountBalanceRow row) {
        if (row.getBalance() == null) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR
            );
        }

        if (row.getBalanceUpdatedAt() == null) {
            throw new BusinessException(
                    ErrorCode.ACCOUNT_BALANCE_NOT_AVAILABLE
            );
        }
    }

    private BusinessException accessDenied() {
        return new BusinessException(
                ErrorCode.FINANCIAL_ACCOUNT_ACCESS_DENIED
        );
    }
}
