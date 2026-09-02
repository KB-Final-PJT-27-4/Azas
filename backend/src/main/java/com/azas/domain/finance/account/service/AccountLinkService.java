package com.azas.domain.finance.account.service;

import com.azas.domain.finance.account.dto.*;
import com.azas.domain.finance.account.entity.FinancialAccountOwnerType;
import com.azas.domain.finance.account.mapper.FinancialAccountMapper;
import com.azas.domain.member.entity.Member;
import com.azas.domain.member.entity.MemberStatus;
import com.azas.domain.member.entity.MemberType;
import com.azas.domain.member.mapper.MemberMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import com.azas.global.security.AccountNumberProtector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Service
public class AccountLinkService {
    private final MemberMapper memberMapper;
    private final FinancialAccountMapper financialAccountMapper;
    private final AccountNumberProtector accountNumberProtector;
    private final Clock clock;

    @Autowired
    public AccountLinkService(MemberMapper memberMapper,
                              FinancialAccountMapper financialAccountMapper,
                              AccountNumberProtector accountNumberProtector) {
        this(memberMapper, financialAccountMapper,
                accountNumberProtector, Clock.systemUTC());
    }

    AccountLinkService(MemberMapper memberMapper,
                       FinancialAccountMapper financialAccountMapper,
                       AccountNumberProtector accountNumberProtector,
                       Clock clock) {
        this.memberMapper = memberMapper;
        this.financialAccountMapper = financialAccountMapper;
        this.accountNumberProtector = accountNumberProtector;
        this.clock = clock;
    }

    @Transactional
    public AccountLinkResult link(long memberId, AccountLinkRequest request) {
        validateParent(memberId);
        FinancialAccountOwnerType ownerType = parseAndValidate(request);
        validateChildScope(memberId, ownerType, request.getChildId());

        List<Long> ids = request.getAccountIds();
        List<AccountLinkTargetRow> targets = financialAccountMapper
                .findAccountLinkTargetsForUpdate(ids);
        if (targets == null || targets.size() != ids.size()) {
            throw new BusinessException(ErrorCode.FINANCIAL_ACCOUNT_NOT_FOUND);
        }
        validateTargets(memberId, ownerType, request.getChildId(), targets);

        boolean hasActiveParentDemand = financialAccountMapper
                .countActiveParentDemandDeposit(memberId) > 0;
        if (!hasActiveParentDemand && ownerType == FinancialAccountOwnerType.CHILD) {
            throw new BusinessException(ErrorCode.PARENT_DEMAND_DEPOSIT_REQUIRED);
        }
        OptionalLong firstParentDemand = targets.stream()
                .filter(row -> ownerType == FinancialAccountOwnerType.PARENT
                        && "DEMAND_DEPOSIT".equals(row.getAccountProductType()))
                .mapToLong(AccountLinkTargetRow::getAccountId).min();
        if (!hasActiveParentDemand && firstParentDemand.isEmpty()) {
            throw new BusinessException(ErrorCode.PARENT_DEMAND_DEPOSIT_REQUIRED);
        }

        LocalDateTime linkedAt = LocalDateTime.ofInstant(
                clock.instant(), ZoneOffset.UTC
        );
        boolean hasActiveScopeDemand = ownerType == FinancialAccountOwnerType.PARENT
                ? hasActiveParentDemand
                : financialAccountMapper.countActiveChildDemandDeposit(
                        request.getChildId()
                ) > 0;
        OptionalLong firstScopeDemand = targets.stream()
                .filter(row -> "DEMAND_DEPOSIT".equals(row.getAccountProductType()))
                .mapToLong(AccountLinkTargetRow::getAccountId)
                .min();
        List<LinkedAccountResult> results = new ArrayList<>();
        List<Long> goalIds = new ArrayList<>();
        for (AccountLinkTargetRow target : targets.stream()
                .sorted(Comparator.comparing(AccountLinkTargetRow::getAccountId))
                .toList()) {
            boolean primary = !hasActiveScopeDemand
                    && firstScopeDemand.isPresent()
                    && target.getAccountId() == firstScopeDemand.getAsLong();
            if (financialAccountMapper.linkAccount(
                    target.getAccountId(), linkedAt, primary
            ) != 1) {
                throw new BusinessException(ErrorCode.FINANCIAL_ACCOUNT_ALREADY_EXISTS);
            }
            boolean requiresGoal = ownerType == FinancialAccountOwnerType.CHILD
                    && "SAVINGS".equals(target.getAccountProductType());
            if (requiresGoal) goalIds.add(target.getAccountId());
            results.add(toResult(target, linkedAt, primary, requiresGoal));
        }
        return new AccountLinkResult(results, goalIds);
    }

