package com.azas.domain.finance.account.mapper;

import com.azas.domain.finance.account.dto.AccountDetailRow;
import com.azas.domain.finance.account.dto.ChildAccountListRow;
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
}
