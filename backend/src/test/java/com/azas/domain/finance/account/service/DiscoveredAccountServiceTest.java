package com.azas.domain.finance.account.service;

import com.azas.domain.finance.account.dto.DiscoveredAccountListResult;
import com.azas.domain.finance.account.dto.DiscoveredAccountRow;
import com.azas.domain.finance.account.mapper.FinancialAccountMapper;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiscoveredAccountServiceTest {

    private static final long MEMBER_ID = 1L;
    private static final long CHILD_ID = 2L;

    @Mock MemberMapper memberMapper;
    @Mock FinancialAccountMapper financialAccountMapper;
    @Mock AccountNumberProtector accountNumberProtector;

    private DiscoveredAccountService service;

    @BeforeEach
    void setUp() {
        service = new DiscoveredAccountService(
                memberMapper,
                financialAccountMapper,
                accountNumberProtector
        );
    }

    @Test
    void returnsParentDiscoveredAccounts() {
        byte[] ciphertext = {1, 2, 3};
        when(memberMapper.findById(MEMBER_ID)).thenReturn(parent());
        when(financialAccountMapper.findDiscoveredAccounts(
                MEMBER_ID, "PARENT", null
        )).thenReturn(List.of(row(ciphertext)));
        when(accountNumberProtector.decrypt(ciphertext))
                .thenReturn("1234-567-890123");

        DiscoveredAccountListResult result = service
                .getDiscoveredAccounts(MEMBER_ID, "PARENT", null);

        assertEquals(1, result.getAccounts().size());
        assertEquals(
                "1234-567-890123",
                result.getAccounts().get(0).getAccountNumber()
        );
        assertEquals(
                "KB Young Youth 적금",
                result.getAccounts().get(0).getAccountName()
        );
    }

    @Test
    void returnsEmptyListWhenNoCandidateExists() {
        when(memberMapper.findById(MEMBER_ID)).thenReturn(parent());
        when(financialAccountMapper.findDiscoveredAccounts(
                MEMBER_ID, "PARENT", null
        )).thenReturn(List.of());

        assertEquals(
                0,
                service.getDiscoveredAccounts(
                        MEMBER_ID, "PARENT", null
                ).getAccounts().size()
        );
    }

    @Test
    void validatesChildAccessAndParentOnboarding() {
        when(memberMapper.findById(MEMBER_ID)).thenReturn(parent());
        when(financialAccountMapper.countActiveChildById(CHILD_ID))
                .thenReturn(1);
        when(financialAccountMapper.countActiveParentAccess(
                MEMBER_ID, CHILD_ID
        )).thenReturn(1);
        when(financialAccountMapper.countActiveParentDemandDeposit(
                MEMBER_ID
        )).thenReturn(1);
        when(financialAccountMapper.findDiscoveredAccounts(
                MEMBER_ID, "CHILD", CHILD_ID
        )).thenReturn(List.of());

        service.getDiscoveredAccounts(MEMBER_ID, "CHILD", CHILD_ID);

        verify(financialAccountMapper).findDiscoveredAccounts(
                MEMBER_ID, "CHILD", CHILD_ID
        );
    }

    @Test
    void rejectsChildScopeBeforeParentOnboarding() {
        when(memberMapper.findById(MEMBER_ID)).thenReturn(parent());
        when(financialAccountMapper.countActiveChildById(CHILD_ID))
                .thenReturn(1);
        when(financialAccountMapper.countActiveParentAccess(
                MEMBER_ID, CHILD_ID
        )).thenReturn(1);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.getDiscoveredAccounts(
                        MEMBER_ID, "CHILD", CHILD_ID
                )
        );
        assertEquals(
                ErrorCode.PARENT_DEMAND_DEPOSIT_REQUIRED,
                exception.getErrorCode()
        );
    }

    @Test
    void rejectsInvalidOwnerScope() {
        when(memberMapper.findById(MEMBER_ID)).thenReturn(parent());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.getDiscoveredAccounts(
                        MEMBER_ID, "PARENT", CHILD_ID
                )
        );
        assertEquals(
                ErrorCode.INVALID_ACCOUNT_DISCOVERY_REQUEST,
                exception.getErrorCode()
        );
    }

    @Test
    void rejectsChildMember() {
        when(memberMapper.findById(MEMBER_ID)).thenReturn(
                Member.createChild("child@test.com", "child", null)
        );
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.getDiscoveredAccounts(
                        MEMBER_ID, "PARENT", null
                )
        );
        assertEquals(
                ErrorCode.PARENT_ACCESS_REQUIRED,
                exception.getErrorCode()
        );
    }

    private Member parent() {
        return Member.createParent("parent@test.com", "parent", null);
    }

    private DiscoveredAccountRow row(byte[] ciphertext) {
        DiscoveredAccountRow row = new DiscoveredAccountRow();
        ReflectionTestUtils.setField(row, "accountId", 101L);
        ReflectionTestUtils.setField(row, "bankName", "KB국민은행");
        ReflectionTestUtils.setField(
                row, "accountName", "KB Young Youth 적금"
        );
        ReflectionTestUtils.setField(
                row, "accountNumberCiphertext", ciphertext
        );
        ReflectionTestUtils.setField(
                row, "accountProductType", "DEMAND_DEPOSIT"
        );
        ReflectionTestUtils.setField(
                row, "balance", new BigDecimal("12450000")
        );
        return row;
    }
}
