package com.azas.domain.finance.account.service;

import com.azas.domain.finance.account.dto.AccountDetailResult;
import com.azas.domain.finance.account.dto.AccountTransactionAccountResult;
import com.azas.domain.finance.account.dto.AccountTransactionItemResult;
import com.azas.domain.finance.account.dto.AccountTransactionListResult;
import com.azas.domain.finance.account.dto.AccountTransactionRow;
import com.azas.domain.finance.account.mapper.FinancialAccountMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import com.azas.global.security.AccountNumberProtector;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountTransactionService {

    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;
    private static final String CREDIT = "CREDIT";
    private static final String DEBIT = "DEBIT";
    private static final String CURSOR_SEPARATOR = "|";

    private final FinancialAccountMapper financialAccountMapper;
    private final AccountDetailService accountDetailService;
    private final AccountNumberProtector accountNumberProtector;

    @Transactional(readOnly = true)
    public AccountTransactionListResult getTransactions(
            long requesterMemberId,
            long financialAccountId,
            String cursor,
            Integer requestedSize
    ) {
        int size = validateAndResolveSize(requestedSize);
        TransactionCursor decodedCursor = decodeCursor(cursor);

        AccountDetailResult currentAccount = accountDetailService
                .getAccountDetail(requesterMemberId, financialAccountId);

        List<AccountTransactionRow> rows = financialAccountMapper
                .findAccountTransactions(
                        financialAccountId,
                        decodedCursor == null
                                ? null
                                : decodedCursor.occurredAt,
                        decodedCursor == null
                                ? null
                                : decodedCursor.transactionId,
                        size + 1
                );

        boolean hasNext = rows.size() > size;
        List<AccountTransactionRow> pageRows = hasNext
                ? rows.subList(0, size)
                : rows;

        List<AccountTransactionItemResult> transactions = new ArrayList<>();
        for (AccountTransactionRow row : pageRows) {
            transactions.add(toItemResult(currentAccount, row));
        }

        String nextCursor = null;
        if (hasNext && !pageRows.isEmpty()) {
            AccountTransactionRow lastRow = pageRows.get(
                    pageRows.size() - 1
            );
            nextCursor = encodeCursor(
                    lastRow.getOccurredAt(),
                    lastRow.getAccountTransactionId()
            );
        }

        return new AccountTransactionListResult(
                financialAccountId,
                List.copyOf(transactions),
                nextCursor,
                hasNext
        );
    }

    private AccountTransactionItemResult toItemResult(
            AccountDetailResult currentAccount,
            AccountTransactionRow row
    ) {
        validateStoredTransaction(row);

        AccountTransactionAccountResult current =
                new AccountTransactionAccountResult(
                        currentAccount.getAccountId(),
                        currentAccount.getBankName(),
                        currentAccount.getAccountName(),
                        currentAccount.getAccountNumber()
                );
        AccountTransactionAccountResult counterparty =
                toCounterpartyResult(row);

        boolean credit = CREDIT.equals(row.getDirection());
        return new AccountTransactionItemResult(
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

    private AccountTransactionAccountResult toCounterpartyResult(
            AccountTransactionRow row
    ) {
        String accountName = row.getCounterpartyAccountName() == null
                ? row.getCounterpartyName()
                : row.getCounterpartyAccountName();

        return new AccountTransactionAccountResult(
                row.getCounterpartyAccountId(),
                row.getCounterpartyBankName(),
                accountName,
                decryptCounterpartyAccountNumber(
                        row.getCounterpartyAccountNumberCiphertext()
                )
        );
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

    private void validateStoredTransaction(AccountTransactionRow row) {
        if (row == null
                || row.getAccountTransactionId() == null
                || row.getAccountTransactionId() < 1
                || row.getOccurredAt() == null
                || row.getAmount() == null
                || row.getAmount().signum() < 0
                || (!CREDIT.equals(row.getDirection())
                && !DEBIT.equals(row.getDirection()))) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private int validateAndResolveSize(Integer requestedSize) {
        int size = requestedSize == null ? DEFAULT_SIZE : requestedSize;
        if (size < 1 || size > MAX_SIZE) {
            throw new BusinessException(ErrorCode.INVALID_QUERY_PARAMETER);
        }
        return size;
    }

    private TransactionCursor decodeCursor(String cursor) {
        if (cursor == null) {
            return null;
        }
        if (cursor.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_QUERY_PARAMETER);
        }

        try {
            String decoded = new String(
                    Base64.getUrlDecoder().decode(cursor),
                    StandardCharsets.UTF_8
            );
            int separatorIndex = decoded.lastIndexOf(CURSOR_SEPARATOR);
            if (separatorIndex < 1
                    || separatorIndex == decoded.length() - 1) {
                throw new IllegalArgumentException();
            }

            LocalDateTime occurredAt = LocalDateTime.parse(
                    decoded.substring(0, separatorIndex)
            );
            long transactionId = Long.parseLong(
                    decoded.substring(separatorIndex + 1)
            );
            if (transactionId < 1) {
                throw new IllegalArgumentException();
            }
            return new TransactionCursor(occurredAt, transactionId);
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.INVALID_QUERY_PARAMETER);
        }
    }

    private String encodeCursor(
            LocalDateTime occurredAt,
            long transactionId
    ) {
        String raw = occurredAt + CURSOR_SEPARATOR + transactionId;
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    private static final class TransactionCursor {

        private final LocalDateTime occurredAt;
        private final long transactionId;

        private TransactionCursor(
                LocalDateTime occurredAt,
                long transactionId
        ) {
            this.occurredAt = occurredAt;
            this.transactionId = transactionId;
        }
    }
}
