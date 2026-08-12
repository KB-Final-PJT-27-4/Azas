package com.azas.domain.finance.account.mapper;

import com.azas.domain.finance.account.dto.AccountDetailRow;
import com.azas.domain.finance.account.dto.AccountBalanceHistorySnapshotRow;
import com.azas.domain.finance.account.dto.AccountBalanceRow;
import com.azas.domain.finance.account.dto.AccountUnlinkTargetRow;
import com.azas.domain.finance.account.dto.AccountPrimaryTargetRow;
import com.azas.domain.finance.account.dto.ChildAccountListRow;
import com.azas.domain.finance.account.dto.ChildAvailableAmountAccountRow;
import com.azas.domain.finance.account.dto.ParentAccountListRow;
import com.azas.domain.finance.account.entity.ChildUsageMode;
import com.azas.domain.finance.account.entity.FinancialAccountUsagePolicy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface FinancialAccountMapper {

    AccountBalanceRow findLinkedAccountBalanceById(
            @Param("financialAccountId")
            long financialAccountId
    );

    List<AccountBalanceHistorySnapshotRow> findBalanceSnapshotsByPeriod(
            @Param("financialAccountId")
            long financialAccountId,
            @Param("startObservedAt")
            LocalDateTime startObservedAt,
            @Param("endObservedAtExclusive")
            LocalDateTime endObservedAtExclusive
    );

    AccountDetailRow findLinkedAccountDetailById(
            @Param("financialAccountId")
            long financialAccountId
    );

    List<ParentAccountListRow> findActiveParentAccounts(
            @Param("memberId")
            long memberId
    );

    List<ChildAccountListRow> findActiveChildAccounts(
            @Param("childId")
            long childId
    );

    Long findActiveChildIdByMemberId(
            @Param("memberId")
            long memberId
    );

    ChildAvailableAmountAccountRow
    findActivePrimaryChildDemandDepositByMemberId(
            @Param("memberId")
            long memberId
    );

    BigDecimal sumDebitAmountByAccountAndPeriod(
            @Param("financialAccountId")
            long financialAccountId,
            @Param("startOccurredAt")
            LocalDateTime startOccurredAt,
            @Param("endOccurredAtExclusive")
            LocalDateTime endOccurredAtExclusive
    );

    int countActiveChildById(
            @Param("childId")
            long childId
    );

    FinancialAccountUsagePolicy findUsagePolicyByAccountId(
            @Param("financialAccountId")
            long financialAccountId
    );

    FinancialAccountUsagePolicy findUsagePolicyByAccountIdForUpdate(
            @Param("financialAccountId")
            long financialAccountId
    );

    int countActiveParentAccess(
            @Param("memberId")
            long memberId,
            @Param("childId")
            long childId
    );

    int countActiveChildMemberAccess(
            @Param("memberId")
            long memberId,
            @Param("childId")
            long childId
    );

    int updateUsagePolicy(
            @Param("financialAccountId")
            long financialAccountId,
            @Param("childUsageMode")
            ChildUsageMode childUsageMode,
            @Param("childMonthlyBudgetAmount")
            BigDecimal childMonthlyBudgetAmount,
            @Param("updatedByMemberId")
            long updatedByMemberId,
            @Param("updatedAt")
            LocalDateTime updatedAt
    );

    AccountUnlinkTargetRow findAccountUnlinkTargetByIdForUpdate(
            @Param("financialAccountId")
            long financialAccountId
    );

    int unlinkAccount(
            @Param("financialAccountId")
            long financialAccountId,
            @Param("unlinkedAt")
            LocalDateTime unlinkedAt
    );

    AccountPrimaryTargetRow findAccountPrimaryTargetByIdForUpdate(
            @Param("financialAccountId")
            long financialAccountId
    );

    int setPrimaryAccountForParentScope(
            @Param("connectedByMemberId")
            long connectedByMemberId,
            @Param("financialAccountId")
            long financialAccountId
    );

    int setPrimaryAccountForChildScope(
            @Param("childId")
            long childId,
            @Param("financialAccountId")
            long financialAccountId
    );
}
