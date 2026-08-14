package com.azas.domain.finance.account.service;

import com.azas.domain.finance.account.dto.AccountDetailChildResult;
import com.azas.domain.finance.account.dto.AccountDetailResult;
import com.azas.domain.finance.account.dto.AccountDetailRow;
import com.azas.domain.finance.account.dto.AccountFinancialGoalResult;
import com.azas.domain.finance.account.mapper.FinancialAccountMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import com.azas.global.security.AccountNumberProtector;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountDetailService {

    private static final String PARENT_OWNER_TYPE = "PARENT";
    private static final String CHILD_OWNER_TYPE = "CHILD";

    private final FinancialAccountMapper financialAccountMapper;
    private final AccountNumberProtector accountNumberProtector;

    @Transactional(readOnly = true)
    public AccountDetailResult getAccountDetail(
            long requesterMemberId,
            long financialAccountId
    ) {
        validateFinancialAccountId(financialAccountId);

        AccountDetailRow row = financialAccountMapper
                .findLinkedAccountDetailById(financialAccountId);

        if (row == null) {
            throw new BusinessException(
                    ErrorCode.FINANCIAL_ACCOUNT_NOT_FOUND
            );
        }

        validateAccess(requesterMemberId, row);

        return toResult(row);
    }

    private void validateFinancialAccountId(long financialAccountId) {
        if (financialAccountId < 1) {
            throw new BusinessException(ErrorCode.BADREQUEST);
        }
    }

    private void validateAccess(
            long requesterMemberId,
            AccountDetailRow row
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
            AccountDetailRow row
    ) {
        Long childId = row.getChildId();

        if (childId == null || row.getChildName() == null) {
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

    private AccountDetailResult toResult(AccountDetailRow row) {
        return new AccountDetailResult(
                row.getAccountId(),
                row.getOwnerType(),
                toChildResult(row),
                row.getOrganizationCode(),
                row.getBankName(),
                row.getAccountName(),
                decryptAccountNumber(
                        row.getAccountNumberCiphertext()
                ),
                row.getAccountProductType(),
                row.getBalance(),
                row.getBalanceUpdatedAt(),
                row.getAccountStatus(),
                row.isPrimaryAccount(),
                row.getOpenedAt(),
                row.getMaturityDate(),
                row.getLinkedAt(),
                toFinancialGoal(row)
        );
    }

    private AccountDetailChildResult toChildResult(
            AccountDetailRow row
    ) {
        if (!CHILD_OWNER_TYPE.equals(row.getOwnerType())) {
            return null;
        }

        return new AccountDetailChildResult(
                row.getChildId(),
                row.getChildName()
        );
    }

    private AccountFinancialGoalResult toFinancialGoal(
            AccountDetailRow row
    ) {
        if (row.getGoalNameSnapshot() == null
                && row.getGoalTargetAmount() == null
                && row.getGoalTargetDate() == null) {
            return null;
        }

        return new AccountFinancialGoalResult(
                row.getGoalNameSnapshot(),
                row.getGoalTargetAmount(),
                row.getGoalTargetDate()
        );
    }

    private String decryptAccountNumber(byte[] ciphertext) {
        try {
            return accountNumberProtector.decrypt(ciphertext);
        } catch (IllegalArgumentException exception) {
            // 암호문이나 키 정보가 외부 오류 응답으로 노출되지 않게 변환한다.
            throw new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR
            );
        }
    }

    private BusinessException accessDenied() {
        return new BusinessException(
                ErrorCode.FINANCIAL_ACCOUNT_ACCESS_DENIED
        );
    }
}
