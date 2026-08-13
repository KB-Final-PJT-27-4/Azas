package com.azas.domain.finance.account.service;

import com.azas.domain.finance.account.dto.DiscoveredAccountListResult;
import com.azas.domain.finance.account.dto.DiscoveredAccountResult;
import com.azas.domain.finance.account.dto.DiscoveredAccountRow;
import com.azas.domain.finance.account.entity.FinancialAccountOwnerType;
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

import java.util.List;

@Service
@RequiredArgsConstructor
public class DiscoveredAccountService {

    private final MemberMapper memberMapper;
    private final FinancialAccountMapper financialAccountMapper;
    private final AccountNumberProtector accountNumberProtector;

    @Transactional(readOnly = true)
    public DiscoveredAccountListResult getDiscoveredAccounts(
            long requesterMemberId,
            String ownerTypeValue,
            Long childId
    ) {
        validateParentMember(requesterMemberId);
        FinancialAccountOwnerType ownerType = parseOwnerType(ownerTypeValue);
        validateScope(requesterMemberId, ownerType, childId);

        List<DiscoveredAccountRow> rows = financialAccountMapper
                .findDiscoveredAccounts(
                        requesterMemberId,
                        ownerType.name(),
                        childId
                );

        return new DiscoveredAccountListResult(
                rows == null ? List.of() : rows.stream()
                        .map(this::toResult)
                        .toList()
        );
    }

    private void validateParentMember(long memberId) {
        Member member = memberMapper.findById(memberId);
        if (member == null || member.getStatus() != MemberStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.INVALID_ACCESS_TOKEN);
        }
        if (member.getMemberType() != MemberType.PARENT) {
            throw new BusinessException(ErrorCode.PARENT_ACCESS_REQUIRED);
        }
    }

    private FinancialAccountOwnerType parseOwnerType(String value) {
        try {
            return FinancialAccountOwnerType.valueOf(value);
        } catch (RuntimeException exception) {
            throw new BusinessException(
                    ErrorCode.INVALID_ACCOUNT_DISCOVERY_REQUEST
            );
        }
    }

    private void validateScope(
            long requesterMemberId,
            FinancialAccountOwnerType ownerType,
            Long childId
    ) {
        if (ownerType == FinancialAccountOwnerType.PARENT) {
            if (childId != null) {
                throw new BusinessException(
                        ErrorCode.INVALID_ACCOUNT_DISCOVERY_REQUEST
                );
            }
            return;
        }

        if (childId == null || childId < 1) {
            throw new BusinessException(
                    ErrorCode.INVALID_ACCOUNT_DISCOVERY_REQUEST
            );
        }
        if (financialAccountMapper.countActiveChildById(childId) < 1) {
            throw new BusinessException(ErrorCode.CHILD_NOT_FOUND);
        }
        if (financialAccountMapper.countActiveParentAccess(
                requesterMemberId,
                childId
        ) < 1) {
            throw new BusinessException(ErrorCode.CHILD_ACCESS_DENIED);
        }
        if (financialAccountMapper.countActiveParentDemandDeposit(
                requesterMemberId
        ) < 1) {
            throw new BusinessException(
                    ErrorCode.PARENT_DEMAND_DEPOSIT_REQUIRED
            );
        }
    }

    private DiscoveredAccountResult toResult(DiscoveredAccountRow row) {
        try {
            return new DiscoveredAccountResult(
                    row.getAccountId(),
                    row.getBankName(),
                    accountNumberProtector.decrypt(
                            row.getAccountNumberCiphertext()
                    ),
                    row.getAccountProductType(),
                    row.getBalance()
            );
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
