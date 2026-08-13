package com.azas.domain.finance.account.service;

import com.azas.domain.finance.account.dto.ParentAccountListResult;
import com.azas.domain.finance.account.dto.ParentAccountListRow;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParentAccountListServiceTest {

    private static final long MEMBER_ID = 1L;

    @Mock
    private MemberMapper memberMapper;

    @Mock
    private FinancialAccountMapper financialAccountMapper;

    @Mock
    private AccountNumberProtector accountNumberProtector;

    private ParentAccountListService service;

    @BeforeEach
    void setUp() {
        service = new ParentAccountListService(
                memberMapper,
                financialAccountMapper,
                accountNumberProtector
        );
    }

    @Test
    void returnsActiveParentAccountsAndTotalBalance() {
        byte[] firstCiphertext = new byte[]{1, 2, 3};
        byte[] secondCiphertext = new byte[]{4, 5, 6};

        when(memberMapper.findById(MEMBER_ID)).thenReturn(parentMember());
        when(financialAccountMapper.findActiveParentAccounts(MEMBER_ID))
                .thenReturn(List.of(
                        accountRow(10L, firstCiphertext, "1000000.00"),
                        accountRow(11L, secondCiphertext, "250000.00")
                ));
        when(accountNumberProtector.decrypt(firstCiphertext))
                .thenReturn("987-6543-5678");
        when(accountNumberProtector.decrypt(secondCiphertext))
                .thenReturn("987-6543-9999");

        ParentAccountListResult result = service.getMyAccounts(MEMBER_ID);

        assertEquals(2, result.getAccounts().size());
        assertEquals("987-6543-5678",
                result.getAccounts().get(0).getAccountNumber());
        assertEquals(new BigDecimal("1250000.00"),
                result.getTotalBalance());
    }

    @Test
    void returnsZeroTotalAndEmptyListWhenNoAccountIsConnected() {
        when(memberMapper.findById(MEMBER_ID)).thenReturn(parentMember());
        when(financialAccountMapper.findActiveParentAccounts(MEMBER_ID))
                .thenReturn(List.of());

        ParentAccountListResult result = service.getMyAccounts(MEMBER_ID);

        assertEquals(0, result.getAccounts().size());
        assertEquals(BigDecimal.ZERO, result.getTotalBalance());
    }

    @Test
    void rejectsChildMember() {
        when(memberMapper.findById(MEMBER_ID)).thenReturn(Member.createChild(
                "child@example.com", "child", null
        ));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.getMyAccounts(MEMBER_ID)
        );

        assertEquals(ErrorCode.PARENT_ACCESS_REQUIRED,
                exception.getErrorCode());
        verify(financialAccountMapper, never())
                .findActiveParentAccounts(MEMBER_ID);
    }

    @Test
    void hidesCiphertextWhenDecryptionFails() {
        byte[] ciphertext = new byte[]{1, 2, 3};
        when(memberMapper.findById(MEMBER_ID)).thenReturn(parentMember());
        when(financialAccountMapper.findActiveParentAccounts(MEMBER_ID))
                .thenReturn(List.of(
                        accountRow(10L, ciphertext, "1250000.00")
                ));
        when(accountNumberProtector.decrypt(ciphertext))
                .thenThrow(new IllegalArgumentException());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.getMyAccounts(MEMBER_ID)
        );

        assertEquals(ErrorCode.INTERNAL_SERVER_ERROR,
                exception.getErrorCode());
    }

    private Member parentMember() {
        return Member.createParent("parent@example.com", "parent", null);
    }

    private ParentAccountListRow accountRow(
            long accountId,
            byte[] ciphertext,
            String balance
    ) {
        ParentAccountListRow row = new ParentAccountListRow();
        ReflectionTestUtils.setField(row, "accountId", accountId);
        ReflectionTestUtils.setField(row, "accountName", "생활비 통장");
        ReflectionTestUtils.setField(
                row, "accountNumberCiphertext", ciphertext
        );
        ReflectionTestUtils.setField(
                row, "accountProductType", "DEMAND_DEPOSIT"
        );
        ReflectionTestUtils.setField(
                row, "balance", new BigDecimal(balance)
        );
        return row;
    }
}
