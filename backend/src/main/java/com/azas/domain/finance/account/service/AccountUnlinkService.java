package com.azas.domain.finance.account.service;

import com.azas.domain.finance.account.dto.AccountUnlinkTargetRow;
import com.azas.domain.finance.account.mapper.FinancialAccountMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class AccountUnlinkService {

    private static final String ACTIVE = "ACTIVE";
    private static final String DISCOVERED = "DISCOVERED";
    private static final String UNLINKED = "UNLINKED";

    private final FinancialAccountMapper financialAccountMapper;
    private final Clock clock;

    @Autowired
    public AccountUnlinkService(
            FinancialAccountMapper financialAccountMapper
    ) {
        this(financialAccountMapper, Clock.systemUTC());
    }

    AccountUnlinkService(
            FinancialAccountMapper financialAccountMapper,
            Clock clock
    ) {
        this.financialAccountMapper = financialAccountMapper;
        this.clock = clock;
    }

    @Transactional
    public void unlinkAccount(
            long requesterMemberId,
            long financialAccountId
    ) {
        validateFinancialAccountId(financialAccountId);

        AccountUnlinkTargetRow target = financialAccountMapper
                .findAccountUnlinkTargetByIdForUpdate(
                        financialAccountId
                );

        if (target == null) {
            throw new BusinessException(
                    ErrorCode.FINANCIAL_ACCOUNT_NOT_FOUND
            );
        }

        validateConnectionOwner(requesterMemberId, target);

        String linkStatus = target.getLinkStatus();
        if (UNLINKED.equals(linkStatus)) {
            return;
        }

        if (!ACTIVE.equals(linkStatus)
                && !DISCOVERED.equals(linkStatus)) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR
            );
        }

        LocalDateTime unlinkedAt = LocalDateTime.ofInstant(
                clock.instant(),
                ZoneOffset.UTC
        );

        int updatedRows = financialAccountMapper.unlinkAccount(
                financialAccountId,
                unlinkedAt
        );

        if (updatedRows != 1) {
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

    private void validateConnectionOwner(
            long requesterMemberId,
            AccountUnlinkTargetRow target
    ) {
        Long ownerMemberId = target.getOwnerMemberId();

        if (ownerMemberId == null
                || ownerMemberId != requesterMemberId) {
            throw new BusinessException(
                    ErrorCode.FINANCIAL_ACCOUNT_ACCESS_DENIED
            );
        }
    }
}