    private void validateParent(long memberId) {
        Member member = memberMapper.findById(memberId);
        if (member == null || member.getStatus() != MemberStatus.ACTIVE)
            throw new BusinessException(ErrorCode.INVALID_ACCESS_TOKEN);
        if (member.getMemberType() != MemberType.PARENT)
            throw new BusinessException(ErrorCode.PARENT_ACCESS_REQUIRED);
    }

    private FinancialAccountOwnerType parseAndValidate(AccountLinkRequest request) {
        if (request == null || request.getAccountIds() == null
                || request.getAccountIds().isEmpty()
                || request.getAccountIds().stream().anyMatch(id -> id == null || id < 1)
                || new HashSet<>(request.getAccountIds()).size()
                != request.getAccountIds().size()) {
            throw new BusinessException(ErrorCode.INVALID_ACCOUNT_LINK_REQUEST);
        }
        try {
            FinancialAccountOwnerType type = FinancialAccountOwnerType
                    .valueOf(request.getOwnerType());
            if ((type == FinancialAccountOwnerType.PARENT && request.getChildId() != null)
                    || (type == FinancialAccountOwnerType.CHILD
                    && (request.getChildId() == null || request.getChildId() < 1)))
                throw new IllegalArgumentException();
            return type;
        } catch (RuntimeException exception) {
            throw new BusinessException(ErrorCode.INVALID_ACCOUNT_LINK_REQUEST);
        }
    }

    private void validateChildScope(long memberId,
                                    FinancialAccountOwnerType type,
                                    Long childId) {
        if (type == FinancialAccountOwnerType.PARENT) return;
        if (financialAccountMapper.countActiveChildById(childId) < 1)
            throw new BusinessException(ErrorCode.CHILD_NOT_FOUND);
        if (financialAccountMapper.countActiveParentAccess(memberId, childId) < 1)
            throw new BusinessException(ErrorCode.CHILD_ACCESS_DENIED);
    }

    private void validateTargets(long memberId,
                                 FinancialAccountOwnerType type,
                                 Long childId,
                                 List<AccountLinkTargetRow> targets) {
        for (AccountLinkTargetRow target : targets) {
            if (!"ACTIVE".equals(target.getAccountStatus()))
                throw new BusinessException(ErrorCode.FINANCIAL_ACCOUNT_NOT_FOUND);
            if ("ACTIVE".equals(target.getLinkStatus()))
                throw new BusinessException(ErrorCode.FINANCIAL_ACCOUNT_ALREADY_EXISTS);
            if (!"DISCOVERED".equals(target.getLinkStatus())
                    && !"UNLINKED".equals(target.getLinkStatus()))
                throw new BusinessException(ErrorCode.INVALID_ACCOUNT_LINK_REQUEST);
            boolean scopeMatches = type.name().equals(target.getOwnerType())
                    && (type == FinancialAccountOwnerType.PARENT
                    ? Objects.equals(target.getOwnerMemberId(), memberId)
                    && target.getChildId() == null
                    : Objects.equals(target.getChildId(), childId));
            if (!scopeMatches)
                throw new BusinessException(ErrorCode.INVALID_ACCOUNT_LINK_REQUEST);
        }
    }

    private LinkedAccountResult toResult(AccountLinkTargetRow row,
                                         LocalDateTime linkedAt,
                                         boolean primary,
                                         boolean requiresGoal) {
        try {
            return new LinkedAccountResult(
                    row.getAccountId(), row.getOwnerType(), row.getChildId(),
                    row.getBankName(), row.getAccountName(),
                    accountNumberProtector.decrypt(row.getAccountNumberCiphertext()),
                    row.getAccountProductType(), row.getBalance(), "ACTIVE",
                    "ACTIVE", primary, requiresGoal, linkedAt
            );
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
