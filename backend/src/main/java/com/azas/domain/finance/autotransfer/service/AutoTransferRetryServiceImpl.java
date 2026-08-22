package com.azas.domain.finance.autotransfer.service;

import com.azas.domain.finance.autotransfer.dto.*;
import com.azas.domain.finance.autotransfer.entity.AutoTransferScheduleStatus;
import com.azas.domain.finance.autotransfer.mapper.AutoTransferRetryMapper;
import com.azas.domain.finance.autotransfer.mapper.AutoTransferScheduleMapper;
import com.azas.domain.finance.transfer.dto.TransferTransactionInsertCommand;
import com.azas.domain.finance.transfer.entity.TransferStatus;
import com.azas.domain.notification.service.PushMessage;
import com.azas.domain.notification.service.PushNotificationPublisher;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class AutoTransferRetryServiceImpl
        implements AutoTransferRetryService {

    private final AutoTransferScheduleMapper scheduleMapper;
    private final AutoTransferRetryMapper retryMapper;
    private final Clock clock;
    private final PushNotificationPublisher pushNotificationPublisher;

    @Autowired
    public AutoTransferRetryServiceImpl(
            AutoTransferScheduleMapper scheduleMapper,
            AutoTransferRetryMapper retryMapper,
            PushNotificationPublisher pushNotificationPublisher
    ) {
        this(
                scheduleMapper,
                retryMapper,
                Clock.systemUTC(),
                pushNotificationPublisher
        );
    }

    AutoTransferRetryServiceImpl(
            AutoTransferScheduleMapper scheduleMapper,
            AutoTransferRetryMapper retryMapper,
            Clock clock,
            PushNotificationPublisher pushNotificationPublisher
    ) {
        this.scheduleMapper = scheduleMapper;
        this.retryMapper = retryMapper;
        this.clock = clock;
        this.pushNotificationPublisher = pushNotificationPublisher;
    }

    @Override
    @Transactional
    public AutoTransferRetryResponse retry(
            Long memberId,
            Long scheduleId,
            String idempotencyKey
    ) {
        validateRequest(scheduleId, idempotencyKey);

        /*
         * 일정 행을 먼저 잠가 정기 실행기와 여러 재시도 요청이
         * 같은 일정을 동시에 실행하지 못하게 한다.
         */
        AutoTransferScheduleRow schedule =
                scheduleMapper.findScheduleForUpdate(scheduleId);

        if (schedule == null) {
            throw new BusinessException(
                    ErrorCode.AUTO_TRANSFER_SCHEDULE_NOT_FOUND
            );
        }

        validateAccess(memberId, schedule);

        AutoTransferRetryRow existing =
                retryMapper.findByIdempotencyKey(idempotencyKey);

        if (existing != null) {
            if (!Objects.equals(existing.getOriginId(), scheduleId)) {
                throw new BusinessException(
                        ErrorCode.DUPLICATE_TRANSFER_REQUEST
                );
            }

            return toResponse(existing, scheduleId);
        }

        AutoTransferRetryRow failedTransfer =
                retryMapper.findLatestTransferForUpdate(scheduleId);

        if (failedTransfer == null
                || failedTransfer.getStatus() != TransferStatus.FAILED) {
            throw new BusinessException(
                    ErrorCode.AUTO_TRANSFER_RETRY_NOT_AVAILABLE
            );
        }

        AutoTransferExecutionAccountRow source;
        AutoTransferExecutionAccountRow destination;

        // 교착상태 방지를 위해 ID가 작은 계좌부터 잠근다.
        if (schedule.getSourceAccountId()
                < schedule.getDestinationAccountId()) {
            source = retryMapper.findAccountForExecutionForUpdate(
                    schedule.getSourceAccountId()
            );
            destination = retryMapper.findAccountForExecutionForUpdate(
                    schedule.getDestinationAccountId()
            );
        } else {
            destination = retryMapper.findAccountForExecutionForUpdate(
                    schedule.getDestinationAccountId()
            );
            source = retryMapper.findAccountForExecutionForUpdate(
                    schedule.getSourceAccountId()
            );
        }

        if (source == null || destination == null) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR
            );
        }

        LocalDateTime requestedAt = LocalDateTime.now(clock);

        AutoTransferRetryInsertCommand command =
                new AutoTransferRetryInsertCommand(
                        null,
                        schedule.getChildId(),
                        memberId,
                        schedule.getFinancialGoalId(),
                        schedule.getSourceAccountId(),
                        schedule.getDestinationAccountId(),
                        schedule.getAmount(),
                        scheduleId,
                        failedTransfer.getFinancialTransferId(),
                        idempotencyKey,
                        requestedAt
                );

        if (retryMapper.insertRetryTransfer(command) != 1
                || command.getFinancialTransferId() == null) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR
            );
        }

        String accountFailure = validateAccounts(
                memberId,
                schedule,
                source,
                destination
        );

        if (accountFailure != null) {
            return completeFailure(
                    schedule,
                    command,
                    "ACCOUNT_NOT_AVAILABLE",
                    accountFailure
            );
        }

        if (retryMapper.decreaseSourceBalance(
                source.getFinancialAccountId(),
                schedule.getAmount()
        ) != 1) {
            return completeFailure(
                    schedule,
                    command,
                    "INSUFFICIENT_BALANCE",
                    "출금 계좌의 잔액이 부족합니다."
            );
        }

        if (retryMapper.increaseDestinationBalance(
                destination.getFinancialAccountId(),
                schedule.getAmount()
        ) != 1) {
            /*
             * 여기서 예외를 발생시키면 전체 트랜잭션이 롤백되어
             * 먼저 수행한 출금도 함께 취소된다.
             */
            throw new BusinessException(
                    ErrorCode.TRANSFER_PROCESSING_FAILED
            );
        }

        LocalDateTime completedAt = LocalDateTime.now(clock);

        TransferTransactionInsertCommand debit =
                transaction(
                        source,
                        destination,
                        null,
                        "DEBIT",
                        source.getBalance().subtract(schedule.getAmount()),
                        schedule
                );

        TransferTransactionInsertCommand credit =
                transaction(
                        destination,
                        source,
                        schedule.getChildId(),
                        "CREDIT",
                        destination.getBalance().add(schedule.getAmount()),
                        schedule
                );

        if (retryMapper.insertTransaction(debit) != 1
                || retryMapper.insertTransaction(credit) != 1) {
            throw new BusinessException(
                    ErrorCode.TRANSFER_PROCESSING_FAILED
            );
        }

        if (retryMapper.markRetrySucceeded(
                command.getFinancialTransferId(),
                debit.getAccountTransactionId(),
                credit.getAccountTransactionId(),
                completedAt
        ) != 1) {
            throw new BusinessException(
                    ErrorCode.TRANSFER_PROCESSING_FAILED
            );
        }

        updateScheduleResult(
                scheduleId,
                TransferStatus.SUCCEEDED,
                completedAt
        );

        insertNotification(
                schedule,
                command.getFinancialTransferId(),
                TransferStatus.SUCCEEDED,
                null,
                null,
                completedAt
        );

        return new AutoTransferRetryResponse(
                command.getFinancialTransferId(),
                scheduleId,
                failedTransfer.getFinancialTransferId(),
                TransferStatus.SUCCEEDED,
                null,
                null,
                requestedAt.toInstant(ZoneOffset.UTC),
                completedAt.toInstant(ZoneOffset.UTC)
        );
    }

    private AutoTransferRetryResponse completeFailure(
            AutoTransferScheduleRow schedule,
            AutoTransferRetryInsertCommand command,
            String failureCode,
            String failureMessage
    ) {
        LocalDateTime completedAt = LocalDateTime.now(clock);

        if (retryMapper.markRetryFailed(
                command.getFinancialTransferId(),
                failureCode,
                failureMessage,
                completedAt
        ) != 1) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR
            );
        }

        updateScheduleResult(
                schedule.getAutoTransferScheduleId(),
                TransferStatus.FAILED,
                completedAt
        );

        insertNotification(
                schedule,
                command.getFinancialTransferId(),
                TransferStatus.FAILED,
                failureCode,
                failureMessage,
                completedAt
        );

        return new AutoTransferRetryResponse(
                command.getFinancialTransferId(),
                schedule.getAutoTransferScheduleId(),
                command.getRetryOfTransferId(),
                TransferStatus.FAILED,
                failureCode,
                failureMessage,
                command.getRequestedAt().toInstant(ZoneOffset.UTC),
                completedAt.toInstant(ZoneOffset.UTC)
        );
    }

    private void updateScheduleResult(
            Long scheduleId,
            TransferStatus status,
            LocalDateTime completedAt
    ) {
        if (retryMapper.updateScheduleLastResult(
                scheduleId,
                status.name(),
                completedAt
        ) != 1) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR
            );
        }
    }

    private void insertNotification(
            AutoTransferScheduleRow schedule,
            Long transferId,
            TransferStatus status,
            String failureCode,
            String failureMessage,
            LocalDateTime createdAt
    ) {
        int insertedNotificationCount =
                retryMapper.insertResultNotification(
                        schedule.getMemberId(),
                        schedule.getChildId(),
                        schedule.getAutoTransferScheduleId(),
                        transferId,
                        status.name(),
                        schedule.getAmount(),
                        failureCode,
                        failureMessage,
                        createdAt
                );

        if (insertedNotificationCount <= 0) {
            return;
        }

        boolean succeeded = status == TransferStatus.SUCCEEDED;
        String title = succeeded
                ? "자동이체가 완료되었어요"
                : "자동이체에 실패했어요";
        String content = succeeded
                ? String.format(
                        Locale.KOREA,
                        "%,.0f원이 정상적으로 이체되었습니다.",
                        schedule.getAmount()
                )
                : failureMessage != null
                ? failureMessage
                : "자동이체 처리에 실패했습니다.";

        pushNotificationPublisher.publish(
                schedule.getMemberId(),
                new PushMessage(
                        title,
                        content,
                        "/assets",
                        Map.of(
                                "notification_type",
                                succeeded
                                        ? "AUTO_TRANSFER_SUCCEEDED"
                                        : "AUTO_TRANSFER_FAILED",
                                "reference_type",
                                "AUTO_TRANSFER_SCHEDULE",
                                "reference_id",
                                String.valueOf(
                                        schedule.getAutoTransferScheduleId()
                                ),
                                "financial_transfer_id",
                                String.valueOf(transferId)
                        )
                )
        );
    }

    private TransferTransactionInsertCommand transaction(
            AutoTransferExecutionAccountRow account,
            AutoTransferExecutionAccountRow counterparty,
            Long childId,
            String direction,
            java.math.BigDecimal balanceAfter,
            AutoTransferScheduleRow schedule
    ) {
        return new TransferTransactionInsertCommand(
                null,
                account.getFinancialAccountId(),
                counterparty.getFinancialAccountId(),
                childId,
                UUID.randomUUID().toString(),
                LocalDateTime.now(clock),
                direction,
                schedule.getAmount(),
                balanceAfter,
                "자동이체 실패 회차 재시도",
                counterparty.getAccountName()
        );
    }

    private String validateAccounts(
            Long memberId,
            AutoTransferScheduleRow schedule,
            AutoTransferExecutionAccountRow source,
            AutoTransferExecutionAccountRow destination
    ) {
        boolean validSource =
                "PARENT".equals(source.getOwnerType())
                        && Objects.equals(
                        source.getOwnerMemberId(),
                        memberId
                )
                        && "ACTIVE".equals(source.getAccountStatus())
                        && "DEMAND_DEPOSIT".equals(
                        source.getAccountProductType()
                )
                        && "ACTIVE".equals(source.getLinkStatus());

        if (!validSource) {
            return "출금 계좌가 비활성화되었거나 연결이 해제되었습니다.";
        }

        boolean validDestinationProduct =
                "SAVINGS".equals(
                        destination.getAccountProductType()
                ) || "DEMAND_DEPOSIT".equals(
                        destination.getAccountProductType()
                );

        boolean validDestination = validDestinationProduct
                        && "ACTIVE".equals(
                        destination.getAccountStatus()
                )
                        && "ACTIVE".equals(
                        destination.getLinkStatus()
                );

        boolean destinationAccessAllowed =
                ("PARENT".equals(destination.getOwnerType())
                        && Objects.equals(
                        destination.getOwnerMemberId(), memberId
                )
                        && schedule.getChildId() == null)
                        || ("CHILD".equals(destination.getOwnerType())
                        && Objects.equals(
                        destination.getChildId(), schedule.getChildId()
                ));

        if (!validDestination || !destinationAccessAllowed) {
            return "받는 계좌가 비활성화되었거나 연결이 해제되었습니다.";
        }

        return null;
    }

    private void validateAccess(
            Long memberId,
            AutoTransferScheduleRow schedule
    ) {
        if (schedule.getChildId() != null
                && scheduleMapper.countChildAccess(
                schedule.getChildId(),
                memberId
        ) <= 0) {
            throw new BusinessException(
                    ErrorCode.CHILD_ACCESS_DENIED
            );
        }

        if (!Objects.equals(schedule.getMemberId(), memberId)) {
            throw new BusinessException(
                    ErrorCode.AUTO_TRANSFER_SCHEDULE_ACCESS_DENIED
            );
        }

        if (schedule.getStatus()
                != AutoTransferScheduleStatus.ACTIVE
                && schedule.getStatus()
                != AutoTransferScheduleStatus.PAUSED) {
            throw new BusinessException(
                    ErrorCode.AUTO_TRANSFER_RETRY_NOT_AVAILABLE
            );
        }
    }

    private void validateRequest(
            Long scheduleId,
            String idempotencyKey
    ) {
        if (scheduleId == null || scheduleId <= 0
                || idempotencyKey == null
                || idempotencyKey.isBlank()) {
            throw new BusinessException(ErrorCode.BADREQUEST);
        }

        try {
            UUID.fromString(idempotencyKey);
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.BADREQUEST);
        }
    }

    private AutoTransferRetryResponse toResponse(
            AutoTransferRetryRow row,
            Long scheduleId
    ) {
        return new AutoTransferRetryResponse(
                row.getFinancialTransferId(),
                scheduleId,
                row.getRetryOfTransferId(),
                row.getStatus(),
                row.getFailureCode(),
                row.getFailureMessage(),
                row.getRequestedAt().toInstant(ZoneOffset.UTC),
                row.getCompletedAt() == null
                        ? null
                        : row.getCompletedAt()
                        .toInstant(ZoneOffset.UTC)
        );
    }
}
