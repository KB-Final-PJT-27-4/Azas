package com.azas.domain.finance.autotransfer.service;

import com.azas.domain.finance.autotransfer.dto.AutoTransferAccountRow;
import com.azas.domain.finance.autotransfer.dto.AutoTransferScheduleInsertCommand;
import com.azas.domain.finance.autotransfer.dto.AutoTransferScheduleResponse;
import com.azas.domain.finance.autotransfer.dto.AutoTransferScheduleRow;
import com.azas.domain.finance.autotransfer.dto.CreateAutoTransferScheduleRequest;
import com.azas.domain.finance.autotransfer.entity.AutoTransferFrequency;
import com.azas.domain.finance.autotransfer.entity.AutoTransferScheduleStatus;
import com.azas.domain.finance.autotransfer.mapper.AutoTransferScheduleMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.UUID;

@Service
public class AutoTransferScheduleServiceImpl
        implements AutoTransferScheduleService {

    private final AutoTransferScheduleMapper mapper;
    private final Clock clock;

    @Autowired
    public AutoTransferScheduleServiceImpl(
            AutoTransferScheduleMapper mapper
    ) {
        this(mapper, Clock.systemUTC());
    }

    AutoTransferScheduleServiceImpl(
            AutoTransferScheduleMapper mapper,
            Clock clock
    ) {
        this.mapper = mapper;
        this.clock = clock;
    }

    @Override
    @Transactional
    public AutoTransferScheduleResponse createSchedule(
            Long memberId,
            String idempotencyKey,
            CreateAutoTransferScheduleRequest request
    ) {
        validateIdempotencyKey(idempotencyKey);
        validateRequest(request);

        AutoTransferScheduleRow existing =
                mapper.findByIdempotencyKey(idempotencyKey);

        if (existing != null) {
            if (!sameRequest(memberId, request, existing)) {
                throw new BusinessException(
                        ErrorCode.DUPLICATE_AUTO_TRANSFER_SCHEDULE
                );
            }
            return toResponse(existing);
        }

        validateChildAccess(memberId, request.getChildId());

        AutoTransferAccountRow source;
        AutoTransferAccountRow destination;

        // 교착상태 방지를 위해 항상 작은 계좌 ID부터 잠근다.
        if (request.getSourceAccountId()
                < request.getDestinationAccountId()) {
            source = mapper.findAccountForUpdate(
                    request.getSourceAccountId()
            );
            destination = mapper.findAccountForUpdate(
                    request.getDestinationAccountId()
            );
        } else {
            destination = mapper.findAccountForUpdate(
                    request.getDestinationAccountId()
            );
            source = mapper.findAccountForUpdate(
                    request.getSourceAccountId()
            );
        }

        validateAccounts(memberId, request, source, destination);

        if (mapper.countEquivalentSchedule(
                memberId,
                request.getChildId(),
                request.getSourceAccountId(),
                request.getDestinationAccountId(),
                request.getAmount(),
                request.getFrequency().name(),
                request.getTransferDay(),
                request.getStartDate(),
                request.getEndDate()
        ) > 0) {
            throw new BusinessException(
                    ErrorCode.DUPLICATE_AUTO_TRANSFER_SCHEDULE
            );
        }

        LocalDate nextTransferDate = calculateNextTransferDate(request);
        LocalDateTime createdAt = LocalDateTime.now(clock);

        AutoTransferScheduleInsertCommand command =
                new AutoTransferScheduleInsertCommand(
                        null,
                        request.getChildId(),
                        memberId,
                        idempotencyKey,
                        destination.getFinancialGoalId(),
                        request.getSourceAccountId(),
                        request.getDestinationAccountId(),
                        request.getAmount(),
                        request.getFrequency(),
                        request.getTransferDay(),
                        request.getStartDate(),
                        request.getEndDate(),
                        nextTransferDate.atStartOfDay(),
                        createdAt
                );

        try {
            if (mapper.insertSchedule(command) != 1) {
                throw new BusinessException(
                        ErrorCode.INTERNAL_SERVER_ERROR
                );
            }
        } catch (DuplicateKeyException exception) {
            AutoTransferScheduleRow concurrent =
                    mapper.findByIdempotencyKey(idempotencyKey);

            if (concurrent != null
                    && sameRequest(memberId, request, concurrent)) {
                return toResponse(concurrent);
            }

            throw new BusinessException(
                    ErrorCode.DUPLICATE_AUTO_TRANSFER_SCHEDULE
            );
        }

        return new AutoTransferScheduleResponse(
                command.getAutoTransferScheduleId(),
                command.getFinancialGoalId(),
                command.getSourceAccountId(),
                command.getDestinationAccountId(),
                command.getAmount(),
                command.getFrequency(),
                command.getTransferDay(),
                command.getStartDate(),
                command.getEndDate(),
                command.getNextTransferAt()
                        .toInstant(ZoneOffset.UTC),
                null,
                null,
                AutoTransferScheduleStatus.ACTIVE,
                command.getCreatedAt()
                        .toInstant(ZoneOffset.UTC)
        );
    }

    private void validateIdempotencyKey(String idempotencyKey) {
        try {
            UUID.fromString(idempotencyKey);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new BusinessException(ErrorCode.BADREQUEST);
        }
    }

    private void validateRequest(
            CreateAutoTransferScheduleRequest request
    ) {
        if (request == null
                || request.getChildId() == null
                || request.getSourceAccountId() == null
                || request.getDestinationAccountId() == null
                || request.getAmount() == null
                || request.getFrequency() == null
                || request.getTransferDay() == null
                || request.getStartDate() == null
                || request.getChildId() <= 0
                || request.getSourceAccountId() <= 0
                || request.getDestinationAccountId() <= 0
                || request.getAmount().signum() <= 0
                || Objects.equals(
                request.getSourceAccountId(),
                request.getDestinationAccountId()
        )
                || request.getFrequency()
                != AutoTransferFrequency.MONTHLY
                || request.getTransferDay() < 1
                || request.getTransferDay() > 28
                || (
                request.getEndDate() != null
                        && request.getEndDate()
                        .isBefore(request.getStartDate())
        )) {
            throw new BusinessException(
                    ErrorCode.INVALID_AUTO_TRANSFER_SCHEDULE
            );
        }
    }

    private void validateChildAccess(
            Long memberId,
            Long childId
    ) {
        if (mapper.countChildAccess(childId, memberId) == 0) {
            throw new BusinessException(
                    ErrorCode.CHILD_ACCESS_DENIED
            );
        }
    }

    private void validateAccounts(
            Long memberId,
            CreateAutoTransferScheduleRequest request,
            AutoTransferAccountRow source,
            AutoTransferAccountRow destination
    ) {
        if (source == null || destination == null) {
            throw new BusinessException(
                    ErrorCode.FINANCIAL_ACCOUNT_NOT_FOUND
            );
        }

        if (!Objects.equals(
                source.getOwnerMemberId(),
                memberId
        )) {
            throw new BusinessException(
                    ErrorCode.FINANCIAL_ACCOUNT_ACCESS_DENIED
            );
        }

        boolean validSource =
                "PARENT".equals(source.getOwnerType())
                        && "DEMAND_DEPOSIT".equals(
                        source.getAccountProductType()
                )
                        && "ACTIVE".equals(source.getAccountStatus())
                        && "ACTIVE".equals(source.getLinkStatus());

        boolean validDestination =
                "CHILD".equals(destination.getOwnerType())
                        && Objects.equals(
                        destination.getChildId(),
                        request.getChildId()
                )
                        && "SAVINGS".equals(
                        destination.getAccountProductType()
                )
                        && "ACTIVE".equals(
                        destination.getAccountStatus()
                )
                        && "ACTIVE".equals(
                        destination.getLinkStatus()
                )
                        && destination.getFinancialGoalId() != null;

        if (!validSource || !validDestination) {
            throw new BusinessException(
                    ErrorCode.INVALID_AUTO_TRANSFER_SCHEDULE
            );
        }
    }

    private LocalDate calculateNextTransferDate(
            CreateAutoTransferScheduleRequest request
    ) {
        LocalDate today = LocalDate.now(clock);
        LocalDate baseDate = request.getStartDate()
                .isAfter(today)
                ? request.getStartDate()
                : today;

        LocalDate candidate = YearMonth.from(baseDate)
                .atDay(request.getTransferDay());

        if (candidate.isBefore(baseDate)) {
            candidate = YearMonth.from(baseDate)
                    .plusMonths(1)
                    .atDay(request.getTransferDay());
        }

        if (request.getEndDate() != null
                && candidate.isAfter(request.getEndDate())) {
            throw new BusinessException(
                    ErrorCode.INVALID_AUTO_TRANSFER_SCHEDULE
            );
        }

        return candidate;
    }

    private boolean sameRequest(
            Long memberId,
            CreateAutoTransferScheduleRequest request,
            AutoTransferScheduleRow existing
    ) {
        return Objects.equals(existing.getMemberId(), memberId)
                && Objects.equals(
                existing.getChildId(),
                request.getChildId()
        )
                && Objects.equals(
                existing.getSourceAccountId(),
                request.getSourceAccountId()
        )
                && Objects.equals(
                existing.getDestinationAccountId(),
                request.getDestinationAccountId()
        )
                && sameAmount(
                existing.getAmount(),
                request.getAmount()
        )
                && existing.getFrequency()
                == request.getFrequency()
                && Objects.equals(
                existing.getTransferDay(),
                request.getTransferDay()
        )
                && Objects.equals(
                existing.getStartDate(),
                request.getStartDate()
        )
                && Objects.equals(
                existing.getEndDate(),
                request.getEndDate()
        );
    }

    private boolean sameAmount(
            BigDecimal first,
            BigDecimal second
    ) {
        return first != null
                && second != null
                && first.compareTo(second) == 0;
    }

    private AutoTransferScheduleResponse toResponse(
            AutoTransferScheduleRow row
    ) {
        return new AutoTransferScheduleResponse(
                row.getAutoTransferScheduleId(),
                row.getFinancialGoalId(),
                row.getSourceAccountId(),
                row.getDestinationAccountId(),
                row.getAmount(),
                row.getFrequency(),
                row.getTransferDay(),
                row.getStartDate(),
                row.getEndDate(),
                toInstant(row.getNextTransferAt()),
                row.getLastTransferStatus(),
                toInstant(row.getLastTransferredAt()),
                row.getStatus(),
                toInstant(row.getCreatedAt())
        );
    }

    private java.time.Instant toInstant(
            LocalDateTime value
    ) {
        return value == null
                ? null
                : value.toInstant(ZoneOffset.UTC);
    }
}