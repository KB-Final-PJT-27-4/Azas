package com.azas.domain.finance.account.service;

import com.azas.domain.finance.account.dto.ParentAccountListItemResult;
import com.azas.domain.finance.account.dto.ParentAccountListResult;
import com.azas.domain.finance.account.dto.ParentAccountListRow;
import com.azas.domain.finance.account.mapper.FinancialAccountMapper;
import com.azas.domain.member.entity.Member;
import com.azas.domain.member.entity.MemberStatus;
import com.azas.domain.member.entity.MemberType;
import com.azas.domain.member.mapper.MemberMapper;
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
public class ParentAccountListService {

    private final MemberMapper memberMapper;
    private final FinancialAccountMapper financialAccountMapper;
    private final AccountNumberProtector accountNumberProtector;

    @Transactional(readOnly = true)
    public ParentAccountListResult getMyAccounts(
            long memberId
    ) {
        validateParentMember(memberId);

        List<ParentAccountListRow> rows =
                financialAccountMapper
                        .findActiveParentAccounts(memberId);

        List<ParentAccountListItemResult> accounts =
                rows == null
                        ? List.of()
                        : rows.stream()
                        .map(this::toResult)
                        .toList();

        BigDecimal totalBalance = accounts.stream()
                .map(ParentAccountListItemResult::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ParentAccountListResult(totalBalance, accounts);
    }

    private void validateParentMember(long memberId) {
        Member member = memberMapper.findById(memberId);

        if (member == null
                || member.getStatus() != MemberStatus.ACTIVE) {
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

    private ParentAccountListItemResult toResult(
            ParentAccountListRow row
    ) {
        return new ParentAccountListItemResult(
                row.getAccountId(),
                row.getAccountName(),
                decryptAccountNumber(
                        row.getAccountNumberCiphertext()
                ),
                row.getAccountProductType(),
                row.getBalance()
        );
    }

    private String decryptAccountNumber(byte[] ciphertext) {
        try {
            return accountNumberProtector.decrypt(ciphertext);
        } catch (IllegalArgumentException exception) {
            // 계좌번호나 암호문을 로그·예외 메시지에 노출하지 않는다.
            throw new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR
            );
        }
    }
}
