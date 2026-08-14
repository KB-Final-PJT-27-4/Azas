package com.azas.domain.finance.account.service;

import com.azas.domain.finance.account.dto.*;
import com.azas.domain.finance.account.mapper.FinancialAccountMapper;
import com.azas.domain.finance.product.entity.FinancialProduct;
import com.azas.domain.finance.product.mapper.FinancialProductMapper;
import com.azas.domain.member.entity.Member;
import com.azas.domain.member.mapper.MemberMapper;
import com.azas.global.exception.*;
import com.azas.global.security.AccountNumberProtector;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountOpenServiceTest {
    @Mock MemberMapper memberMapper;
    @Mock FinancialAccountMapper accountMapper;
    @Mock FinancialProductMapper productMapper;
    @Mock AccountNumberProtector protector;
    @Mock MockAccountNumberGenerator generator;
    private AccountOpenService service;

    @BeforeEach void setUp() {
        service = new AccountOpenService(memberMapper, accountMapper,
                productMapper, protector, generator,
                Clock.fixed(Instant.parse("2026-08-13T05:00:00Z"), ZoneOffset.UTC));
        when(memberMapper.findById(1L)).thenReturn(
                Member.createParent("p@test.com", "p", null));
    }

    @Test void opensFirstParentDemandAsPrimary() {
        AccountOpenRequest request = request("PARENT", null, 2L, null);
        when(productMapper.findActiveProductById(2L)).thenReturn(product(2L, "ACCOUNT", null));
        prepareNumber();
        doAnswer(invocation -> {
            AccountOpenRecord record = invocation.getArgument(0);
            record.setAccountId(10L);
            return 1;
        }).when(accountMapper).insertOpenedAccount(any());

        AccountOpenResult result = service.open(1L, request);
        assertTrue(result.isPrimary());
        assertEquals("DEMAND_DEPOSIT", result.getAccountProductType());
        assertEquals("KB국민 입출금통장", result.getAccountName());
        assertNull(result.getFinancialGoal());

        var captor = org.mockito.ArgumentCaptor.forClass(AccountOpenRecord.class);
        verify(accountMapper).insertOpenedAccount(captor.capture());
        assertEquals("KB국민 입출금통장", captor.getValue().getAccountName());
    }

    @Test void opensChildDemandWithYoungYouthAccountName() {
        AccountOpenRequest request = request("CHILD", 6L, 2L, null);
        when(accountMapper.countActiveChildById(6L)).thenReturn(1);
        when(accountMapper.countActiveParentAccess(1L, 6L)).thenReturn(1);
        when(accountMapper.countActiveParentDemandDeposit(1L)).thenReturn(1);
        when(accountMapper.findActiveChildMemberIdByChildId(6L)).thenReturn(9L);
        when(productMapper.findActiveProductById(2L)).thenReturn(product(2L, "ACCOUNT", null));
        prepareNumber();
        doAnswer(invocation -> {
            AccountOpenRecord record = invocation.getArgument(0);
            record.setAccountId(11L);
            return 1;
        }).when(accountMapper).insertOpenedAccount(any());

        AccountOpenResult result = service.open(1L, request);

        assertEquals("KB Young Youth 입출금통장", result.getAccountName());
        var captor = org.mockito.ArgumentCaptor.forClass(AccountOpenRecord.class);
        verify(accountMapper).insertOpenedAccount(captor.capture());
        assertEquals("KB Young Youth 입출금통장", captor.getValue().getAccountName());
    }

    @Test void opensChildSavingsAndGoalInOneTransaction() {
        AccountOpenGoalRequest goal = goal();
        AccountOpenRequest request = request("CHILD", 6L, 1L, goal);
        when(accountMapper.countActiveChildById(6L)).thenReturn(1);
        when(accountMapper.countActiveParentAccess(1L, 6L)).thenReturn(1);
        when(accountMapper.countActiveParentDemandDeposit(1L)).thenReturn(1);
        when(accountMapper.findActiveChildMemberIdByChildId(6L)).thenReturn(9L);
        when(accountMapper.countActiveFinancialGoalTemplate(1L)).thenReturn(1);
        when(productMapper.findActiveProductById(1L)).thenReturn(product(1L, "SAVING", 12));
        prepareNumber();
        doAnswer(invocation -> {
            AccountOpenRecord record = invocation.getArgument(0);
            record.setAccountId(20L);
            return 1;
        }).when(accountMapper).insertOpenedAccount(any());
        doAnswer(invocation -> {
            FinancialGoalOpenRecord record = invocation.getArgument(0);
            record.setFinancialGoalId(31L);
            return 1;
        }).when(accountMapper).insertFinancialGoal(any());
        when(accountMapper.insertFinancialGoalCheckpoints(
                31L, new BigDecimal("3000000"))).thenReturn(5);

        AccountOpenResult result = service.open(1L, request);
        assertEquals(31L, result.getFinancialGoal().getFinancialGoalId());
        assertEquals("KB Mock 상품", result.getAccountName());
        verify(accountMapper).insertFinancialGoalCheckpoints(
                31L, new BigDecimal("3000000"));
    }

    @Test void rejectsChildSavingsWithoutGoal() {
        AccountOpenRequest request = request("CHILD", 6L, 1L, null);
        when(accountMapper.countActiveChildById(6L)).thenReturn(1);
        when(accountMapper.countActiveParentAccess(1L, 6L)).thenReturn(1);
        when(productMapper.findActiveProductById(1L)).thenReturn(product(1L, "SAVING", 12));
        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.open(1L, request));
        assertEquals(ErrorCode.SAVINGS_GOAL_REQUIRED, exception.getErrorCode());
    }

    @Test void rejectsChildAccountBeforeParentOnboarding() {
        AccountOpenRequest request = request("CHILD", 6L, 2L, null);
        when(accountMapper.countActiveChildById(6L)).thenReturn(1);
        when(accountMapper.countActiveParentAccess(1L, 6L)).thenReturn(1);
        when(productMapper.findActiveProductById(2L)).thenReturn(product(2L, "ACCOUNT", null));
        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.open(1L, request));
        assertEquals(ErrorCode.PARENT_DEMAND_DEPOSIT_REQUIRED, exception.getErrorCode());
    }

    private void prepareNumber() {
        when(generator.generate()).thenReturn("123-456-789012");
        when(accountMapper.countAccountNumberHash(any())).thenReturn(0);
        when(protector.encrypt("123-456-789012")).thenReturn(new byte[]{1});
    }
    private FinancialProduct product(long id, String type, Integer months) {
        FinancialProduct product = new FinancialProduct();
        product.setFinancialProductId(id); product.setProductType(type);
        product.setBankName("KB국민은행"); product.setName("KB Mock 상품");
        product.setContractPeriodMonths(months); return product;
    }
    private AccountOpenRequest request(String type, Long childId, Long productId,
                                       AccountOpenGoalRequest goal) {
        AccountOpenRequest request = new AccountOpenRequest();
        ReflectionTestUtils.setField(request, "ownerType", type);
        ReflectionTestUtils.setField(request, "childId", childId);
        ReflectionTestUtils.setField(request, "financialProductId", productId);
        ReflectionTestUtils.setField(request, "initialDepositAmount", BigDecimal.ZERO);
        ReflectionTestUtils.setField(request, "goal", goal); return request;
    }
    private AccountOpenGoalRequest goal() {
        AccountOpenGoalRequest goal = new AccountOpenGoalRequest();
        ReflectionTestUtils.setField(goal, "financialGoalTemplateId", 1L);
        ReflectionTestUtils.setField(goal, "title", "입학 준비금");
        ReflectionTestUtils.setField(goal, "targetAmount", new BigDecimal("3000000"));
        ReflectionTestUtils.setField(goal, "targetDate", LocalDate.of(2029, 2, 28));
        ReflectionTestUtils.setField(goal, "monthlySavingAmount", new BigDecimal("80000"));
        return goal;
    }
}
