package com.azas.domain.finance.account.service;

import com.azas.domain.finance.account.dto.AccountDetailResult;
import com.azas.domain.finance.account.dto.AccountTransactionDetailResult;
import com.azas.domain.finance.account.dto.AccountTransactionDetailRow;
import com.azas.domain.finance.account.dto.AccountTransactionPartyResult;
import com.azas.domain.finance.account.mapper.FinancialAccountMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import com.azas.global.security.AccountNumberProtector;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountTransactionDetailService {

    private static final String CREDIT = "CREDIT";
    private static final String DEBIT = "DEBIT";

    private final FinancialAccountMapper financialAccountMapper;
    private final AccountDetailService accountDetailService;
    private final AccountNumberProtector accountNumberProtector;

    @Transactional(readOnly = true)
    public AccountTransactionDetailResult getTransactionDetail(
            long requesterMemberId,
            long accountTransactionId
    ) {
        validateTransactionId(accountTransactionId);

        AccountTransactionDetailRow row = financialAccountMapper
                .findAccountTransactionById(accountTransactionId);
        if (row == null) {
            throw transactionNotFound();
        }
        validateStoredTransaction(row);

        AccountDetailResult currentAccount = getAccessibleAccount(
                requesterMemberId,
                row.getFinancialAccountId()
        );

        AccountTransactionPartyResult current =
                new AccountTransactionPartyResult(
                        currentAccount.getBankName(),
                        currentAccount.getAccountName(),
                        currentAccount.getAccountNumber()
                );
        AccountTransactionPartyResult counterparty =
                new AccountTransactionPartyResult(
                        row.getCounterpartyBankName(),
                        resolveCounterpartyName(row),
                        decryptCounterpartyAccountNumber(
                                row.getCounterpartyAccountNumberCiphertext()
                        )
                );

        boolean credit = CREDIT.equals(row.getDirection());
        return new AccountTransactionDetailResult(
                row.getAccountTransactionId(),
                row.getOccurredAt(),
                row.getDirection(),
                row.getAmount(),
                row.getDescription(),
                credit ? current : counterparty,
                credit ? counterparty : current,
                row.getBalanceAfter()
        );
    }

    private AccountDetailResult getAccessibleAccount(
            long requesterMemberId,
            long financialAccountId
    ) {
        try {
            return accountDetailService.getAccountDetail(
                    requesterMemberId,
                    financialAccountId
            );
        } catch (BusinessException exception) {
            if (exception.getErrorCode()
                    == ErrorCode.FINANCIAL_ACCOUNT_NOT_FOUND) {
                throw transactionNotFound();
            }
            throw exception;
        }
    }

    private String resolveCounterpartyName(
            AccountTransactionDetailRow row
    ) {
        return row.getCounterpartyAccountName() == null
                ? row.getCounterpartyName()
                : row.getCounterpartyAccountName();
    }

    private String decryptCounterpartyAccountNumber(byte[] ciphertext) {
        if (ciphertext == null) {
            return null;
        }
        try {
            return accountNumberProtector.decrypt(ciphertext);
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private void validateTransactionId(long accountTransactionId) {
        if (accountTransactionId < 1) {
            throw new BusinessException(ErrorCode.BADREQUEST);
        }
    }

    private void validateStoredTransaction(
            AccountTransactionDetailRow row
    ) {
        if (row.getAccountTransactionId() == null
                || row.getAccountTransactionId() < 1
                || row.getFinancialAccountId() == null
                || row.getFinancialAccountId() < 1
                || row.getOccurredAt() == null
                || row.getAmount() == null
                || row.getAmount().signum() < 0
                || (!CREDIT.equals(row.getDirection())
                && !DEBIT.equals(row.getDirection()))) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private BusinessException transactionNotFound() {
        return new BusinessException(
                ErrorCode.ACCOUNT_TRANSACTION_NOT_FOUND
        );
    }
}
