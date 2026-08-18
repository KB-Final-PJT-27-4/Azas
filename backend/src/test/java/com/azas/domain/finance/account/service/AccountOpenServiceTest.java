package com.azas.domain.finance.account.service;

import com.azas.domain.finance.account.dto.AccountOpenRecord;
import com.azas.domain.finance.account.dto.AccountOpenRequest;
import com.azas.domain.finance.account.dto.AccountOpenResult;
import com.azas.domain.finance.account.mapper.FinancialAccountMapper;
import com.azas.domain.finance.product.entity.FinancialProduct;
import com.azas.domain.finance.product.mapper.FinancialProductMapper;
import com.azas.domain.member.entity.Member;
import com.azas.domain.member.mapper.MemberMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import com.azas.global.security.AccountNumberProtector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountOpenServiceTest {

    @Mock
    MemberMapper memberMapper;
    @Mock
    FinancialAccountMapper accountMapper;
    @Mock
    FinancialProductMapper productMapper;
    @Mock
    AccountNumberProtector protector;
    @Mock
    MockAccountNumberGenerator generator;

    private AccountOpenService service;

    @BeforeEach
    void setUp() {
        service = new AccountOpenService(
                memberMapper,
                accountMapper,
                productMapper,
                protector,
                generator,
                Clock.fixed(
                        Instant.parse("2026-08-13T05:00:00Z"),
                        ZoneOffset.UTC
                )
        );
        when(memberMapper.findById(1L)).thenReturn(
                Member.createParent("p@test.com", "p", null)
        );
    }

    @Test
    void opensFirstParentDemandAsPrimary() {
        AccountOpenRequest request = request("PARENT", null, 2L);
        when(productMapper.findActiveProductById(2L))
                .thenReturn(product(2L, "ACCOUNT", null));
        prepareNumber();
        assignAccountId(10L);

        AccountOpenResult result = service.open(1L, request);

        assertTrue(result.isPrimary());
        assertEquals("DEMAND_DEPOSIT", result.getAccountProductType());
        assertEquals("KB국민 입출금통장", result.getAccountName());

        ArgumentCaptor<AccountOpenRecord> captor =
                ArgumentCaptor.forClass(AccountOpenRecord.class);
        verify(accountMapper).insertOpenedAccount(captor.capture());
        assertEquals("KB국민 입출금통장", captor.getValue().getAccountName());
    }

    @Test
    void opensChildDemandWithYoungYouthAccountName() {
        AccountOpenRequest request = request("CHILD", 6L, 2L);
        prepareChildScope();
        when(productMapper.findActiveProductById(2L))
                .thenReturn(product(2L, "ACCOUNT", null));
        prepareNumber();
        assignAccountId(11L);

        AccountOpenResult result = service.open(1L, request);

        assertEquals("KB Young Youth 입출금통장", result.getAccountName());

        ArgumentCaptor<AccountOpenRecord> captor =
                ArgumentCaptor.forClass(AccountOpenRecord.class);
        verify(accountMapper).insertOpenedAccount(captor.capture());
        assertEquals(
                "KB Young Youth 입출금통장",
                captor.getValue().getAccountName()
        );
    }

    @Test
    void opensChildSavingsWithoutEmbeddedGoal() {
        AccountOpenRequest request = request("CHILD", 6L, 1L);
        prepareChildScope();
        when(productMapper.findActiveProductById(1L))
                .thenReturn(product(1L, "SAVING", 12));
        prepareNumber();
        assignAccountId(20L);

        AccountOpenResult result = service.open(1L, request);

        assertEquals(20L, result.getAccountId());
        assertEquals("CHILD", result.getOwnerType());
        assertEquals(6L, result.getChildId());
        assertEquals("SAVINGS", result.getAccountProductType());
        assertEquals("KB Mock 상품", result.getAccountName());
        verify(accountMapper).insertOpenedAccount(any());
    }

    @Test
    void opensParentSavingsForParentTargetProduct() {
        AccountOpenRequest request = request("PARENT", null, 3L);
        when(accountMapper.countActiveParentDemandDeposit(1L)).thenReturn(1);
        FinancialProduct product = product(3L, "SAVING", 12);
        product.setTargetOwnerType("PARENT");
        product.setName("KB스타적금");
        when(productMapper.findActiveProductById(3L)).thenReturn(product);
        prepareNumber();
        assignAccountId(30L);

        AccountOpenResult result = service.open(1L, request);

        assertEquals("PARENT", result.getOwnerType());
        assertEquals("SAVINGS", result.getAccountProductType());
        assertEquals("KB스타적금", result.getAccountName());
    }

    @Test
    void rejectsParentOpeningChildTargetProduct() {
        AccountOpenRequest request = request("PARENT", null, 1L);
        FinancialProduct product = product(1L, "SAVING", 12);
        product.setTargetOwnerType("CHILD");
        when(productMapper.findActiveProductById(1L)).thenReturn(product);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.open(1L, request)
        );

        assertEquals(
                ErrorCode.INVALID_ACCOUNT_OPEN_REQUEST,
                exception.getErrorCode()
        );
        verify(accountMapper, never()).insertOpenedAccount(any());
    }

    @Test
    void rejectsChildOpeningParentTargetProduct() {
        AccountOpenRequest request = request("CHILD", 6L, 3L);
        when(accountMapper.countActiveChildById(6L)).thenReturn(1);
        when(accountMapper.countActiveParentAccess(1L, 6L)).thenReturn(1);
        FinancialProduct product = product(3L, "SAVING", 12);
        product.setTargetOwnerType("PARENT");
        when(productMapper.findActiveProductById(3L)).thenReturn(product);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.open(1L, request)
        );

        assertEquals(
                ErrorCode.INVALID_ACCOUNT_OPEN_REQUEST,
                exception.getErrorCode()
        );
        verify(accountMapper, never()).insertOpenedAccount(any());
    }

    @Test
    void rejectsUnknownProductTargetOwnerType() {
        AccountOpenRequest request = request("PARENT", null, 2L);
        FinancialProduct product = product(2L, "ACCOUNT", null);
        product.setTargetOwnerType("UNKNOWN");
        when(productMapper.findActiveProductById(2L)).thenReturn(product);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.open(1L, request)
        );

        assertEquals(
                ErrorCode.INVALID_ACCOUNT_OPEN_REQUEST,
                exception.getErrorCode()
        );
        verify(accountMapper, never()).insertOpenedAccount(any());
    }

    @Test
    void rejectsChildAccountBeforeParentOnboarding() {
        AccountOpenRequest request = request("CHILD", 6L, 2L);
        when(accountMapper.countActiveChildById(6L)).thenReturn(1);
        when(accountMapper.countActiveParentAccess(1L, 6L)).thenReturn(1);
        when(productMapper.findActiveProductById(2L))
                .thenReturn(product(2L, "ACCOUNT", null));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.open(1L, request)
        );

        assertEquals(
                ErrorCode.PARENT_DEMAND_DEPOSIT_REQUIRED,
                exception.getErrorCode()
        );
    }

    private void prepareChildScope() {
        when(accountMapper.countActiveChildById(6L)).thenReturn(1);
        when(accountMapper.countActiveParentAccess(1L, 6L)).thenReturn(1);
        when(accountMapper.countActiveParentDemandDeposit(1L)).thenReturn(1);
        when(accountMapper.findActiveChildMemberIdByChildId(6L)).thenReturn(9L);
    }

    private void prepareNumber() {
        when(generator.generate()).thenReturn("123-456-789012");
        when(accountMapper.countAccountNumberHash(any())).thenReturn(0);
        when(protector.encrypt("123-456-789012")).thenReturn(new byte[]{1});
    }

    private void assignAccountId(long accountId) {
        doAnswer(invocation -> {
            AccountOpenRecord record = invocation.getArgument(0);
            record.setAccountId(accountId);
            return 1;
        }).when(accountMapper).insertOpenedAccount(any());
    }

    private FinancialProduct product(long id, String type, Integer months) {
        FinancialProduct product = new FinancialProduct();
        product.setFinancialProductId(id);
        product.setProductType(type);
        product.setBankName("KB국민은행");
        product.setName("KB Mock 상품");
        product.setTargetOwnerType("ACCOUNT".equals(type) ? "BOTH" : "CHILD");
        product.setContractPeriodMonths(months);
        return product;
    }

    private AccountOpenRequest request(
            String type,
            Long childId,
            Long productId
    ) {
        AccountOpenRequest request = new AccountOpenRequest();
        ReflectionTestUtils.setField(request, "ownerType", type);
        ReflectionTestUtils.setField(request, "childId", childId);
        ReflectionTestUtils.setField(request, "financialProductId", productId);
        ReflectionTestUtils.setField(
                request,
                "initialDepositAmount",
                BigDecimal.ZERO
        );
        return request;
    }
}
