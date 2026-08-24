package com.azas.domain.finance.account.service;

import com.azas.domain.finance.account.dto.AccountTransactionDetailRow;
import com.azas.domain.finance.account.dto.ChildcareTransactionTagResponse;
import com.azas.domain.finance.account.mapper.FinancialAccountMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChildcareTransactionTagService {

    private static final String DEBIT = "DEBIT";
    private static final String PARENT = "PARENT";

    private final FinancialAccountMapper financialAccountMapper;
    private final AccountDetailService accountDetailService;

    @Transactional
    public ChildcareTransactionTagResponse updateTag(
            long requesterMemberId,
            long accountTransactionId,
            Long childId
    ) {
        if (accountTransactionId < 1 || (childId != null && childId < 1)) {
            throw new BusinessException(ErrorCode.BADREQUEST);
        }

        AccountTransactionDetailRow transaction = financialAccountMapper
                .findAccountTransactionById(accountTransactionId);
        if (transaction == null || transaction.getFinancialAccountId() == null) {
            throw new BusinessException(ErrorCode.ACCOUNT_TRANSACTION_NOT_FOUND);
        }

        accountDetailService.getAccountDetail(
                requesterMemberId,
                transaction.getFinancialAccountId()
        );
        validateExternalParentDebit(transaction);
        validateChildAccess(requesterMemberId, childId);

        if (financialAccountMapper.updateChildcareChildIdForExternalDebit(
                accountTransactionId,
                childId
        ) != 1) {
            throw new BusinessException(ErrorCode.INELIGIBLE_CHILDCARE_TRANSACTION);
        }

        return ChildcareTransactionTagResponse.of(accountTransactionId, childId);
    }

    private void validateExternalParentDebit(
            AccountTransactionDetailRow transaction
    ) {
        if (!DEBIT.equals(transaction.getDirection())
                || transaction.getCounterpartyAccountId() != null
                || !PARENT.equals(transaction.getOwnerType())) {
            throw new BusinessException(
                    ErrorCode.INELIGIBLE_CHILDCARE_TRANSACTION
            );
        }
    }

    private void validateChildAccess(long requesterMemberId, Long childId) {
        if (childId == null) {
            return;
        }
        if (financialAccountMapper.countActiveChildById(childId) < 1) {
            throw new BusinessException(ErrorCode.CHILD_NOT_FOUND);
        }
        if (financialAccountMapper.countActiveParentAccess(
                requesterMemberId,
                childId
        ) < 1) {
            throw new BusinessException(ErrorCode.PARENT_ACCESS_REQUIRED);
        }
    }
}
