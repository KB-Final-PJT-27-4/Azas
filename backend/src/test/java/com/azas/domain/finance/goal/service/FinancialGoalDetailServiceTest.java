package com.azas.domain.finance.goal.service;

import com.azas.domain.finance.goal.dto.FinancialGoalCheckpointRow;
import com.azas.domain.finance.goal.dto.FinancialGoalDetailRow;
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
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinancialGoalDetailServiceTest {

    private static final long PARENT_ID = 8L;
    private static final long GOAL_ID = 31L;
    private static final long CHILD_ID = 6L;

    @Mock
    private MemberMapper memberMapper;

    @Mock
    private FinancialGoalMapper financialGoalMapper;

    @Mock
    private AccountNumberProtector accountNumberProtector;

    private FinancialGoalDetailService service;

    @BeforeEach
    void setUp() {
        service = new FinancialGoalDetailService(
                memberMapper,
                financialGoalMapper,
                accountNumberProtector
        );
    }

    @Test
    void returnsGoalWithMultipleAccountsAndCheckpoints() {
        byte[] firstCiphertext = new byte[]{1};
        byte[] secondCiphertext = new byte[]{2};
        allowParent();
        when(financialGoalMapper.findAccessibleGoalDetailWithAccounts(
                GOAL_ID, PARENT_ID
        )).thenReturn(List.of(
                goalRow(11L, firstCiphertext, "4800000.00"),
                goalRow(12L, secondCiphertext, "4800000.00")
        ));
        when(financialGoalMapper.findGoalCheckpoints(GOAL_ID))
                .thenReturn(List.of(
                        checkpointRow(101L, 10, "3000000.00",
                                Instant.parse("2027-01-15T09:00:00Z")),
                        checkpointRow(102L, 25, "7500000.00", null)
                ));
        when(accountNumberProtector.decrypt(firstCiphertext))
                .thenReturn("952-17362605-43");
        when(accountNumberProtector.decrypt(secondCiphertext))
                .thenReturn("952-17362605-44");

        var result = service.getGoal(PARENT_ID, GOAL_ID);

        assertEquals(GOAL_ID, result.getFinancialGoalId());
        assertEquals(CHILD_ID, result.getChildId());
        assertEquals(new BigDecimal("9600000.00"),
                result.getCurrentAmount());
        assertEquals(new BigDecimal("20400000.00"),
                result.getRemainingAmount());
        assertEquals(new BigDecimal("32.0"), result.getAchievementRate());
        assertEquals("ACTIVE", result.getStatus());
        assertEquals(2, result.getLinkedAccounts().size());
        assertEquals("952-17362605-43",
                result.getLinkedAccounts().get(0).getAccountNumber());
        assertEquals(2, result.getCheckpoints().size());
        assertTrue(result.getCheckpoints().get(0).isReached());
        assertFalse(result.getCheckpoints().get(1).isReached());
        assertNull(result.getCheckpoints().get(1).getReachedAt());
    }

    @Test
    void returnsDirectGoalWithoutTemplateMetadata() {
        byte[] ciphertext = new byte[]{1};
        allowParent();
        FinancialGoalDetailRow row = goalRow(
                11L, ciphertext, "4800000.00"
        );
        ReflectionTestUtils.setField(row, "financialGoalTemplateId", null);
        ReflectionTestUtils.setField(row, "iconKey", null);
        when(financialGoalMapper.findAccessibleGoalDetailWithAccounts(
                GOAL_ID, PARENT_ID
        )).thenReturn(List.of(row));
        when(financialGoalMapper.findGoalCheckpoints(GOAL_ID))
                .thenReturn(List.of());
        when(accountNumberProtector.decrypt(ciphertext))
                .thenReturn("952-17362605-43");

        var result = service.getGoal(PARENT_ID, GOAL_ID);

        assertNull(result.getFinancialGoalTemplateId());
        assertNull(result.getIconKey());
    }

    @Test
    void capsRateAndCalculatesAchievedStatusWithoutMutation() {
        byte[] ciphertext = new byte[]{1};
        allowParent();
        FinancialGoalDetailRow row = goalRow(
                11L, ciphertext, "31000000.00"
        );
        when(financialGoalMapper.findAccessibleGoalDetailWithAccounts(
                GOAL_ID, PARENT_ID
        )).thenReturn(List.of(row));
        when(financialGoalMapper.findGoalCheckpoints(GOAL_ID))
                .thenReturn(List.of());
        when(accountNumberProtector.decrypt(ciphertext))
                .thenReturn("952-17362605-43");

        var result = service.getGoal(PARENT_ID, GOAL_ID);

        assertEquals(BigDecimal.ZERO, result.getRemainingAmount());
        assertEquals(new BigDecimal("100.0"), result.getAchievementRate());
        assertEquals("ACHIEVED", result.getStatus());
    }

    @Test
    void rejectsInvalidGoalIdBeforeGoalLookup() {
        allowParent();

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.getGoal(PARENT_ID, 0L)
        );

        assertEquals(ErrorCode.BADREQUEST, exception.getErrorCode());
        verify(financialGoalMapper, never())
                .findAccessibleGoalDetailWithAccounts(0L, PARENT_ID);
    }

    @Test
    void rejectsChildMember() {
        when(memberMapper.findById(PARENT_ID)).thenReturn(
                Member.createChild("child@example.com", "child", null)
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.getGoal(PARENT_ID, GOAL_ID)
        );

        assertEquals(ErrorCode.PARENT_ACCESS_REQUIRED,
                exception.getErrorCode());
    }

    @Test
    void hidesMissingAndInaccessibleGoalAsNotFound() {
        allowParent();
        when(financialGoalMapper.findAccessibleGoalDetailWithAccounts(
                GOAL_ID, PARENT_ID
        )).thenReturn(List.of());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.getGoal(PARENT_ID, GOAL_ID)
        );

        assertEquals(ErrorCode.FINANCIAL_GOAL_NOT_FOUND,
                exception.getErrorCode());
        verify(financialGoalMapper, never()).findGoalCheckpoints(GOAL_ID);
    }

    @Test
    void returnsServerErrorWhenAccountNumberCannotBeDecrypted() {
        byte[] ciphertext = new byte[]{1};
        allowParent();
        when(financialGoalMapper.findAccessibleGoalDetailWithAccounts(
                GOAL_ID, PARENT_ID
        )).thenReturn(List.of(
                goalRow(11L, ciphertext, "4800000.00")
        ));
        when(accountNumberProtector.decrypt(ciphertext))
                .thenThrow(new IllegalArgumentException());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.getGoal(PARENT_ID, GOAL_ID)
        );

        assertEquals(ErrorCode.INTERNAL_SERVER_ERROR,
                exception.getErrorCode());
        verify(financialGoalMapper, never()).findGoalCheckpoints(GOAL_ID);
    }

    private void allowParent() {
        when(memberMapper.findById(PARENT_ID)).thenReturn(
                Member.createParent("parent@example.com", "parent", null)
        );
    }

    private FinancialGoalDetailRow goalRow(
            long accountId,
            byte[] ciphertext,
            String balance
    ) {
        FinancialGoalDetailRow row = new FinancialGoalDetailRow();
        ReflectionTestUtils.setField(row, "financialGoalId", GOAL_ID);
        ReflectionTestUtils.setField(row, "childId", CHILD_ID);
        ReflectionTestUtils.setField(row, "financialGoalTemplateId", 1L);
        ReflectionTestUtils.setField(row, "title", "대학자금");
        ReflectionTestUtils.setField(row, "iconKey", "goal-university");
        ReflectionTestUtils.setField(row, "targetAmount",
                new BigDecimal("30000000.00"));
        ReflectionTestUtils.setField(row, "targetDate",
                LocalDate.of(2045, 3, 31));
        ReflectionTestUtils.setField(row, "monthlySavingAmount",
                new BigDecimal("125000.00"));
        ReflectionTestUtils.setField(row, "status", "ACTIVE");
        ReflectionTestUtils.setField(row, "accountId", accountId);
        ReflectionTestUtils.setField(row, "accountName", "아이사랑적금");
        ReflectionTestUtils.setField(row, "bankName", "KB국민은행");
        ReflectionTestUtils.setField(row, "accountNumberCiphertext",
                ciphertext);
        ReflectionTestUtils.setField(row, "balance", new BigDecimal(balance));
        return row;
    }

    private FinancialGoalCheckpointRow checkpointRow(
            long checkpointId,
            int percentage,
            String targetAmount,
            Instant reachedAt
    ) {
        FinancialGoalCheckpointRow row = new FinancialGoalCheckpointRow();
        ReflectionTestUtils.setField(row, "financialGoalCheckpointId",
                checkpointId);
        ReflectionTestUtils.setField(row, "percentage", percentage);
        ReflectionTestUtils.setField(row, "targetAmount",
                new BigDecimal(targetAmount));
        ReflectionTestUtils.setField(row, "reachedAt", reachedAt);
        return row;
    }
}
