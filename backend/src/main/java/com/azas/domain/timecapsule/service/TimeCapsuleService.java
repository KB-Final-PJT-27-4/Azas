package com.azas.domain.timecapsule.service;

import com.azas.domain.timecapsule.dto.CreateTimeCapsuleRequest;
import com.azas.domain.timecapsule.dto.CreateTimeCapsuleResponse;
import com.azas.domain.timecapsule.dto.TimeCapsuleListResponse;
import com.azas.domain.timecapsule.dto.TimeCapsuleResponse;
import com.azas.domain.timecapsule.dto.TimeCapsuleSummaryResponse;
import com.azas.domain.timecapsule.entity.TimeCapsule;
import com.azas.domain.timecapsule.entity.TimeCapsuleAccount;
import com.azas.domain.timecapsule.mapper.TimeCapsuleMapper;
import com.azas.domain.timecapsule.mapper.TimeCapsuleEntryMapper;
import com.azas.domain.timecapsule.mapper.TimeCapsuleExportMapper;
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
    private final TimeCapsuleExportMapper timeCapsuleExportMapper;
    private final TimeCapsuleObjectStorage timeCapsuleObjectStorage;

    @Transactional
    // [JMG] CAPSULE-1 부모 또는 자녀의 활성 계좌를 자녀 타임캡슐 보관함에 연결한다.
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
    // [JMG] CAPSULE-2 자녀의 타임캡슐 보관함을 카드 또는 캘린더 목록으로 조회한다.
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

    @Transactional(readOnly = true)
    // [JMG] CAPSULE-3 부모 권한을 확인한 뒤 타임캡슐 보관함 상세를 조회한다.
    public TimeCapsuleResponse getTimeCapsule(
            long requesterMemberId,
            long timeCapsuleId
    ) {
        TimeCapsule timeCapsule =
                getAccessibleTimeCapsuleOrThrow(
                        requesterMemberId,
                        timeCapsuleId
                );

        return TimeCapsuleResponse.from(timeCapsule);
    }

    @Transactional
    // [JMG] CAPSULE-6 부모·보호자 요청에 따라 보관함 하위 S3 객체와 DB 데이터를 영구 삭제한다.
    public void deleteTimeCapsule(
            long requesterMemberId,
            long timeCapsuleId
    ) {
        TimeCapsule timeCapsule = getAccessibleTimeCapsuleForUpdateOrThrow(
                requesterMemberId,
                timeCapsuleId
        );
        timeCapsuleEntryMapper.lockByTimeCapsuleId(timeCapsuleId);

        Set<String> objectKeys = new LinkedHashSet<>();
        addObjectKeys(objectKeys,
                timeCapsuleMediaMapper
                        .findObjectKeysByTimeCapsuleIdForUpdate(timeCapsuleId));
        addObjectKeys(objectKeys,
                timeCapsuleExportMapper
                        .findOutputObjectKeysByTimeCapsuleIdForUpdate(
                                timeCapsuleId
                        ));
        for (String objectKey : objectKeys) {
            timeCapsuleObjectStorage.deleteObject(objectKey);
        }

        timeCapsuleMediaMapper.deleteByTimeCapsuleId(timeCapsuleId);
        timeCapsuleEntryMapper.deleteByTimeCapsuleId(timeCapsuleId);
        timeCapsuleExportMapper.deleteByTimeCapsuleId(timeCapsuleId);
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

    // [JMG] CAPSULE-3 요청 부모가 접근 가능한 보관함을 조회하고 없으면 예외를 발생시킨다.
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

    // [JMG] CAPSULE-6 삭제 처리 중 엔트리 생성·수정과의 경합을 막도록 보관함 행을 잠근다.
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

    // [JMG] CAPSULE-6 null·공백·중복 객체 키를 제외해 같은 S3 객체를 한 번만 삭제한다.
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

    // [JMG] CAPSULE-2 요청 회원의 자녀 접근 권한을 검증하고 존재 여부 노출을 막는다.
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

    // [JMG] CAPSULE-2 상태 필터 문자열을 ERD 상태값으로 변환한다.
    // [JMG] CAPSULE-2 요청 페이지 크기를 기본값과 최대값 범위로 정규화한다.
    // [JMG] CAPSULE-2 카드 조회에 캘린더 기간 파라미터가 섞이지 않도록 검증한다.
    // [JMG] CAPSULE-2 캘린더 조회에 필요한 연도와 월을 검증해 월 범위를 생성한다.
    // [JMG] CAPSULE-2 조회 결과를 keyset pagination 응답 형태로 변환한다.
}
