package com.azas.domain.timecapsule.service;

import com.azas.domain.timecapsule.dto.CreateTimeCapsuleRequest;
import com.azas.domain.timecapsule.dto.CreateTimeCapsuleResponse;
import com.azas.domain.timecapsule.dto.TimeCapsuleListResponse;
import com.azas.domain.timecapsule.dto.TimeCapsuleSummaryResponse;
import com.azas.domain.timecapsule.entity.TimeCapsule;
import com.azas.domain.timecapsule.entity.TimeCapsuleAccount;
import com.azas.domain.timecapsule.mapper.TimeCapsuleMapper;
import com.azas.domain.timecapsule.mapper.TimeCapsuleEntryMapper;
import com.azas.domain.timecapsule.mapper.TimeCapsuleMediaMapper;
import com.azas.domain.timecapsule.storage.TimeCapsuleObjectStorage;
import com.azas.domain.member.entity.Member;
import com.azas.domain.member.entity.MemberStatus;
import com.azas.domain.member.entity.MemberType;
import com.azas.domain.member.mapper.MemberMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimeCapsuleService {

    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");

    private final MemberMapper memberMapper;
    private final TimeCapsuleMapper timeCapsuleMapper;
    private final TimeCapsuleEntryMapper timeCapsuleEntryMapper;
    private final TimeCapsuleMediaMapper timeCapsuleMediaMapper;
    private final TimeCapsuleObjectStorage timeCapsuleObjectStorage;

    @Transactional
    public CreateTimeCapsuleResponse createTimeCapsule(
            long requesterMemberId,
            long childId,
            CreateTimeCapsuleRequest request
    ) {
        validateCreateRequest(childId, request);
        assertParentAccess(requesterMemberId, childId);

        long financialAccountId = request.getFinancialAccountId();
        TimeCapsuleAccount account = getTimeCapsuleAccountOrThrow(
                financialAccountId
        );
        assertAccountAccess(requesterMemberId, childId, account);

        if (!account.isEligibleTimeCapsuleAccount()) {
            throw new BusinessException(
                    ErrorCode.INELIGIBLE_TIME_CAPSULE_ACCOUNT
            );
        }

        if (timeCapsuleMapper.findByChildIdAndFinancialAccountId(
                childId,
                financialAccountId
        ) != null) {
            throw new BusinessException(
                    ErrorCode.DUPLICATE_TIME_CAPSULE
            );
        }

        TimeCapsule timeCapsule = TimeCapsule.create(
                childId,
                financialAccountId,
                account.getAccountName(),
                request.getReleaseDate()
        );

        try {
            timeCapsuleMapper.insert(timeCapsule);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(
                    ErrorCode.DUPLICATE_TIME_CAPSULE,
                    exception
            );
        }

        return CreateTimeCapsuleResponse.from(
                getAccessibleTimeCapsuleOrThrow(
                        requesterMemberId,
                        timeCapsule.getTimeCapsuleId()
                ),
                account
        );
    }

    @Transactional(readOnly = true)
    public TimeCapsuleListResponse getTimeCapsules(
            long requesterMemberId,
            long childId
    ) {
        if (childId < 1) {
            throw new BusinessException(ErrorCode.BADREQUEST);
        }
        assertParentAccess(requesterMemberId, childId);

        List<TimeCapsuleSummaryResponse> summaries = timeCapsuleMapper
                .findSummariesByChildId(childId)
                .stream()
                .map(timeCapsule -> TimeCapsuleSummaryResponse.from(
                        timeCapsule,
                        LocalDate.now(SERVICE_ZONE)
                ))
                .collect(Collectors.toList());

        return new TimeCapsuleListResponse(summaries);
    }

    @Transactional
    public void deleteTimeCapsule(
            long requesterMemberId,
            long timeCapsuleId
    ) {
        if (timeCapsuleId < 1) {
            throw new BusinessException(ErrorCode.BADREQUEST);
        }

        TimeCapsule timeCapsule = getAccessibleTimeCapsuleForUpdateOrThrow(
                requesterMemberId,
                timeCapsuleId
        );
        timeCapsuleEntryMapper.lockByTimeCapsuleId(timeCapsuleId);

        Set<String> objectKeys = new LinkedHashSet<>();
        addObjectKeys(objectKeys,
                timeCapsuleMediaMapper
                        .findObjectKeysByTimeCapsuleIdForUpdate(timeCapsuleId));
        for (String objectKey : objectKeys) {
            timeCapsuleObjectStorage.deleteObject(objectKey);
        }

        timeCapsuleMediaMapper.deleteByTimeCapsuleId(timeCapsuleId);
        timeCapsuleEntryMapper.deleteByTimeCapsuleId(timeCapsuleId);
        if (timeCapsuleMapper.deleteById(
                timeCapsule.getTimeCapsuleId()
        ) != 1) {
            throw new BusinessException(ErrorCode.TIME_CAPSULE_NOT_FOUND);
        }
    }

    private TimeCapsuleAccount getTimeCapsuleAccountOrThrow(
            long financialAccountId
    ) {
        TimeCapsuleAccount account =
                timeCapsuleMapper.findAccountById(financialAccountId);

        if (account == null) {
            throw new BusinessException(
                    ErrorCode.FINANCIAL_ACCOUNT_NOT_FOUND
            );
        }

        return account;
    }

    private void validateCreateRequest(
            long childId,
            CreateTimeCapsuleRequest request
    ) {
        if (childId < 1
                || request == null
                || request.getFinancialAccountId() == null
                || request.getFinancialAccountId() < 1) {
            throw new BusinessException(ErrorCode.BADREQUEST);
        }

        LocalDate releaseDate = request.getReleaseDate();
        if (releaseDate != null
                && !releaseDate.isAfter(LocalDate.now(SERVICE_ZONE))) {
            throw new BusinessException(ErrorCode.BADREQUEST);
        }
    }

    private void assertAccountAccess(
            long requesterMemberId,
            long childId,
            TimeCapsuleAccount account
    ) {
        if (account.isParentOwnedBy(requesterMemberId)
                || account.isOwnedByChild(childId)) {
            return;
        }

        throw new BusinessException(
                ErrorCode.FINANCIAL_ACCOUNT_ACCESS_DENIED
        );
    }

    private TimeCapsule getAccessibleTimeCapsuleOrThrow(
            long requesterMemberId,
            long timeCapsuleId
    ) {
        TimeCapsule timeCapsule =
                timeCapsuleMapper.findAccessibleById(
                        timeCapsuleId,
                        requesterMemberId
                );

        if (timeCapsule == null) {
            throw new BusinessException(
                    ErrorCode.TIME_CAPSULE_NOT_FOUND
            );
        }

        return timeCapsule;
    }

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

    private void addObjectKeys(Set<String> destination, List<String> source) {
        if (source == null) {
            return;
        }

        for (String objectKey : source) {
            if (objectKey != null && !objectKey.isBlank()) {
                destination.add(objectKey);
            }
        }
    }

    private void assertParentAccess(
            long requesterMemberId,
            long childId
    ) {
        Member member = memberMapper.findById(requesterMemberId);
        if (member == null
                || member.getStatus() != MemberStatus.ACTIVE
                || member.getMemberType() != MemberType.PARENT) {
            throw new BusinessException(ErrorCode.PARENT_ACCESS_REQUIRED);
        }

        if (!timeCapsuleMapper.existsChildById(childId)) {
            throw new BusinessException(ErrorCode.CHILD_NOT_FOUND);
        }

        if (!timeCapsuleMapper.existsActiveParentRelation(
                requesterMemberId,
                childId
        )) {
            throw new BusinessException(ErrorCode.CHILD_ACCESS_DENIED);
        }
    }
}
