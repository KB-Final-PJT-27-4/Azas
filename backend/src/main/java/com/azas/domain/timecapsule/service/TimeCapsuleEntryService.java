package com.azas.domain.timecapsule.service;

import com.azas.domain.timecapsule.dto.CreateTimeCapsuleEntryRequest;
import com.azas.domain.timecapsule.dto.TimeCapsuleEntryListResponse;
import com.azas.domain.timecapsule.dto.TimeCapsuleEntryResponse;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimeCapsuleEntryService {

    private final TimeCapsuleMapper timeCapsuleMapper;
    private final TimeCapsuleEntryMapper timeCapsuleEntryMapper;

    @Transactional(readOnly = true)
    // [JMG] CAPSULE-4 부모 권한을 확인한 뒤 삭제되지 않은 타임캡슐 기록 목록을 조회한다.
    public TimeCapsuleEntryListResponse getTimeCapsuleEntries(
            long requesterMemberId,
            long timeCapsuleId
    ) {
        getAccessibleTimeCapsuleOrThrow(
                requesterMemberId,
                timeCapsuleId
        );

        List<TimeCapsuleEntrySummaryResponse> entries =
                timeCapsuleEntryMapper
                        .findVisibleEntriesByTimeCapsuleId(timeCapsuleId)
                        .stream()
                        .map(TimeCapsuleEntrySummaryResponse::from)
                        .collect(Collectors.toList());

        return new TimeCapsuleEntryListResponse(entries);
    }

    @Transactional
    // [JMG] CAPSULE-5 적금 입금 거래와 부모 메시지를 연결한 새 타임캡슐 기록을 생성한다.
    public TimeCapsuleEntryResponse createTimeCapsuleEntry(
            long requesterMemberId,
            long timeCapsuleId,
            CreateTimeCapsuleEntryRequest request
    ) {
        TimeCapsule timeCapsule =
                getAccessibleTimeCapsuleForUpdateOrThrow(
                        requesterMemberId,
                        timeCapsuleId
                );

        assertEntryCreationAllowed(timeCapsule);

        TimeCapsuleEntryTransaction transaction =
                getTransactionForTimeCapsuleOrThrow(
                        timeCapsuleId,
                        request.getAccountTransactionId()
                );
        assertCreditTransaction(transaction);

        TimeCapsuleEntry entry = TimeCapsuleEntry.create(
                timeCapsuleId,
                requesterMemberId,
                transaction,
                request.getTitle(),
                request.getMessage(),
                request.toMediaMode()
        );

        try {
            timeCapsuleEntryMapper.insert(entry);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(
                    ErrorCode.DUPLICATE_TIME_CAPSULE_ENTRY,
                    exception
            );
        }

        if (timeCapsuleEntryMapper
                .increaseEntryCountAndRefreshLatestEntry(entry) != 1) {
            throw new BusinessException(
                    ErrorCode.TIME_CAPSULE_ENTRY_CREATION_NOT_ALLOWED
            );
        }

        return TimeCapsuleEntryResponse.from(entry);
    }

    // [JMG] CAPSULE-4 요청한 부모에게 공개된 보관함만 조회해 보관함 존재 여부를 보호한다.
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

    // [JMG] CAPSULE-5 생성 동안 상태 확인과 집계 갱신이 분리되지 않도록 보관함 행을 잠가 조회한다.
    private TimeCapsule getAccessibleTimeCapsuleForUpdateOrThrow(
            long requesterMemberId,
            long timeCapsuleId
    ) {
        TimeCapsule timeCapsule =
                timeCapsuleMapper.findAccessibleByIdForUpdate(
                        timeCapsuleId,
                        requesterMemberId
                );

        if (timeCapsule == null) {
            throw new BusinessException(ErrorCode.TIME_CAPSULE_NOT_FOUND);
        }

        return timeCapsule;
    }

    // [JMG] CAPSULE-5 공개 또는 보관 처리된 보관함에 새 기록이 추가되지 않도록 상태를 검증한다.
    private void assertEntryCreationAllowed(TimeCapsule timeCapsule) {
        if (timeCapsule.getStatus() != TimeCapsuleStatus.COLLECTING) {
            throw new BusinessException(
                    ErrorCode.TIME_CAPSULE_ENTRY_CREATION_NOT_ALLOWED
            );
        }
    }

    // [JMG] CAPSULE-5 보관함의 적금 계좌에 연결된 거래만 찾아 다른 계좌 거래의 존재를 노출하지 않는다.
    private TimeCapsuleEntryTransaction getTransactionForTimeCapsuleOrThrow(
            long timeCapsuleId,
            long accountTransactionId
    ) {
        TimeCapsuleEntryTransaction transaction =
                timeCapsuleEntryMapper.findTransactionByTimeCapsuleAndId(
                        timeCapsuleId,
                        accountTransactionId
                );

        if (transaction == null) {
            throw new BusinessException(
                    ErrorCode.ACCOUNT_TRANSACTION_NOT_FOUND
            );
        }

        return transaction;
    }

    // [JMG] CAPSULE-5 적금 계좌의 출금 거래가 기록에 연결되는 것을 차단한다.
    private void assertCreditTransaction(
            TimeCapsuleEntryTransaction transaction
    ) {
        if (!transaction.isCredit()) {
            throw new BusinessException(
                    ErrorCode.INELIGIBLE_TIME_CAPSULE_TRANSACTION
            );
        }
    }
}
