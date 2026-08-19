package com.azas.domain.finance.goal.service;

import com.azas.domain.finance.account.mapper.FinancialAccountMapper;
import com.azas.domain.finance.goal.dto.*;
import com.azas.domain.finance.goal.entity.FinancialGoalTemplate;
import com.azas.domain.finance.goal.mapper.FinancialGoalMapper;
import com.azas.domain.finance.goal.mapper.FinancialGoalTemplateMapper;
import com.azas.domain.member.entity.*;
import com.azas.domain.member.mapper.MemberMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinancialGoalCreateServiceTest {
    private static final Instant NOW = Instant.parse("2026-08-18T01:30:00Z");

    @Mock MemberMapper memberMapper;
    @Mock FinancialAccountMapper accountMapper;
    @Mock FinancialGoalTemplateMapper templateMapper;
    @Mock FinancialGoalMapper goalMapper;
    private FinancialGoalCreateService service;

    @BeforeEach
    void setUp() {
        service = new FinancialGoalCreateService(memberMapper, accountMapper,
                templateMapper, goalMapper, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void createsTemplateGoalWithMultipleSavingsAccounts() {
        givenAccess();
        FinancialGoalTemplate template = new FinancialGoalTemplate();
        template.setFinancialGoalTemplateId(1L);
        template.setGoalName("대학자금");
        when(templateMapper.findActiveDefaultTemplateById(1L)).thenReturn(template);
        when(goalMapper.findAccountTargetsForUpdate(List.of(11L, 12L)))
                .thenReturn(List.of(
                        account(12L, 6L, "적금2", "4800000", null),
                        account(11L, 6L, "적금1", "4800000", null)));
        generateGoalId(31L);
        when(goalMapper.insertFinancialGoalAccount(anyLong(), anyLong())).thenReturn(1);
        when(goalMapper.insertFinancialGoalCheckpoint(any())).thenReturn(1);

        FinancialGoalCreateResult result = service.create(8L, 6L,
                command(1L, null, "30000000", List.of(11L, 12L)));

        assertEquals(31L, result.getFinancialGoalId());
        assertEquals("대학자금", result.getTitle());
        assertEquals(new BigDecimal("9600000"), result.getCurrentAmount());
        assertEquals(new BigDecimal("20400000"), result.getRemainingAmount());
        assertEquals(new BigDecimal("32.0"), result.getAchievementRate());
        assertEquals(11L, result.getLinkedAccounts().get(0).getAccountId());
        verify(goalMapper).insertFinancialGoalAccount(31L, 11L);
        verify(goalMapper).insertFinancialGoalAccount(31L, 12L);
        verify(goalMapper, times(5)).insertFinancialGoalCheckpoint(
                any(FinancialGoalCheckpointInsertCommand.class));
    }

    @Test
    void createsCustomGoalWithTrimmedTitle() {
        givenAccess();
        when(goalMapper.findAccountTargetsForUpdate(List.of(13L)))
                .thenReturn(List.of(account(13L, 6L, "적금", "100000", null)));
        generateGoalId(32L);
        when(goalMapper.insertFinancialGoalAccount(32L, 13L)).thenReturn(1);
        when(goalMapper.insertFinancialGoalCheckpoint(any())).thenReturn(1);

        FinancialGoalCreateResult result = service.create(8L, 6L,
                command(null, "  유학자금  ", "50000000", List.of(13L)));

        assertEquals("유학자금", result.getTitle());
        assertNull(result.getFinancialGoalTemplateId());
        verify(templateMapper, never()).findActiveDefaultTemplateById(anyLong());
    }

    @Test
    void rejectsDuplicateAccountIds() {
        givenAccess();
        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.create(8L, 6L,
                        command(1L, null, "30000000", List.of(11L, 11L))));
        assertEquals(ErrorCode.INVALID_FINANCIAL_GOAL_REQUEST,
                exception.getErrorCode());
        verify(goalMapper, never()).findAccountTargetsForUpdate(any());
    }

    @Test
    void rejectsMissingTemplate() {
        givenAccess();
        when(templateMapper.findActiveDefaultTemplateById(99L)).thenReturn(null);
        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.create(8L, 6L,
                        command(99L, null, "30000000", List.of(11L))));
        assertEquals(ErrorCode.FINANCIAL_GOAL_TEMPLATE_NOT_FOUND,
                exception.getErrorCode());
    }

    @Test
    void rejectsAlreadyAssignedAccount() {
        givenAccess();
        template();
        when(goalMapper.findAccountTargetsForUpdate(List.of(11L)))
                .thenReturn(List.of(account(11L, 6L, "적금", "100000", 7L)));
        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.create(8L, 6L,
                        command(1L, null, "30000000", List.of(11L))));
        assertEquals(ErrorCode.FINANCIAL_ACCOUNT_GOAL_ALREADY_ASSIGNED,
                exception.getErrorCode());
    }

    @Test
    void rejectsAlreadyReachedGoal() {
        givenAccess();
        template();
        when(goalMapper.findAccountTargetsForUpdate(List.of(11L)))
                .thenReturn(List.of(account(11L, 6L, "적금", "30000000", null)));
        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.create(8L, 6L,
                        command(1L, null, "30000000", List.of(11L))));
        assertEquals(ErrorCode.FINANCIAL_GOAL_ALREADY_REACHED,
                exception.getErrorCode());
    }

    private void givenAccess() {
        Member parent = Member.createParent(
                "parent@example.com", "부모", null);
        when(memberMapper.findById(8L)).thenReturn(parent);
        when(accountMapper.countActiveChildById(6L)).thenReturn(1);
        when(accountMapper.countActiveParentAccess(8L, 6L)).thenReturn(1);
    }

    private void template() {
        FinancialGoalTemplate template = new FinancialGoalTemplate();
        template.setGoalName("대학자금");
        when(templateMapper.findActiveDefaultTemplateById(1L)).thenReturn(template);
    }

    private void generateGoalId(long id) {
        doAnswer(invocation -> {
            FinancialGoalInsertCommand command = invocation.getArgument(0);
            command.setFinancialGoalId(id);
            return 1;
        }).when(goalMapper).insertFinancialGoal(any());
    }

    private FinancialGoalCreateCommand command(Long templateId, String title,
                                                String targetAmount,
                                                List<Long> accountIds) {
        return new FinancialGoalCreateCommand(templateId, title,
                new BigDecimal(targetAmount), LocalDate.of(2045, 3, 31), accountIds);
    }

    private FinancialGoalAccountTargetRow account(long id, long childId,
                                                   String name, String balance,
                                                   Long goalId) {
        FinancialGoalAccountTargetRow row = new FinancialGoalAccountTargetRow();
        row.setAccountId(id);
        row.setOwnerType("CHILD");
        row.setChildId(childId);
        row.setBankName("KB국민은행");
        row.setAccountName(name);
        row.setAccountProductType("SAVINGS");
        row.setBalance(new BigDecimal(balance));
        row.setAccountStatus("ACTIVE");
        row.setLinkStatus("ACTIVE");
        row.setFinancialGoalId(goalId);
        return row;
    }
}
