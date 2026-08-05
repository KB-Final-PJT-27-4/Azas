package com.azas.domain.timecapsule.service;

import com.azas.domain.timecapsule.dto.TimeCapsuleEntryAutoCreationResult;
import com.azas.domain.timecapsule.dto.TimeCapsuleEntryListResponse;
import com.azas.domain.timecapsule.dto.TimeCapsuleEntrySealResponse;
import com.azas.domain.timecapsule.dto.TimeCapsuleEntrySummaryResponse;
import com.azas.domain.timecapsule.dto.TimeCapsuleEntryUpdateResponse;
import com.azas.domain.timecapsule.dto.UpdateTimeCapsuleEntryRequest;
import com.azas.domain.timecapsule.entity.TimeCapsule;
import com.azas.domain.timecapsule.entity.TimeCapsuleEntry;
import com.azas.domain.timecapsule.entity.TimeCapsuleEntryTransaction;
import com.azas.domain.timecapsule.entity.TimeCapsuleMediaType;
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

    @Transactional
    // [JMG] CAPSULE-12 작성자 본인이 DRAFT 엔트리의 제목 또는 편지를 수정한다.
    public TimeCapsuleEntryUpdateResponse updateTimeCapsuleEntry(
            long requesterMemberId,
            long timeCapsuleEntryId,
            UpdateTimeCapsuleEntryRequest request
    ) {
        assertValidUpdateRequest(request);
        TimeCapsuleEntry entry = getOwnedTimeCapsuleEntryForUpdateOrThrow(
                requesterMemberId,
                timeCapsuleEntryId
        );
        assertDraftEntry(entry);

        if (timeCapsuleEntryMapper.updateDraftContent(
                timeCapsuleEntryId,
                request.getTrimmedTitle(),
                request.getTrimmedMessage()
        ) != 1) {
            throw new BusinessException(
                    ErrorCode.TIME_CAPSULE_ENTRY_MODIFICATION_NOT_ALLOWED
            );
        }

        return TimeCapsuleEntryUpdateResponse.from(
                getOwnedTimeCapsuleEntryOrThrow(
                        requesterMemberId,
                        timeCapsuleEntryId
                )
        );
    }

    @Transactional
    // [JMG] CAPSULE-15 미디어 조건을 충족한 DRAFT 엔트리를 작성자 본인이 봉인한다.
    public TimeCapsuleEntrySealResponse sealTimeCapsuleEntry(
            long requesterMemberId,
            long timeCapsuleEntryId
    ) {
        TimeCapsuleEntry entry = getOwnedTimeCapsuleEntryForUpdateOrThrow(
                requesterMemberId,
                timeCapsuleEntryId
        );
        assertDraftEntry(entry);
        assertMediaRequirementsForSeal(entry);

        if (timeCapsuleEntryMapper.sealDraftEntry(timeCapsuleEntryId) != 1) {
            throw new BusinessException(
                    ErrorCode.TIME_CAPSULE_ENTRY_MODIFICATION_NOT_ALLOWED
            );
        }

        return TimeCapsuleEntrySealResponse.from(
                getOwnedTimeCapsuleEntryOrThrow(
                        requesterMemberId,
                        timeCapsuleEntryId
                )
        );
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

    // [JMG] CAPSULE-12 작성자이면서 자녀와 연결된 부모에게만 엔트리를 노출한다.
    private TimeCapsuleEntry getOwnedTimeCapsuleEntryOrThrow(
            long requesterMemberId,
            long timeCapsuleEntryId
    ) {
        TimeCapsuleEntry entry = timeCapsuleEntryMapper.findOwnedById(
                timeCapsuleEntryId,
                requesterMemberId
        );
        if (entry == null) {
            throw new BusinessException(ErrorCode.TIME_CAPSULE_ENTRY_NOT_FOUND);
        }

        return entry;
    }

    // [JMG] CAPSULE-12 수정·봉인 직전 엔트리 행을 잠가 상태 변경 경쟁 조건을 방지한다.
    private TimeCapsuleEntry getOwnedTimeCapsuleEntryForUpdateOrThrow(
            long requesterMemberId,
            long timeCapsuleEntryId
    ) {
        TimeCapsuleEntry entry =
                timeCapsuleEntryMapper.findOwnedByIdForUpdate(
                        timeCapsuleEntryId,
                        requesterMemberId
                );
        if (entry == null) {
            throw new BusinessException(ErrorCode.TIME_CAPSULE_ENTRY_NOT_FOUND);
        }

        return entry;
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

    // [JMG] CAPSULE-12 빈 수정 요청과 공백뿐인 제목·편지를 400 오류로 차단한다.
    private void assertValidUpdateRequest(
            UpdateTimeCapsuleEntryRequest request
    ) {
        if (!request.hasUpdateField() || !request.hasOnlyValidText()) {
            throw new BusinessException(ErrorCode.BADREQUEST);
        }
    }

    // [JMG] CAPSULE-12 봉인 또는 삭제된 엔트리의 변경 시도를 상태 충돌 오류로 처리한다.
    private void assertDraftEntry(TimeCapsuleEntry entry) {
        if (!entry.isDraft()) {
            throw new BusinessException(
                    ErrorCode.TIME_CAPSULE_ENTRY_MODIFICATION_NOT_ALLOWED
            );
        }
    }

    // [JMG] CAPSULE-15 엔트리의 미디어 유형별 활성 개수와 업로드 완료 상태를 봉인 전에 검증한다.
    private void assertMediaRequirementsForSeal(TimeCapsuleEntry entry) {
        if (timeCapsuleEntryMapper.countPendingMediaByEntryId(
                entry.getTimeCapsuleEntryId()
        ) > 0) {
            throw new BusinessException(
                    ErrorCode.TIME_CAPSULE_ENTRY_MEDIA_REQUIREMENT_NOT_MET
            );
        }

        int activeImageCount =
                timeCapsuleEntryMapper.countActiveMediaByEntryIdAndType(
                        entry.getTimeCapsuleEntryId(),
                        TimeCapsuleMediaType.IMAGE
                );
        int activeVideoCount =
                timeCapsuleEntryMapper.countActiveMediaByEntryIdAndType(
                        entry.getTimeCapsuleEntryId(),
                        TimeCapsuleMediaType.VIDEO
                );

        boolean isValid = switch (entry.getMediaMode()) {
            case NONE -> activeImageCount == 0 && activeVideoCount == 0;
            case IMAGE -> activeImageCount >= 1
                    && activeImageCount <= 3
                    && activeVideoCount == 0;
            case VIDEO -> activeImageCount == 0 && activeVideoCount == 1;
        };
        if (!isValid) {
            throw new BusinessException(
                    ErrorCode.TIME_CAPSULE_ENTRY_MEDIA_REQUIREMENT_NOT_MET
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
