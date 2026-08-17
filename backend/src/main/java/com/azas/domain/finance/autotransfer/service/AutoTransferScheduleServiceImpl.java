package com.azas.domain.finance.autotransfer.service;

import com.azas.domain.finance.autotransfer.dto.*;
import com.azas.domain.finance.autotransfer.entity.AutoTransferFrequency;
import com.azas.domain.finance.autotransfer.entity.AutoTransferScheduleStatus;
import com.azas.domain.finance.autotransfer.mapper.AutoTransferScheduleMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.azas.domain.finance.autotransfer.dto.AutoTransferScheduleListItemResponse;
import com.azas.domain.finance.autotransfer.dto.AutoTransferScheduleListQuery;
import com.azas.domain.finance.autotransfer.dto.AutoTransferScheduleListResponse;
import com.azas.domain.finance.autotransfer.dto.AutoTransferScheduleListRow;
import com.azas.domain.finance.autotransfer.dto.AutoTransferScheduleDetailResponse;
import com.azas.domain.finance.autotransfer.dto.AutoTransferScheduleDetailRow;
import com.azas.domain.finance.autotransfer.entity.AutoTransferAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

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
        return calculateNextTransferDate(
                request.getStartDate(),
                request.getEndDate(),
                request.getTransferDay()
        );
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

    @Override
    @Transactional(readOnly = true)
    public AutoTransferScheduleListResponse getSchedules(
            Long memberId,
            Long childId,
            String status,
            String cursor,
            Integer size
    ) {
        if (childId == null || childId <= 0) {
            throw new BusinessException(ErrorCode.BADREQUEST);
        }

        validateChildAccess(memberId, childId);

        AutoTransferScheduleStatus parsedStatus =
                parseScheduleStatus(status);
        Long cursorId = parseCursor(cursor);
        int pageSize = normalizePageSize(size);

        AutoTransferScheduleListQuery query =
                new AutoTransferScheduleListQuery(
                        childId,
                        parsedStatus,
                        cursorId,
                        pageSize + 1
                );

        List<AutoTransferScheduleListRow> rows =
                mapper.findSchedules(query);

        boolean hasNext = rows.size() > pageSize;

        List<AutoTransferScheduleListRow> pageRows =
                hasNext
                        ? new ArrayList<>(
                        rows.subList(0, pageSize)
                )
                        : rows;

        List<AutoTransferScheduleListItemResponse> items =
                pageRows.stream()
                        .map(this::toListItemResponse)
                        .toList();

        String nextCursor =
                hasNext && !pageRows.isEmpty()
                        ? String.valueOf(
                        pageRows.get(
                                pageRows.size() - 1
                        ).getAutoTransferScheduleId()
                )
                        : null;

        return new AutoTransferScheduleListResponse(
                items,
                nextCursor,
                hasNext
        );
    }

    private AutoTransferScheduleStatus parseScheduleStatus(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return AutoTransferScheduleStatus.valueOf(
                    value.toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.BADREQUEST);
        }
    }

    private Long parseCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }

        try {
            long value = Long.parseLong(cursor);

            if (value <= 0) {
                throw new NumberFormatException();
            }

            return value;
        } catch (NumberFormatException exception) {
            throw new BusinessException(ErrorCode.BADREQUEST);
        }
    }

    private int normalizePageSize(Integer size) {
        if (size == null) {
            return DEFAULT_PAGE_SIZE;
        }

        if (size <= 0 || size > MAX_PAGE_SIZE) {
            throw new BusinessException(ErrorCode.BADREQUEST);
        }

        return size;
    }

    private AutoTransferScheduleListItemResponse
    toListItemResponse(
            AutoTransferScheduleListRow row
    ) {
        return new AutoTransferScheduleListItemResponse(
                row.getAutoTransferScheduleId(),
                row.getFinancialGoalId(),
                row.getGoalTitle(),
                row.getAmount(),
                row.getFrequency(),
                row.getTransferDay(),
                toInstant(row.getNextTransferAt()),
                row.getLastTransferStatus(),
                toInstant(row.getLastTransferredAt()),
                row.getStatus()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AutoTransferScheduleDetailResponse getScheduleDetail(
            Long memberId,
            Long scheduleId
    ) {
        if (scheduleId == null || scheduleId <= 0) {
            throw new BusinessException(ErrorCode.BADREQUEST);
        }

        AutoTransferScheduleDetailRow row =
                mapper.findScheduleDetail(scheduleId);

        if (row == null) {
            throw new BusinessException(
                    ErrorCode.AUTO_TRANSFER_SCHEDULE_NOT_FOUND
            );
        }

        validateChildAccess(memberId, row.getChildId());

        return toDetailResponse(row);
    }
    @Override
    @Transactional
    public AutoTransferScheduleDetailResponse updateSchedule(
            Long memberId,
            Long scheduleId,
            UpdateAutoTransferScheduleRequest request
    ) {
        if (scheduleId == null || scheduleId <= 0) {
            throw new BusinessException(ErrorCode.BADREQUEST);
        }

        if (request == null || request.getAction() == null) {
            throw new BusinessException(ErrorCode.BADREQUEST);
        }

        AutoTransferScheduleRow schedule =
                mapper.findScheduleForUpdate(scheduleId);

        if (schedule == null) {
            throw new BusinessException(
                    ErrorCode.AUTO_TRANSFER_SCHEDULE_NOT_FOUND
            );
        }

        validateChildAccess(
                memberId,
                schedule.getChildId()
        );

        /*
         * 다른 보호자가 목록·상세를 보는 것은 허용하지만,
         * 다른 보호자 계좌에서 출금되는 일정 변경은 허용하지 않는다.
         */
        if (!Objects.equals(
                schedule.getMemberId(),
                memberId
        )) {
            throw new BusinessException(
                    ErrorCode.AUTO_TRANSFER_SCHEDULE_ACCESS_DENIED
            );
        }

        if (schedule.getStatus()
                == AutoTransferScheduleStatus.ENDED
                || schedule.getStatus()
                == AutoTransferScheduleStatus.CANCELED) {
            throw new BusinessException(
                    ErrorCode.INVALID_AUTO_TRANSFER_STATUS_TRANSITION
            );
        }

        BigDecimal amount = schedule.getAmount();
        Integer transferDay = schedule.getTransferDay();
        LocalDate endDate = schedule.getEndDate();
        LocalDateTime nextTransferAt =
                schedule.getNextTransferAt();
        AutoTransferScheduleStatus nextStatus =
                schedule.getStatus();

        switch (request.getAction()) {
            case UPDATE:
                validateUpdateAction(request);

                amount = request.getAmount() != null
                        ? request.getAmount()
                        : schedule.getAmount();

                transferDay =
                        request.getTransferDay() != null
                                ? request.getTransferDay()
                                : schedule.getTransferDay();

                endDate = request.isEndDatePresent()
                        ? request.getEndDate()
                        : schedule.getEndDate();

                validateUpdatedValues(
                        amount,
                        transferDay,
                        schedule.getStartDate(),
                        endDate
                );

                LocalDate nextDate =
                        calculateNextTransferDate(
                                schedule.getStartDate(),
                                endDate,
                                transferDay
                        );

                nextTransferAt = nextDate.atStartOfDay();

                if (mapper.countEquivalentScheduleExcludingId(
                        scheduleId,
                        memberId,
                        schedule.getChildId(),
                        schedule.getSourceAccountId(),
                        schedule.getDestinationAccountId(),
                        amount,
                        schedule.getFrequency().name(),
                        transferDay,
                        schedule.getStartDate(),
                        endDate
                ) > 0) {
                    throw new BusinessException(
                            ErrorCode.DUPLICATE_AUTO_TRANSFER_SCHEDULE
                    );
                }
                break;

            case PAUSE:
                validateStatusOnlyAction(request);

                if (schedule.getStatus()
                        != AutoTransferScheduleStatus.ACTIVE) {
                    throw new BusinessException(
                            ErrorCode.INVALID_AUTO_TRANSFER_STATUS_TRANSITION
                    );
                }

                nextStatus =
                        AutoTransferScheduleStatus.PAUSED;

                // 예정일은 화면 표시를 위해 보존한다.
                nextTransferAt =
                        schedule.getNextTransferAt();
                break;

            case RESUME:
                validateStatusOnlyAction(request);

                if (schedule.getStatus()
                        != AutoTransferScheduleStatus.PAUSED) {
                    throw new BusinessException(
                            ErrorCode.INVALID_AUTO_TRANSFER_STATUS_TRANSITION
                    );
                }

                nextStatus =
                        AutoTransferScheduleStatus.ACTIVE;

                nextTransferAt =
                        calculateNextTransferDate(
                                schedule.getStartDate(),
                                schedule.getEndDate(),
                                schedule.getTransferDay()
                        ).atStartOfDay();
                break;

            default:
                throw new BusinessException(ErrorCode.BADREQUEST);
        }

        UpdateAutoTransferScheduleCommand command =
                new UpdateAutoTransferScheduleCommand(
                        scheduleId,
                        amount,
                        transferDay,
                        endDate,
                        nextTransferAt,
                        nextStatus,
                        LocalDateTime.now(clock)
                );

        if (mapper.updateSchedule(command) != 1) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR
            );
        }

        AutoTransferScheduleDetailRow updated =
                mapper.findScheduleDetail(scheduleId);

        if (updated == null) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR
            );
        }

        return toDetailResponse(updated);
    }

    private void validateUpdateAction(
            UpdateAutoTransferScheduleRequest request
    ) {
        boolean hasUpdateField =
                request.getAmount() != null
                        || request.getTransferDay() != null
                        || request.isEndDatePresent();

        if (!hasUpdateField) {
            throw new BusinessException(ErrorCode.BADREQUEST);
        }
    }

    private void validateStatusOnlyAction(
            UpdateAutoTransferScheduleRequest request
    ) {
        boolean hasUpdateField =
                request.getAmount() != null
                        || request.getTransferDay() != null
                        || request.isEndDatePresent();

        if (hasUpdateField) {
            throw new BusinessException(ErrorCode.BADREQUEST);
        }
    }

    private void validateUpdatedValues(
            BigDecimal amount,
            Integer transferDay,
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (amount == null
                || amount.signum() <= 0
                || transferDay == null
                || transferDay < 1
                || transferDay > 28
                || startDate == null
                || (
                endDate != null
                        && endDate.isBefore(startDate)
        )) {
            throw new BusinessException(
                    ErrorCode.INVALID_AUTO_TRANSFER_SCHEDULE
            );
        }
    }

    private LocalDate calculateNextTransferDate(
            LocalDate startDate,
            LocalDate endDate,
            Integer transferDay
    ) {
        LocalDate today = LocalDate.now(clock);

        LocalDate baseDate =
                startDate.isAfter(today)
                        ? startDate
                        : today;

        LocalDate candidate =
                YearMonth.from(baseDate)
                        .atDay(transferDay);

        if (candidate.isBefore(baseDate)) {
            candidate =
                    YearMonth.from(baseDate)
                            .plusMonths(1)
                            .atDay(transferDay);
        }

        if (endDate != null
                && candidate.isAfter(endDate)) {
            throw new BusinessException(
                    ErrorCode.INVALID_AUTO_TRANSFER_SCHEDULE
            );
        }

        return candidate;
    }
    private AutoTransferScheduleDetailResponse toDetailResponse(
            AutoTransferScheduleDetailRow row
    ) {
        return new AutoTransferScheduleDetailResponse(
                row.getAutoTransferScheduleId(),
                row.getChildId(),
                row.getFinancialGoalId(),
                row.getGoalTitle(),
                row.getSourceAccountId(),
                row.getSourceAccountName(),
                row.getDestinationAccountId(),
                row.getDestinationAccountName(),
                row.getAmount(),
                row.getFrequency(),
                row.getTransferDay(),
                row.getStartDate(),
                row.getEndDate(),
                toInstant(row.getNextTransferAt()),
                row.getLastTransferId(),
                row.getLastTransferStatus(),
                row.getLastFailureCode(),
                row.getLastFailureMessage(),
                toInstant(row.getLastTransferredAt()),
                row.getStatus(),
                toInstant(row.getCreatedAt()),
                toInstant(row.getUpdatedAt())
        );
    }

}