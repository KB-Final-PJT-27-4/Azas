package com.azas.domain.finance.goal.service;

import com.azas.domain.finance.goal.dto.FinancialGoalAccountTargetRow;
import com.azas.domain.finance.goal.dto.FinancialGoalCheckpointRow;
import com.azas.domain.finance.goal.dto.FinancialGoalDetailResult;
import com.azas.domain.finance.goal.dto.FinancialGoalUpdateRequest;
import com.azas.domain.finance.goal.dto.FinancialGoalUpdateTargetRow;
import com.azas.domain.finance.goal.mapper.FinancialGoalMapper;
import com.azas.domain.member.entity.Member;
import com.azas.domain.member.mapper.MemberMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinancialGoalUpdateServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-18T01:30:00Z");

    @Mock MemberMapper memberMapper;
    @Mock FinancialGoalMapper goalMapper;
    @Mock FinancialGoalDetailService detailService;
    private FinancialGoalUpdateService service;

    @BeforeEach
    void setUp() {
        service = new FinancialGoalUpdateService(memberMapper, goalMapper,
                detailService, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void updatesSettingsAndFinalAccountLinks() {
        givenParent();
        FinancialGoalUpdateTargetRow goal = goal();
        when(goalMapper.findAccessibleGoalForUpdate(31L, 8L))
                .thenReturn(goal);
        when(goalMapper.findGoalAccountIds(31L)).thenReturn(List.of(11L, 12L));
        when(goalMapper.findAccountTargetsForUpdate(List.of(11L, 12L, 13L)))
                .thenReturn(List.of(
                        account(11L, "4800000", 31L),
                        account(12L, "4800000", 31L),
                        account(13L, "1000000", null)));
        when(goalMapper.deleteFinancialGoalAccount(31L, 12L)).thenReturn(1);
        when(goalMapper.clearAccountGoalSnapshot(12L)).thenReturn(1);
        when(goalMapper.insertFinancialGoalAccount(31L, 13L)).thenReturn(1);
        when(goalMapper.updateFinancialGoal(eq(31L), any(), any(), any(),
                eq("ACTIVE"))).thenReturn(1);
        when(goalMapper.updateAccountGoalSnapshot(anyLong(), eq(1L),
                eq("대학자금"), any(), any())).thenReturn(1);
        List<FinancialGoalCheckpointRow> checkpoints = checkpoints();
        when(goalMapper.findGoalCheckpoints(31L)).thenReturn(checkpoints);
        when(goalMapper.updateFinancialGoalCheckpoint(anyLong(), any(), any()))
                .thenReturn(1);
        FinancialGoalDetailResult expected = mock(FinancialGoalDetailResult.class);
        when(detailService.getGoal(8L, 31L)).thenReturn(expected);

        FinancialGoalDetailResult result = service.update(8L, 31L,
                new FinancialGoalUpdateRequest(
                        new BigDecimal("40000000"),
                        LocalDate.of(2046, 3, 31),
                        List.of(11L, 13L)));

        assertSame(expected, result);
        verify(goalMapper).deleteFinancialGoalAccount(31L, 12L);
        verify(goalMapper).clearAccountGoalSnapshot(12L);
        verify(goalMapper).insertFinancialGoalAccount(31L, 13L);
        verify(goalMapper).updateFinancialGoal(31L,
                new BigDecimal("40000000"), LocalDate.of(2046, 3, 31),
                new BigDecimal("145532"), "ACTIVE");
        verify(goalMapper).updateAccountGoalSnapshot(11L, 1L,
                "대학자금", new BigDecimal("40000000"),
                LocalDate.of(2046, 3, 31));
        verify(goalMapper).updateAccountGoalSnapshot(13L, 1L,
                "대학자금", new BigDecimal("40000000"),
                LocalDate.of(2046, 3, 31));
        verify(goalMapper).updateFinancialGoalCheckpoint(101L,
                new BigDecimal("4000000.00"),
                java.time.LocalDateTime.ofInstant(NOW, ZoneOffset.UTC));
        verify(goalMapper).updateFinancialGoalCheckpoint(102L,
                new BigDecimal("10000000.00"), null);
    }

    @Test
    void updatesOnlyTargetAmountAndKeepsExistingAccounts() {
        givenParent();
        FinancialGoalUpdateTargetRow goal = goal();
        when(goalMapper.findAccessibleGoalForUpdate(31L, 8L)).thenReturn(goal);
        when(goalMapper.findGoalAccountIds(31L)).thenReturn(List.of(11L));
        when(goalMapper.findAccountTargetsForUpdate(List.of(11L)))
                .thenReturn(List.of(account(11L, "4800000", 31L)));
        when(goalMapper.updateFinancialGoal(anyLong(), any(), any(), any(), any()))
                .thenReturn(1);
        when(goalMapper.updateAccountGoalSnapshot(anyLong(), any(), any(), any(), any()))
                .thenReturn(1);
        List<FinancialGoalCheckpointRow> checkpoints = checkpoints();
        when(goalMapper.findGoalCheckpoints(31L)).thenReturn(checkpoints);
        when(goalMapper.updateFinancialGoalCheckpoint(anyLong(), any(), any()))
                .thenReturn(1);
        FinancialGoalDetailResult expected = mock(FinancialGoalDetailResult.class);
        when(detailService.getGoal(8L, 31L)).thenReturn(expected);

        service.update(8L, 31L, new FinancialGoalUpdateRequest(
                new BigDecimal("35000000"), null, null));

        verify(goalMapper, never()).deleteFinancialGoalAccount(anyLong(), anyLong());
        verify(goalMapper, never()).insertFinancialGoalAccount(anyLong(), anyLong());
    }

    @Test
    void rejectsEmptyPatch() {
        givenParent();
        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.update(8L, 31L,
                        new FinancialGoalUpdateRequest(null, null, null)));
        assertEquals(ErrorCode.INVALID_FINANCIAL_GOAL_REQUEST,
                exception.getErrorCode());
        verify(goalMapper, never()).findAccessibleGoalForUpdate(anyLong(), anyLong());
    }

    @Test
    void rejectsRemovingEveryAccount() {
        givenParent();
        FinancialGoalUpdateTargetRow goal = goal();
        when(goalMapper.findAccessibleGoalForUpdate(31L, 8L)).thenReturn(goal);
        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.update(8L, 31L,
                        new FinancialGoalUpdateRequest(null, null, List.of())));
        assertEquals(ErrorCode.INVALID_FINANCIAL_GOAL_REQUEST,
                exception.getErrorCode());
    }

    @Test
    void rejectsAccountAssignedToAnotherGoal() {
        givenParent();
        FinancialGoalUpdateTargetRow goal = goal();
        when(goalMapper.findAccessibleGoalForUpdate(31L, 8L)).thenReturn(goal);
        when(goalMapper.findGoalAccountIds(31L)).thenReturn(List.of(11L));
        when(goalMapper.findAccountTargetsForUpdate(List.of(11L, 13L)))
                .thenReturn(List.of(
                        account(11L, "4800000", 31L),
                        account(13L, "1000000", 77L)));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.update(8L, 31L,
                        new FinancialGoalUpdateRequest(
                                null, null, List.of(11L, 13L))));
        assertEquals(ErrorCode.FINANCIAL_ACCOUNT_GOAL_ALREADY_ASSIGNED,
                exception.getErrorCode());
    }

    @Test
    void rejectsTargetAlreadyReachedByFinalAccounts() {
        givenParent();
        FinancialGoalUpdateTargetRow goal = goal();
        when(goalMapper.findAccessibleGoalForUpdate(31L, 8L)).thenReturn(goal);
        when(goalMapper.findGoalAccountIds(31L)).thenReturn(List.of(11L));
        when(goalMapper.findAccountTargetsForUpdate(List.of(11L)))
                .thenReturn(List.of(account(11L, "30000000", 31L)));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.update(8L, 31L,
                        new FinancialGoalUpdateRequest(
                                new BigDecimal("30000000"), null, null)));
        assertEquals(ErrorCode.FINANCIAL_GOAL_ALREADY_REACHED,
                exception.getErrorCode());
    }

    private void givenParent() {
        Member parent = Member.createParent("parent@example.com", "부모", null);
        when(memberMapper.findById(8L)).thenReturn(parent);
    }

    private FinancialGoalUpdateTargetRow goal() {
        FinancialGoalUpdateTargetRow row = new FinancialGoalUpdateTargetRow();
        row.setFinancialGoalId(31L);
        row.setChildId(6L);
        row.setFinancialGoalTemplateId(1L);
        row.setTitle("대학자금");
        row.setTargetAmount(new BigDecimal("30000000"));
        row.setTargetDate(LocalDate.of(2045, 3, 31));
        row.setStatus("ACTIVE");
        return row;
    }

    private FinancialGoalAccountTargetRow account(long id, String balance,
                                                   Long goalId) {
        FinancialGoalAccountTargetRow row = new FinancialGoalAccountTargetRow();
        row.setAccountId(id);
        row.setOwnerType("CHILD");
        row.setChildId(6L);
        row.setAccountProductType("SAVINGS");
        row.setAccountStatus("ACTIVE");
        row.setLinkStatus("ACTIVE");
        row.setBalance(new BigDecimal(balance));
        row.setFinancialGoalId(goalId);
        return row;
    }

    private List<FinancialGoalCheckpointRow> checkpoints() {
        return List.of(
                checkpoint(101L, 10, null),
                checkpoint(102L, 25, null),
                checkpoint(103L, 50, null),
                checkpoint(104L, 75, null),
                checkpoint(105L, 100, null));
    }

    private FinancialGoalCheckpointRow checkpoint(long id, int percentage,
                                                   Instant reachedAt) {
        FinancialGoalCheckpointRow row = mock(FinancialGoalCheckpointRow.class);
        when(row.getFinancialGoalCheckpointId()).thenReturn(id);
        when(row.getPercentage()).thenReturn(percentage);
        if (reachedAt != null) {
            when(row.getReachedAt()).thenReturn(reachedAt);
        }
        return row;
    }
}
