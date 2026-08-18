package com.azas.domain.finance.account.service;

import com.azas.domain.finance.account.dto.ChildAccountListItemResult;
import com.azas.domain.finance.account.dto.ChildAccountListResult;
import com.azas.domain.finance.account.dto.ChildAccountListRow;
import com.azas.domain.finance.account.mapper.FinancialAccountMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import com.azas.global.security.AccountNumberProtector;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChildAccountListService {

    private final FinancialAccountMapper financialAccountMapper;
    private final AccountNumberProtector accountNumberProtector;

    @Transactional(readOnly = true)
    public ChildAccountListResult getChildAccounts(
            long requesterMemberId,
            long childId
    ) {
        validateChildId(childId);
        validateActiveChild(childId);
        validateChildAccess(requesterMemberId, childId);

        List<ChildAccountListRow> rows =
                financialAccountMapper.findActiveChildAccounts(childId);

        List<ChildAccountListItemResult> accounts = rows == null
                ? List.of()
                : rows.stream()
                .map(this::toResult)
                .toList();

        BigDecimal totalBalance = accounts.stream()
                .map(ChildAccountListItemResult::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ChildAccountListResult(
                childId,
                totalBalance,
                accounts
        );
    }

    private void validateChildId(long childId) {
        if (childId < 1) {
            throw new BusinessException(ErrorCode.BADREQUEST);
        }
    }

    private void validateActiveChild(long childId) {
        if (financialAccountMapper.countActiveChildById(childId) == 0) {
            throw new BusinessException(ErrorCode.CHILD_NOT_FOUND);
        }
    }

    private void validateChildAccess(
            long requesterMemberId,
            long childId
    ) {
        boolean parentAccess =
                financialAccountMapper.countActiveParentAccess(
                        requesterMemberId,
                        childId
                ) > 0;

        boolean childMemberAccess =
                financialAccountMapper.countActiveChildMemberAccess(
                        requesterMemberId,
                        childId
                ) > 0;

        if (!parentAccess && !childMemberAccess) {
            throw new BusinessException(ErrorCode.CHILD_ACCESS_DENIED);
        }
    }

    private ChildAccountListItemResult toResult(
            ChildAccountListRow row
    ) {
        return new ChildAccountListItemResult(
                row.getAccountId(),
                row.getAccountName(),
                decryptAccountNumber(row.getAccountNumberCiphertext()),
                row.getAccountProductType(),
                row.getBalance()
        );
    }

    private String decryptAccountNumber(byte[] ciphertext) {
        try {
            return accountNumberProtector.decrypt(ciphertext);
        } catch (IllegalArgumentException exception) {
            // 암호문이나 키 정보가 외부 오류 응답에 노출되지 않도록 공통 오류로 변환한다.
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
