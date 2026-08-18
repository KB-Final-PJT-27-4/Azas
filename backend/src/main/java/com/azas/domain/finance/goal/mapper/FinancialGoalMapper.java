package com.azas.domain.finance.goal.mapper;

import com.azas.domain.finance.goal.dto.FinancialGoalAccountTargetRow;
import com.azas.domain.finance.goal.dto.FinancialGoalCheckpointInsertCommand;
import com.azas.domain.finance.goal.dto.FinancialGoalCheckpointRow;
import com.azas.domain.finance.goal.dto.FinancialGoalDetailRow;
import com.azas.domain.finance.goal.dto.FinancialGoalInsertCommand;
import com.azas.domain.finance.goal.dto.FinancialGoalListRow;
import com.azas.domain.finance.goal.dto.FinancialGoalUpdateTargetRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface FinancialGoalMapper {

    FinancialGoalUpdateTargetRow findAccessibleGoalForUpdate(
            @Param("financialGoalId") long financialGoalId,
            @Param("memberId") long memberId
    );

    List<Long> findGoalAccountIds(
            @Param("financialGoalId") long financialGoalId
    );

    List<FinancialGoalDetailRow> findAccessibleGoalDetailWithAccounts(
            @Param("financialGoalId") long financialGoalId,
            @Param("memberId") long memberId
    );

    List<FinancialGoalCheckpointRow> findGoalCheckpoints(
            @Param("financialGoalId") long financialGoalId
    );

    List<FinancialGoalListRow> findActiveAndAchievedGoalsWithAccounts(
            @Param("childId") long childId
    );

    List<FinancialGoalAccountTargetRow> findAccountTargetsForUpdate(
            @Param("accountIds") List<Long> accountIds
    );

    int insertFinancialGoal(FinancialGoalInsertCommand command);

    int insertFinancialGoalAccount(
            @Param("financialGoalId") long financialGoalId,
            @Param("financialAccountId") long financialAccountId
    );

    int insertFinancialGoalCheckpoint(
            FinancialGoalCheckpointInsertCommand command
    );

    int updateAccountGoalSnapshot(
            @Param("financialAccountId") long financialAccountId,
            @Param("financialGoalTemplateId") Long financialGoalTemplateId,
            @Param("title") String title,
            @Param("targetAmount") BigDecimal targetAmount,
            @Param("targetDate") LocalDate targetDate
    );

    int updateFinancialGoal(
            @Param("financialGoalId") long financialGoalId,
            @Param("targetAmount") BigDecimal targetAmount,
            @Param("targetDate") LocalDate targetDate,
            @Param("monthlySavingAmount") BigDecimal monthlySavingAmount,
            @Param("status") String status
    );

    int deleteFinancialGoalAccount(
            @Param("financialGoalId") long financialGoalId,
            @Param("financialAccountId") long financialAccountId
    );

    int clearAccountGoalSnapshot(
            @Param("financialAccountId") long financialAccountId
    );

    int archiveFinancialGoal(
            @Param("financialGoalId") long financialGoalId
    );

    int updateFinancialGoalCheckpoint(
            @Param("financialGoalCheckpointId") long financialGoalCheckpointId,
            @Param("targetAmount") BigDecimal targetAmount,
            @Param("reachedAt") java.time.LocalDateTime reachedAt
    );
}
