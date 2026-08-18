package com.azas.domain.finance.goal.mapper;

import com.azas.domain.finance.goal.dto.FinancialGoalAccountTargetRow;
import com.azas.domain.finance.goal.dto.FinancialGoalCheckpointInsertCommand;
import com.azas.domain.finance.goal.dto.FinancialGoalInsertCommand;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface FinancialGoalMapper {

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
}
