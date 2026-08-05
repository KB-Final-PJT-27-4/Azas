package com.azas.domain.timecapsule.service;

import com.azas.domain.timecapsule.dto.TimeCapsuleEntryAutoCreationResult;
import com.azas.domain.timecapsule.dto.TimeCapsuleEntryListResponse;
import com.azas.domain.timecapsule.dto.TimeCapsuleEntrySummaryResponse;
import com.azas.domain.timecapsule.entity.TimeCapsule;
import com.azas.domain.timecapsule.entity.TimeCapsuleEntry;
import com.azas.domain.timecapsule.entity.TimeCapsuleEntryTransaction;
import com.azas.domain.timecapsule.entity.TimeCapsuleStatus;
import com.azas.domain.timecapsule.mapper.TimeCapsuleEntryMapper;
import com.azas.domain.timecapsule.mapper.TimeCapsuleMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimeCapsuleEntryService {

    private final TimeCapsuleMapper timeCapsuleMapper;
    private final TimeCapsuleEntryMapper timeCapsuleEntryMapper;

    @Transactional(readOnly = true)
    // [JMG] CAPSULE-4 부모 권한을 확인한 뒤 삭제되지 않은 타임캡슐 엔트리 목록을 조회한다.
    public TimeCapsuleEntryListResponse getTimeCapsuleEntries(
            long requesterMemberId,
            long timeCapsuleId
    ) {
        getAccessibleTimeCapsuleOrThrow(requesterMemberId, timeCapsuleId);

        List<TimeCapsuleEntrySummaryResponse> entries =
                timeCapsuleEntryMapper
                        .findVisibleEntriesByTimeCapsuleId(timeCapsuleId)
                        .stream()
                        .map(TimeCapsuleEntrySummaryResponse::from)
                        .collect(Collectors.toList());

        return new TimeCapsuleEntryListResponse(entries);
    }

    @Transactional
    // [JMG] CAPSULE-5 성공한 적금 이체의 CREDIT 거래를 기준으로 엔트리 초안을 멱등하게 자동 생성한다.
    public Optional<TimeCapsuleEntryAutoCreationResult>
    createDraftForSuccessfulSavingsTransfer(
            long requesterMemberId,
            long financialAccountId,
            long accountTransactionId
    ) {
        TimeCapsule timeCapsule =
                timeCapsuleMapper.findByFinancialAccountIdForUpdate(
                        financialAccountId
                );

        if (timeCapsule == null
                || timeCapsule.getStatus() != TimeCapsuleStatus.COLLECTING) {
            return Optional.empty();
        }

        TimeCapsuleEntry existingEntry =
                timeCapsuleEntryMapper.findByTimeCapsuleAndTransactionId(
                        timeCapsule.getTimeCapsuleId(),
                        accountTransactionId
                );
        if (existingEntry != null) {
            return Optional.of(
                    TimeCapsuleEntryAutoCreationResult.from(existingEntry)
            );
        }

        TimeCapsuleEntryTransaction transaction =
                getCreditTransactionForSavingsAccountOrThrow(
                        financialAccountId,
                        accountTransactionId
                );
        assertEligibleContributionTransaction(transaction);

        TimeCapsuleEntry entry =
                TimeCapsuleEntry.createDraftForSuccessfulTransfer(
                        timeCapsule.getTimeCapsuleId(),
                        requesterMemberId,
                        transaction
                );

        TimeCapsuleEntry savedEntry = insertEntryIdempotently(entry);

        if (savedEntry != entry) {
            return Optional.of(
                    TimeCapsuleEntryAutoCreationResult.from(savedEntry)
            );
        }

        if (timeCapsuleEntryMapper
                .increaseEntryCountAndRefreshLatestEntry(entry) != 1) {
            throw new BusinessException(
                    ErrorCode.TIME_CAPSULE_ENTRY_CREATION_NOT_ALLOWED
            );
        }

        return Optional.of(TimeCapsuleEntryAutoCreationResult.from(entry));
    }

    // [JMG] CAPSULE-4 부모에게 접근 가능한 보관함만 반환해 보관함 존재 여부를 보호한다.
    private TimeCapsule getAccessibleTimeCapsuleOrThrow(
            long requesterMemberId,
            long timeCapsuleId
    ) {
        TimeCapsule timeCapsule = timeCapsuleMapper.findAccessibleById(
                timeCapsuleId,
                requesterMemberId
        );

        if (timeCapsule == null) {
            throw new BusinessException(ErrorCode.TIME_CAPSULE_NOT_FOUND);
        }

        return timeCapsule;
    }

    // [JMG] CAPSULE-5 대상 적금 계좌에 실제로 기록된 거래만 조회해 임의 거래 연결을 차단한다.
    private TimeCapsuleEntryTransaction
    getCreditTransactionForSavingsAccountOrThrow(
            long financialAccountId,
            long accountTransactionId
    ) {
        TimeCapsuleEntryTransaction transaction =
                timeCapsuleEntryMapper.findTransactionByFinancialAccountId(
                        financialAccountId,
                        accountTransactionId
                );

        if (transaction == null) {
            throw new BusinessException(
                    ErrorCode.ACCOUNT_TRANSACTION_NOT_FOUND
            );
        }

        return transaction;
    }

    // [JMG] CAPSULE-5 출금·0원·음수 거래를 타임캡슐 저축 기록으로 사용하지 못하게 검증한다.
    private void assertEligibleContributionTransaction(
            TimeCapsuleEntryTransaction transaction
    ) {
        if (!transaction.isCredit() || !transaction.hasPositiveAmount()) {
            throw new BusinessException(
                    ErrorCode.INELIGIBLE_TIME_CAPSULE_TRANSACTION
            );
        }
    }

    // [JMG] CAPSULE-5 DB 고유 제약과 재조회로 이체 이벤트 재시도에도 엔트리를 하나만 유지한다.
    private TimeCapsuleEntry insertEntryIdempotently(TimeCapsuleEntry entry) {
        try {
            if (timeCapsuleEntryMapper.insert(entry) != 1) {
                throw new BusinessException(
                        ErrorCode.TIME_CAPSULE_ENTRY_CREATION_NOT_ALLOWED
                );
            }
            return entry;
        } catch (DuplicateKeyException exception) {
            TimeCapsuleEntry existingEntry =
                    timeCapsuleEntryMapper.findByTimeCapsuleAndTransactionId(
                            entry.getTimeCapsuleId(),
                            entry.getAccountTransactionId()
                    );
            if (existingEntry != null) {
                return existingEntry;
            }

            throw new BusinessException(
                    ErrorCode.DUPLICATE_TIME_CAPSULE_ENTRY,
                    exception
            );
        }
    }
}
