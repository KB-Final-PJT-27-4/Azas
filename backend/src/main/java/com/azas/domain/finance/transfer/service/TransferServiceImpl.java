package com.azas.domain.finance.transfer.service;

import com.azas.domain.finance.transfer.dto.MemberTransferListRow;
import com.azas.domain.finance.transfer.dto.*;
import com.azas.domain.finance.transfer.entity.TransferAccount;
import com.azas.domain.finance.transfer.entity.TransferStatus;
import com.azas.domain.finance.transfer.entity.TransferType;
import com.azas.domain.finance.transfer.mapper.TransferMapper;
import com.azas.domain.report.service.AssetReportSnapshotService;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import com.azas.global.security.AccountNumberProtector;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private static final ZoneId SERVICE_ZONE =
            ZoneId.of("Asia/Seoul");

    private final TransferMapper transferMapper;
    private final AccountNumberProtector accountNumberProtector;
    private final AssetReportSnapshotService assetReportSnapshotService;

    // 부모 입출금 계좌에서 접근 가능한 입출금/적금 계좌로 이체한다.
    @Override
    @Transactional
    public TransferCreateResponse createTransfer(
            Long memberId,
            String idempotencyKey,
            CreateTransferRequest request
    ) {
        validateIdempotencyKey(idempotencyKey);
        validateTransferRequest(request);

        if (transferMapper.findTransferIdByIdempotencyKey(idempotencyKey) != null) {
            throw new BusinessException(ErrorCode.DUPLICATE_TRANSFER_REQUEST);
        }

        TransferAccount source;
        TransferAccount destination;
        if (request.getSourceAccountId() < request.getDestinationAccountId()) {
            source = transferMapper.findSourceAccountForUpdate(
                    request.getSourceAccountId(), memberId
            );
            destination = transferMapper.findDestinationAccountForUpdate(
                    request.getDestinationAccountId()
            );
        } else {
            destination = transferMapper.findDestinationAccountForUpdate(
                    request.getDestinationAccountId()
            );
            source = transferMapper.findSourceAccountForUpdate(
                    request.getSourceAccountId(), memberId
            );
        }

        validateTransferAccounts(memberId, source, destination, request.getAmount());

        LocalDateTime requestedAt = LocalDateTime.now(ZoneOffset.UTC);
        TransferInsertCommand command = new TransferInsertCommand(
                null,
                destination.getChildId(),
                memberId,
                source.getFinancialAccountId(),
                destination.getFinancialAccountId(),
                request.getAmount(),
                request.getMemo(),
                idempotencyKey,
                requestedAt
        );
        insertTransfer(command);

        LocalDateTime completedAt = LocalDateTime.now(ZoneOffset.UTC);
        if (transferMapper.decreaseSourceBalance(
                source.getFinancialAccountId(), request.getAmount()
        ) != 1) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_ACCOUNT_BALANCE);
        }
        if (transferMapper.increaseDestinationBalance(
                destination.getFinancialAccountId(), request.getAmount()
        ) != 1) {
            throw new BusinessException(ErrorCode.TRANSFER_PROCESSING_FAILED);
        }

        if (destination.getChildId() != null
                && transferMapper.insertDestinationBalanceSnapshot(
                destination.getFinancialAccountId(),
                destination.getChildId(),
                destination.getBalance().add(request.getAmount()),
                completedAt
        ) != 1) {
            throw new BusinessException(ErrorCode.TRANSFER_PROCESSING_FAILED);
        }

        TransferTransactionInsertCommand debitTransaction =
                new TransferTransactionInsertCommand(
                        null,
                        source.getFinancialAccountId(),
                        destination.getFinancialAccountId(),
                        null,
                        createTransactionFingerprint(),
                        requestedAt,
                        "DEBIT",
                        request.getAmount(),
                        source.getBalance().subtract(request.getAmount()),
                        request.getMemo(),
                        destination.getAccountName()
                );
        TransferTransactionInsertCommand creditTransaction =
                new TransferTransactionInsertCommand(
                        null,
                        destination.getFinancialAccountId(),
                        source.getFinancialAccountId(),
                        destination.getChildId(),
                        createTransactionFingerprint(),
                        requestedAt,
                        "CREDIT",
                        request.getAmount(),
                        destination.getBalance().add(request.getAmount()),
                        request.getMemo(),
                        source.getAccountName()
                );
        insertTransaction(debitTransaction);
        insertTransaction(creditTransaction);
        if (transferMapper.markTransferSucceeded(
                command.getFinancialTransferId(),
                debitTransaction.getAccountTransactionId(),
                completedAt
        ) != 1) {
            throw new BusinessException(ErrorCode.TRANSFER_PROCESSING_FAILED);
        }

        if (destination.getChildId() != null) {
            assetReportSnapshotService.generateForChild(
                    destination.getChildId(),
                    YearMonth.from(
                            completedAt.atZone(ZoneOffset.UTC)
                                    .withZoneSameInstant(SERVICE_ZONE)
                    )
            );
        }

        return new TransferCreateResponse(
                command.getFinancialTransferId(),
                destination.getFinancialGoalId(),
                toAccountResponse(source),
                toAccountResponse(destination),
                request.getAmount(),
                request.getMemo(),
                TransferType.MANUAL,
                TransferStatus.SUCCEEDED,
                completedAt.toInstant(ZoneOffset.UTC)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public TransferListResponse<ChildTransferListItemResponse> getChildTransfers(
            Long memberId,
            Long childId,
            Long financialGoalId,
            String status,
            LocalDate startDate,
            LocalDate endDate,
            String cursor,
            Integer size
    ) {
        validateChildAccess(memberId, childId);
        validateDateRange(startDate, endDate);

        TransferListQuery query = new TransferListQuery(
                null,
                childId,
                financialGoalId,
                parseStatus(status),
                null,
                startDate,
                endDate,
                parseCursor(cursor),
                normalizePageSize(size) + 1
        );

        return toPage(transferMapper.findChildTransfers(query), normalizePageSize(size));
    }

    @Override
    @Transactional(readOnly = true)
    public TransferListResponse<MemberTransferListItemResponse> getMemberTransfers(
            Long memberId,
            String status,
            String transferType,
            Long childId,
            LocalDate startDate,
            LocalDate endDate,
            String cursor,
            Integer size
    ) {
        validateDateRange(startDate, endDate);

        int pageSize = normalizePageSize(size);
        TransferListQuery query = new TransferListQuery(
                memberId,
                childId,
                null,
                parseStatus(status),
                parseTransferType(transferType),
                startDate,
                endDate,
                parseCursor(cursor),
                pageSize + 1
        );

        List<MemberTransferListItemResponse> items =
                transferMapper.findMemberTransfers(query)
                        .stream()
                        .map(this::toMemberTransferItemResponse)
                        .toList();

        return toPage(items, pageSize);
    }

    @Override
    @Transactional(readOnly = true)
    public TransferDetailResponse getTransfer(Long memberId, Long transferId) {
        TransferDetailResponse response = transferMapper.findTransferDetail(transferId, memberId);
        if (response == null) {
            throw new BusinessException(ErrorCode.TRANSFER_NOT_FOUND);
        }
        return response;
    }

    private void validateTransferRequest(CreateTransferRequest request) {
        if (request == null
                || request.getSourceAccountId() == null
                || request.getDestinationAccountId() == null
                || request.getAmount() == null
                || request.getAmount().signum() <= 0
                || Objects.equals(request.getSourceAccountId(), request.getDestinationAccountId())) {
            throw new BusinessException(ErrorCode.INVALID_TRANSFER_REQUEST);
        }
    }

    private void validateTransferAccounts(
            Long memberId,
            TransferAccount source,
            TransferAccount destination,
            BigDecimal amount
    ) {
        if (source == null) {
            throw new BusinessException(ErrorCode.FINANCIAL_ACCOUNT_NOT_FOUND);
        }

        // 입금 계좌 자체가 존재하지 않는 경우 (404 처리)
        if (destination == null) {
            throw new BusinessException(ErrorCode.FINANCIAL_ACCOUNT_NOT_FOUND);
        }

        boolean isSupportedDestination =
                "DEMAND_DEPOSIT".equals(
                        destination.getAccountProductType()
                ) || "SAVINGS".equals(
                        destination.getAccountProductType()
                );

        if (!isSupportedDestination
                || !"ACTIVE".equals(destination.getAccountStatus())
                || !"ACTIVE".equals(destination.getLinkStatus())) {
            throw new BusinessException(ErrorCode.INVALID_TRANSFER_REQUEST);
        }

        if ("PARENT".equals(destination.getOwnerType())) {
            if (!Objects.equals(destination.getOwnerMemberId(), memberId)) {
                throw new BusinessException(
                        ErrorCode.FINANCIAL_ACCOUNT_ACCESS_DENIED
                );
            }
        } else if ("CHILD".equals(destination.getOwnerType())
                && destination.getChildId() != null) {
            validateChildAccess(memberId, destination.getChildId());
        } else {
            throw new BusinessException(ErrorCode.INVALID_TRANSFER_REQUEST);
        }

        if (source.getBalance() == null || source.getBalance().compareTo(amount) < 0) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_ACCOUNT_BALANCE);
        }
    }

    private void validateChildAccess(Long memberId, Long childId) {
        if (transferMapper.countChildAccess(childId, memberId) == 0) {
            throw new BusinessException(ErrorCode.CHILD_ACCESS_DENIED);
        }
    }

    private void validateIdempotencyKey(String idempotencyKey) {
        try {
            UUID.fromString(idempotencyKey);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new BusinessException(ErrorCode.BADREQUEST);
        }
    }

    private void insertTransfer(TransferInsertCommand command) {
        try {
            if (transferMapper.insertTransfer(command) != 1) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
            }
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(ErrorCode.DUPLICATE_TRANSFER_REQUEST);
        }
    }

    private void insertTransaction(TransferTransactionInsertCommand transaction) {
        if (transferMapper.insertTransaction(transaction) != 1) {
            throw new BusinessException(ErrorCode.TRANSFER_PROCESSING_FAILED);
        }
    }

    private String createTransactionFingerprint() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private TransferAccountResponse toAccountResponse(
            TransferAccount account
    ) {
        String accountNumber = account.getAccountNumber();

        if (
                accountNumber == null
                        && account.getAccountNumberCiphertext() != null
        ) {
            accountNumber = accountNumberProtector.decrypt(
                    account.getAccountNumberCiphertext()
            );
        }

        return new TransferAccountResponse(
                account.getFinancialAccountId(),
                account.getBankName(),
                account.getAccountName(),
                accountNumber
        );
    }

    private MemberTransferListItemResponse toMemberTransferItemResponse(
            MemberTransferListRow row
    ) {
        return new MemberTransferListItemResponse(
                row.getFinancialTransferId(),
                row.getChildId(),
                row.getRequestedByMemberId(),
                row.getTransferType(),
                new TransferAccountResponse(
                        row.getSourceAccountId(),
                        row.getSourceBankName(),
                        row.getSourceAccountName(),
                        decryptAccountNumber(
                                row.getSourceAccountNumberCiphertext()
                        )
                ),
                new TransferAccountResponse(
                        row.getDestinationAccountId(),
                        row.getDestinationBankName(),
                        row.getDestinationAccountName(),
                        decryptAccountNumber(
                                row.getDestinationAccountNumberCiphertext()
                        )
                ),
                row.getAmount(),
                row.getMemo(),
                row.getStatus(),
                row.getFailureCode(),
                row.getFailureMessage(),
                row.getRequestedAt(),
                row.getCompletedAt()
        );
    }

    private String decryptAccountNumber(byte[] ciphertext) {
        try {
            return accountNumberProtector.decrypt(ciphertext);
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR
            );
        }
    }


    private TransferStatus parseStatus(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return TransferStatus.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.BADREQUEST);
        }
    }

    private TransferType parseTransferType(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return TransferType.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.BADREQUEST);
        }
    }

    private Long parseCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            long cursorId = Long.parseLong(cursor);
            if (cursorId <= 0) {
                throw new NumberFormatException();
            }
            return cursorId;
        } catch (NumberFormatException exception) {
            throw new BusinessException(ErrorCode.BADREQUEST);
        }
    }

    private int normalizePageSize(Integer size) {
        if (size == null) {
            return DEFAULT_PAGE_SIZE;
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessException(ErrorCode.BADREQUEST);
        }
        return size;
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BusinessException(ErrorCode.BADREQUEST);
        }
    }

    private <T> TransferListResponse<T> toPage(List<T> rows, int pageSize) {
        List<T> items = rows == null ? new ArrayList<>() : new ArrayList<>(rows);
        boolean hasNext = items.size() > pageSize;
        if (hasNext) {
            items.remove(items.size() - 1);
        }

        String nextCursor = null;
        if (hasNext && !items.isEmpty()) {
            Object lastItem = items.get(items.size() - 1);
            if (lastItem instanceof ChildTransferListItemResponse) {
                nextCursor = String.valueOf(
                        ((ChildTransferListItemResponse) lastItem).getFinancialTransferId()
                );
            } else if (lastItem instanceof MemberTransferListItemResponse) {
                nextCursor = String.valueOf(
                        ((MemberTransferListItemResponse) lastItem).getFinancialTransferId()
                );
            }
        }
        return new TransferListResponse<>(items, nextCursor, hasNext);
    }
}
