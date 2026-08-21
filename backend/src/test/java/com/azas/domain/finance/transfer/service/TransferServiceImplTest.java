package com.azas.domain.finance.transfer.service;

import com.azas.domain.finance.transfer.dto.CreateTransferRequest;
import com.azas.domain.finance.transfer.dto.TransferInsertCommand;
import com.azas.domain.finance.transfer.dto.TransferTransactionInsertCommand;
import com.azas.domain.finance.transfer.entity.TransferAccount;
import com.azas.domain.finance.transfer.entity.TransferStatus;
import com.azas.domain.finance.transfer.mapper.TransferMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import com.azas.global.security.AccountNumberProtector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferServiceImplTest {

    @Mock
    private AccountNumberProtector accountNumberProtector;

    @Mock
    private TransferMapper transferMapper;

    @InjectMocks
    private TransferServiceImpl transferService;


    @Test
    void 이체를_즉시_처리한다() {
        String key = givenNewIdempotencyKey();

        givenSourceAccount(301L, 100_000L);
        givenDestinationAccount(300L, 10L, 3L);

        when(transferMapper.countChildAccess(10L, 1L))
                .thenReturn(1);

        assignTransferId(5001L);

        when(transferMapper.decreaseSourceBalance(
                301L,
                new BigDecimal("10000")
        )).thenReturn(1);

        when(transferMapper.increaseDestinationBalance(
                300L,
                new BigDecimal("10000")
        )).thenReturn(1);

        when(transferMapper.insertTransaction(any()))
                .thenAnswer(invocation -> {
                    TransferTransactionInsertCommand command =
                            invocation.getArgument(0);

                    ReflectionTestUtils.setField(
                            command,
                            "accountTransactionId",
                            7001L
                    );

                    return 1;
                });

        when(transferMapper.markTransferSucceeded(
                eq(5001L),
                eq(7001L),
                any()
        )).thenReturn(1);

        var response = transferService.createTransfer(
                1L,
                key,
                createRequest(
                        301L,
                        300L,
                        10_000L,
                        "대학자금"
                )
        );

        assertEquals(
                5001L,
                response.getFinancialTransferId()
        );

        assertEquals(
                3L,
                response.getFinancialGoalId()
        );

        assertEquals(
                TransferStatus.SUCCEEDED,
                response.getStatus()
        );

        assertNotNull(response.getRequestedAt());

        verify(transferMapper)
                .insertTransfer(any(TransferInsertCommand.class));

        verify(transferMapper)
                .decreaseSourceBalance(
                        301L,
                        new BigDecimal("10000")
                );

        verify(transferMapper)
                .increaseDestinationBalance(
                        300L,
                        new BigDecimal("10000")
                );

        verify(
                transferMapper,
                org.mockito.Mockito.times(2)
        ).insertTransaction(any());
    }

    @Test
    void 잘못된_멱등키를_거절한다() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> transferService.createTransfer(
                        1L,
                        "not-a-uuid",
                        createRequest(
                                301L,
                                300L,
                                10_000L,
                                null
                        )
                )
        );

        assertEquals(
                ErrorCode.BADREQUEST,
                exception.getErrorCode()
        );
    }

    @Test
    void 동일한_계좌로의_이체를_거절한다() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> transferService.createTransfer(
                        1L,
                        UUID.randomUUID().toString(),
                        createRequest(
                                301L,
                                301L,
                                10_000L,
                                null
                        )
                )
        );

        assertEquals(
                ErrorCode.INVALID_TRANSFER_REQUEST,
                exception.getErrorCode()
        );
    }

    @Test
    void 목표가_연결되지_않은_입금계좌를_거절한다() {
        String key = givenNewIdempotencyKey();

        givenSourceAccount(301L, 100_000L);
        givenDestinationAccount(300L, 10L, null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> transferService.createTransfer(
                        1L,
                        key,
                        createRequest(
                                301L,
                                300L,
                                10_000L,
                                null
                        )
                )
        );

        assertEquals(
                ErrorCode.INVALID_TRANSFER_REQUEST,
                exception.getErrorCode()
        );
    }

    @Test
    void 잔액이_부족하면_거절한다() {
        String key = givenNewIdempotencyKey();

        givenSourceAccount(301L, 9_999L);
        givenDestinationAccount(300L, 10L, 3L);

        when(transferMapper.countChildAccess(10L, 1L))
                .thenReturn(1);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> transferService.createTransfer(
                        1L,
                        key,
                        createRequest(
                                301L,
                                300L,
                                10_000L,
                                null
                        )
                )
        );

        assertEquals(
                ErrorCode.INSUFFICIENT_ACCOUNT_BALANCE,
                exception.getErrorCode()
        );
    }

    @Test
    void 자녀_관리권한이_없으면_거절한다() {
        String key = givenNewIdempotencyKey();

        givenSourceAccount(301L, 100_000L);
        givenDestinationAccount(300L, 10L, 3L);

        when(transferMapper.countChildAccess(10L, 1L))
                .thenReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> transferService.createTransfer(
                        1L,
                        key,
                        createRequest(
                                301L,
                                300L,
                                10_000L,
                                null
                        )
                )
        );

        assertEquals(
                ErrorCode.CHILD_ACCESS_DENIED,
                exception.getErrorCode()
        );
    }

    @Test
    void 중복_멱등키를_거절한다() {
        String key = UUID.randomUUID().toString();

        when(transferMapper.findTransferIdByIdempotencyKey(key))
                .thenReturn(5001L);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> transferService.createTransfer(
                        1L,
                        key,
                        createRequest(
                                301L,
                                300L,
                                10_000L,
                                null
                        )
                )
        );

        assertEquals(
                ErrorCode.DUPLICATE_TRANSFER_REQUEST,
                exception.getErrorCode()
        );
    }

    private String givenNewIdempotencyKey() {
        String key = UUID.randomUUID().toString();

        when(transferMapper.findTransferIdByIdempotencyKey(key))
                .thenReturn(null);

        return key;
    }

    private CreateTransferRequest createRequest(
            Long sourceAccountId,
            Long destinationAccountId,
            long amount,
            String memo
    ) {
        CreateTransferRequest request =
                new CreateTransferRequest();

        ReflectionTestUtils.setField(
                request,
                "sourceAccountId",
                sourceAccountId
        );

        ReflectionTestUtils.setField(
                request,
                "destinationAccountId",
                destinationAccountId
        );

        ReflectionTestUtils.setField(
                request,
                "amount",
                BigDecimal.valueOf(amount)
        );

        ReflectionTestUtils.setField(
                request,
                "memo",
                memo
        );

        return request;
    }

    private void givenSourceAccount(
            Long accountId,
            long balance
    ) {
        TransferAccount account =
                new TransferAccount();

        ReflectionTestUtils.setField(
                account,
                "financialAccountId",
                accountId
        );

        ReflectionTestUtils.setField(
                account,
                "bankName",
                "KB국민은행"
        );

        ReflectionTestUtils.setField(
                account,
                "accountNumber",
                "987-6543-54321"
        );

        ReflectionTestUtils.setField(
                account,
                "accountName",
                "부모 생활비 통장"
        );

        ReflectionTestUtils.setField(
                account,
                "balance",
                BigDecimal.valueOf(balance)
        );

        when(transferMapper.findSourceAccountForUpdate(
                eq(accountId),
                anyLong()
        )).thenReturn(account);
    }

    private void givenDestinationAccount(
            Long accountId,
            Long childId,
            Long goalTemplateId
    ) {
        TransferAccount account =
                new TransferAccount();

        ReflectionTestUtils.setField(
                account,
                "financialAccountId",
                accountId
        );

        ReflectionTestUtils.setField(
                account,
                "childId",
                childId
        );

        ReflectionTestUtils.setField(
                account,
                "financialGoalId",
                goalTemplateId
        );

        ReflectionTestUtils.setField(
                account,
                "bankName",
                "KB국민은행"
        );

        ReflectionTestUtils.setField(
                account,
                "accountNumber",
                "123-4567-56789"
        );

        ReflectionTestUtils.setField(
                account,
                "accountName",
                "자녀 대학자금 적금"
        );

        ReflectionTestUtils.setField(
                account,
                "balance",
                BigDecimal.ZERO
        );

        when(transferMapper.findDestinationAccountForUpdate(
                eq(accountId)
        )).thenReturn(account);
    }

    private void assignTransferId(Long transferId) {
        when(transferMapper.insertTransfer(any()))
                .thenAnswer(invocation -> {
                    TransferInsertCommand command =
                            invocation.getArgument(0);

                    command.setFinancialTransferId(transferId);

                    return 1;
                });
    }
}
