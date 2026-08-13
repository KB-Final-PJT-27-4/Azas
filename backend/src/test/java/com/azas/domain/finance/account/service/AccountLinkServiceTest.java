package com.azas.domain.finance.account.service;

import com.azas.domain.finance.account.dto.AccountLinkRequest;
import com.azas.domain.finance.account.dto.AccountLinkResult;
import com.azas.domain.finance.account.dto.AccountLinkTargetRow;
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
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountLinkServiceTest {
    @Mock MemberMapper memberMapper;
    @Mock FinancialAccountMapper mapper;
    @Mock AccountNumberProtector protector;
    private AccountLinkService service;

    @BeforeEach
    void setUp() {
        service = new AccountLinkService(
                memberMapper, mapper, protector,
                Clock.fixed(Instant.parse("2026-08-13T05:00:00Z"), ZoneOffset.UTC)
        );
    }

    @Test
    void linksParentAccountsAtomicallyAndSelectsFirstDemandAsPrimary() {
        AccountLinkRequest request = request("PARENT", null, List.of(12L, 11L));
        AccountLinkTargetRow demand = target(11L, "PARENT", 1L, null,
                "DEMAND_DEPOSIT", "DISCOVERED");
        AccountLinkTargetRow savings = target(12L, "PARENT", 1L, null,
                "SAVINGS", "UNLINKED");
        when(memberMapper.findById(1L)).thenReturn(parent());
        when(mapper.findAccountLinkTargetsForUpdate(List.of(12L, 11L)))
                .thenReturn(List.of(savings, demand));
        when(mapper.linkAccount(11L, java.time.LocalDateTime.of(
                2026, 8, 13, 5, 0), true)).thenReturn(1);
        when(mapper.linkAccount(12L, java.time.LocalDateTime.of(
                2026, 8, 13, 5, 0), false)).thenReturn(1);
        when(protector.decrypt(new byte[]{1})).thenReturn("123-456");

        AccountLinkResult result = service.link(1L, request);
        assertEquals(2, result.getAccounts().size());
        assertEquals(11L, result.getAccounts().get(0).getAccountId());
        assertEquals(true, result.getAccounts().get(0).isPrimary());
    }

    @Test
    void reportsGoalSetupForLinkedChildSavings() {
        AccountLinkRequest request = request("CHILD", 6L, List.of(21L));
        AccountLinkTargetRow savings = target(21L, "CHILD", 9L, 6L,
                "SAVINGS", "DISCOVERED");
        when(memberMapper.findById(1L)).thenReturn(parent());
        when(mapper.countActiveChildById(6L)).thenReturn(1);
        when(mapper.countActiveParentAccess(1L, 6L)).thenReturn(1);
        when(mapper.findAccountLinkTargetsForUpdate(List.of(21L)))
                .thenReturn(List.of(savings));
        when(mapper.countActiveParentDemandDeposit(1L)).thenReturn(1);
        when(mapper.linkAccount(21L, java.time.LocalDateTime.of(
                2026, 8, 13, 5, 0), false)).thenReturn(1);
        when(protector.decrypt(new byte[]{1})).thenReturn("123-456");

        AccountLinkResult result = service.link(1L, request);
        assertEquals(List.of(21L), result.getGoalSetupAccountIds());
    }

    @Test
    void rejectsDuplicateIds() {
        when(memberMapper.findById(1L)).thenReturn(parent());
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.link(1L, request(
                        "PARENT", null, List.of(11L, 11L)
                ))
        );
        assertEquals(ErrorCode.INVALID_ACCOUNT_LINK_REQUEST,
                exception.getErrorCode());
    }

    @Test
    void rejectsAlreadyLinkedCandidate() {
        AccountLinkRequest request = request("PARENT", null, List.of(11L));
        when(memberMapper.findById(1L)).thenReturn(parent());
        when(mapper.findAccountLinkTargetsForUpdate(List.of(11L)))
                .thenReturn(List.of(target(11L, "PARENT", 1L,
                        null, "DEMAND_DEPOSIT", "ACTIVE")));
        BusinessException exception = assertThrows(
                BusinessException.class, () -> service.link(1L, request)
        );
        assertEquals(ErrorCode.FINANCIAL_ACCOUNT_ALREADY_EXISTS,
                exception.getErrorCode());
    }

    private Member parent() {
        return Member.createParent("p@test.com", "p", null);
    }

    private AccountLinkRequest request(String type, Long childId, List<Long> ids) {
        AccountLinkRequest request = new AccountLinkRequest();
        ReflectionTestUtils.setField(request, "ownerType", type);
        ReflectionTestUtils.setField(request, "childId", childId);
        ReflectionTestUtils.setField(request, "accountIds", ids);
        return request;
    }

    private AccountLinkTargetRow target(long id, String type, Long memberId,
                                        Long childId, String productType,
                                        String linkStatus) {
        AccountLinkTargetRow row = new AccountLinkTargetRow();
        ReflectionTestUtils.setField(row, "accountId", id);
        ReflectionTestUtils.setField(row, "ownerType", type);
        ReflectionTestUtils.setField(row, "ownerMemberId", memberId);
        ReflectionTestUtils.setField(row, "childId", childId);
        ReflectionTestUtils.setField(row, "bankName", "KB국민은행");
        ReflectionTestUtils.setField(row, "accountName", "Mock 계좌");
        ReflectionTestUtils.setField(row, "accountNumberCiphertext", new byte[]{1});
        ReflectionTestUtils.setField(row, "accountProductType", productType);
        ReflectionTestUtils.setField(row, "balance", BigDecimal.ZERO);
        ReflectionTestUtils.setField(row, "accountStatus", "ACTIVE");
        ReflectionTestUtils.setField(row, "linkStatus", linkStatus);
        return row;
    }
}
