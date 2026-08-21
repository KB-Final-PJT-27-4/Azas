package com.azas.domain.finance.goal.service;

import com.azas.domain.finance.account.mapper.FinancialAccountMapper;
import com.azas.domain.finance.goal.dto.FinancialGoalListResult;
import com.azas.domain.finance.goal.dto.FinancialGoalListRow;
import com.azas.domain.finance.goal.mapper.FinancialGoalMapper;
import com.azas.domain.member.entity.Member;
import com.azas.domain.member.mapper.MemberMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import com.azas.global.security.AccountNumberProtector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinancialGoalListServiceTest {

    private static final long PARENT_ID = 8L;
    private static final long CHILD_ID = 6L;

    @Mock
    private MemberMapper memberMapper;

    @Mock
    private FinancialAccountMapper financialAccountMapper;

    @Mock
    private FinancialGoalMapper financialGoalMapper;

    @Mock
    private AccountNumberProtector accountNumberProtector;

    private FinancialGoalListService service;

    @BeforeEach
    void setUp() {
        service = new FinancialGoalListService(
                memberMapper,
                financialAccountMapper,
                financialGoalMapper,
                accountNumberProtector
        );
    }

    @Test
    void returnsGoalsWithMultipleAccountsAndCalculatedAmounts() {
        byte[] firstCiphertext = new byte[]{1, 2, 3};
        byte[] secondCiphertext = new byte[]{4, 5, 6};
        allowParentAccess();
        when(financialGoalMapper.findActiveAndAchievedGoalsWithAccounts(CHILD_ID))
                .thenReturn(List.of(
                        row(31L, 1L, "대학자금", "goal-university",
                                "30000000.00", "ACTIVE", 11L,
                                firstCiphertext, "4800000.00"),
                        row(31L, 1L, "대학자금", "goal-university",
                                "30000000.00", "ACTIVE", 12L,
                                secondCiphertext, "4800000.00")
                ));
        when(accountNumberProtector.decrypt(firstCiphertext))
                .thenReturn("952-17362605-43");
        when(accountNumberProtector.decrypt(secondCiphertext))
                .thenReturn("952-17362605-44");

        FinancialGoalListResult result = service.getGoals(PARENT_ID, CHILD_ID);

        assertEquals(CHILD_ID, result.getChildId());
        assertEquals(1, result.getFinancialGoals().size());
        var goal = result.getFinancialGoals().get(0);
        assertEquals(new BigDecimal("9600000.00"), goal.getCurrentAmount());
        assertEquals(new BigDecimal("20400000.00"), goal.getRemainingAmount());
        assertEquals(new BigDecimal("32.0"), goal.getAchievementRate());
        assertEquals("ACTIVE", goal.getStatus());
        assertEquals(2, goal.getLinkedAccounts().size());
        assertEquals("952-17362605-43",
                goal.getLinkedAccounts().get(0).getAccountNumber());
    }

    @Test
    void preservesMapperGoalOrderAndDirectGoalHasNoTemplateMetadata() {
        byte[] newestCiphertext = new byte[]{1};
        byte[] olderCiphertext = new byte[]{2};
        allowParentAccess();
        when(financialGoalMapper.findActiveAndAchievedGoalsWithAccounts(CHILD_ID))
                .thenReturn(List.of(
                        row(40L, null, "유학자금", null,
                                "10000000.00", "ACTIVE", 20L,
                                newestCiphertext, "1000000.00"),
                        row(31L, 1L, "대학자금", "goal-university",
                                "30000000.00", "ACTIVE", 11L,
                                olderCiphertext, "4800000.00")
                ));
        when(accountNumberProtector.decrypt(newestCiphertext))
                .thenReturn("111-2222-3333");
        when(accountNumberProtector.decrypt(olderCiphertext))
                .thenReturn("444-5555-6666");

        FinancialGoalListResult result = service.getGoals(PARENT_ID, CHILD_ID);

        assertEquals(List.of(40L, 31L), result.getFinancialGoals().stream()
                .map(goal -> goal.getFinancialGoalId())
                .toList());
        assertNull(result.getFinancialGoals().get(0)
                .getFinancialGoalTemplateId());
        assertNull(result.getFinancialGoals().get(0).getIconKey());
    }

    @Test
    void capsAchievementRateAndReturnsCalculatedAchievedStatus() {
        byte[] ciphertext = new byte[]{1};
        allowParentAccess();
        when(financialGoalMapper.findActiveAndAchievedGoalsWithAccounts(CHILD_ID))
                .thenReturn(List.of(
                        row(31L, 1L, "대학자금", "goal-university",
                                "1000000.00", "ACTIVE", 11L,
                                ciphertext, "1250000.00")
                ));
        when(accountNumberProtector.decrypt(ciphertext))
                .thenReturn("952-17362605-43");

        var goal = service.getGoals(PARENT_ID, CHILD_ID)
                .getFinancialGoals().get(0);

        assertEquals(BigDecimal.ZERO, goal.getRemainingAmount());
        assertEquals(new BigDecimal("100.0"), goal.getAchievementRate());
        assertEquals("ACHIEVED", goal.getStatus());
    }

    @Test
    void returnsEmptyListWhenChildHasNoGoal() {
        allowParentAccess();
        when(financialGoalMapper.findActiveAndAchievedGoalsWithAccounts(CHILD_ID))
                .thenReturn(List.of());

        FinancialGoalListResult result = service.getGoals(PARENT_ID, CHILD_ID);

        assertEquals(List.of(), result.getFinancialGoals());
    }

    @Test
    void rejectsChildMember() {
        when(memberMapper.findById(PARENT_ID)).thenReturn(
                Member.createChild("child@example.com", "child", null)
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.getGoals(PARENT_ID, CHILD_ID)
        );

        assertEquals(ErrorCode.PARENT_ACCESS_REQUIRED,
                exception.getErrorCode());
        verify(financialGoalMapper, never())
                .findActiveAndAchievedGoalsWithAccounts(CHILD_ID);
    }

    @Test
    void rejectsInaccessibleChild() {
        when(memberMapper.findById(PARENT_ID)).thenReturn(parentMember());
        when(financialAccountMapper.countActiveChildById(CHILD_ID))
                .thenReturn(1);
        when(financialAccountMapper.countActiveParentAccess(PARENT_ID, CHILD_ID))
                .thenReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.getGoals(PARENT_ID, CHILD_ID)
        );

        assertEquals(ErrorCode.CHILD_ACCESS_DENIED,
                exception.getErrorCode());
    }

    @Test
    void rejectsInvalidChildIdBeforeDatabaseLookup() {
        when(memberMapper.findById(PARENT_ID)).thenReturn(parentMember());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.getGoals(PARENT_ID, 0L)
        );

        assertEquals(ErrorCode.BADREQUEST, exception.getErrorCode());
        verify(financialAccountMapper, never()).countActiveChildById(0L);
    }

    @Test
    void hidesCiphertextWhenAccountNumberDecryptionFails() {
        byte[] ciphertext = new byte[]{1, 2, 3};
        allowParentAccess();
        when(financialGoalMapper.findActiveAndAchievedGoalsWithAccounts(CHILD_ID))
                .thenReturn(List.of(
                        row(31L, 1L, "대학자금", "goal-university",
                                "30000000.00", "ACTIVE", 11L,
                                ciphertext, "4800000.00")
                ));
        when(accountNumberProtector.decrypt(ciphertext))
                .thenThrow(new IllegalArgumentException());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.getGoals(PARENT_ID, CHILD_ID)
        );

        assertEquals(ErrorCode.INTERNAL_SERVER_ERROR,
                exception.getErrorCode());
    }

    private void allowParentAccess() {
        when(memberMapper.findById(PARENT_ID)).thenReturn(parentMember());
        when(financialAccountMapper.countActiveChildById(CHILD_ID))
                .thenReturn(1);
        when(financialAccountMapper.countActiveParentAccess(PARENT_ID, CHILD_ID))
                .thenReturn(1);
    }

    private Member parentMember() {
        return Member.createParent("parent@example.com", "parent", null);
    }

    private FinancialGoalListRow row(
            long goalId,
            Long templateId,
            String title,
            String iconKey,
            String targetAmount,
            String status,
            long accountId,
            byte[] ciphertext,
            String balance
    ) {
        FinancialGoalListRow row = new FinancialGoalListRow();
        ReflectionTestUtils.setField(row, "financialGoalId", goalId);
        ReflectionTestUtils.setField(row, "childId", CHILD_ID);
        ReflectionTestUtils.setField(row, "financialGoalTemplateId", templateId);
        ReflectionTestUtils.setField(row, "title", title);
        ReflectionTestUtils.setField(row, "iconKey", iconKey);
        ReflectionTestUtils.setField(row, "targetAmount", new BigDecimal(targetAmount));
        ReflectionTestUtils.setField(row, "targetDate", LocalDate.of(2045, 3, 31));
        ReflectionTestUtils.setField(row, "status", status);
        ReflectionTestUtils.setField(row, "createdAt",
                LocalDateTime.of(2026, 8, 18, 3, 0));
        ReflectionTestUtils.setField(row, "accountId", accountId);
        ReflectionTestUtils.setField(row, "accountName", "KB 아이사랑적금");
        ReflectionTestUtils.setField(row, "bankName", "KB국민은행");
        ReflectionTestUtils.setField(row, "accountNumberCiphertext", ciphertext);
        ReflectionTestUtils.setField(row, "balance", new BigDecimal(balance));
        return row;
    }
}
