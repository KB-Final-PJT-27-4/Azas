package com.azas.domain.timecapsule.service;

import com.azas.domain.timecapsule.dto.CreateTimeCapsuleRequest;
import com.azas.domain.timecapsule.dto.TimeCapsuleCursor;
import com.azas.domain.timecapsule.dto.TimeCapsuleListResponse;
import com.azas.domain.timecapsule.dto.TimeCapsuleResponse;
import com.azas.domain.timecapsule.dto.TimeCapsuleSearchCondition;
import com.azas.domain.timecapsule.dto.TimeCapsuleSummaryResponse;
import com.azas.domain.timecapsule.entity.TimeCapsule;
import com.azas.domain.timecapsule.entity.TimeCapsuleAccount;
import com.azas.domain.timecapsule.entity.TimeCapsuleStatus;
import com.azas.domain.timecapsule.entity.TimeCapsuleView;
import com.azas.domain.timecapsule.mapper.TimeCapsuleMapper;
import com.azas.domain.timecapsule.mapper.TimeCapsuleEntryMapper;
import com.azas.domain.timecapsule.mapper.TimeCapsuleExportMapper;
import com.azas.domain.timecapsule.mapper.TimeCapsuleMediaMapper;
import com.azas.domain.timecapsule.storage.TimeCapsuleObjectStorage;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimeCapsuleService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 50;

    private final TimeCapsuleMapper timeCapsuleMapper;
    private final TimeCapsuleEntryMapper timeCapsuleEntryMapper;
    private final TimeCapsuleMediaMapper timeCapsuleMediaMapper;
    private final TimeCapsuleExportMapper timeCapsuleExportMapper;
    private final TimeCapsuleObjectStorage timeCapsuleObjectStorage;

    @Transactional
    // [JMG] CAPSULE-1 활성 적금 계좌에 연결된 타임캡슐 보관함을 생성한다.
    public TimeCapsuleResponse createTimeCapsule(
            long requesterMemberId,
            long financialAccountId,
            CreateTimeCapsuleRequest request
    ) {
        TimeCapsuleAccount account =
                getAccessibleTimeCapsuleAccountOrThrow(
                        requesterMemberId,
                        financialAccountId
                );

        if (!account.isEligibleSavingsAccount()) {
            throw new BusinessException(
                    ErrorCode.INELIGIBLE_TIME_CAPSULE_ACCOUNT
            );
        }

        if (timeCapsuleMapper.findByFinancialAccountId(
                financialAccountId
        ) != null) {
            throw new BusinessException(
                    ErrorCode.DUPLICATE_TIME_CAPSULE
            );
        }

        TimeCapsule timeCapsule = TimeCapsule.create(
                account.getChildId(),
                financialAccountId,
                request.getTitle().trim(),
                account.getMaturityDate()
        );

        try {
            timeCapsuleMapper.insert(timeCapsule);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(
                    ErrorCode.DUPLICATE_TIME_CAPSULE,
                    exception
            );
        }

        return TimeCapsuleResponse.from(
                getAccessibleTimeCapsuleOrThrow(
                        requesterMemberId,
                        timeCapsule.getTimeCapsuleId()
                )
        );
    }

    @Transactional(readOnly = true)
    // [JMG] CAPSULE-2 자녀의 타임캡슐 보관함을 카드 또는 캘린더 목록으로 조회한다.
    public TimeCapsuleListResponse getTimeCapsules(
            long requesterMemberId,
            long childId,
            String viewValue,
            String statusValue,
            String encodedCursor,
            Integer size,
            Integer year,
            Integer month
    ) {
        assertParentAccess(requesterMemberId, childId);

        TimeCapsuleView view = TimeCapsuleView.from(viewValue);
        TimeCapsuleStatus status = parseStatus(statusValue);
        TimeCapsuleCursor cursor = TimeCapsuleCursor.decode(
                encodedCursor,
                view
        );
        int pageSize = normalizePageSize(size);

        List<TimeCapsule> timeCapsules;
        if (view == TimeCapsuleView.CARD) {
            assertCardQueryHasNoCalendarPeriod(year, month);
            timeCapsules = timeCapsuleMapper.findCardSummaries(
                    TimeCapsuleSearchCondition.forCard(
                            childId,
                            status,
                            cursor,
                            pageSize + 1
                    )
            );
        } else {
            YearMonth yearMonth = getYearMonthOrThrow(year, month);
            timeCapsules = timeCapsuleMapper.findCalendarSummaries(
                    TimeCapsuleSearchCondition.forCalendar(
                            childId,
                            status,
                            cursor,
                            pageSize + 1,
                            yearMonth.atDay(1).atStartOfDay(),
                            yearMonth.plusMonths(1)
                                    .atDay(1)
                                    .atStartOfDay()
                    )
            );
        }

        return createListResponse(
                timeCapsules,
                pageSize,
                view
        );
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

    // [JMG] CAPSULE-1 요청 부모가 접근 가능한 금융 계좌의 생성 가능 정보를 조회한다.
    private TimeCapsuleAccount getAccessibleTimeCapsuleAccountOrThrow(
            long requesterMemberId,
            long financialAccountId
    ) {
        TimeCapsuleAccount account =
                timeCapsuleMapper.findAccessibleAccountById(
                        financialAccountId,
                        requesterMemberId
                );

        if (account == null) {
            throw new BusinessException(
                    ErrorCode.FINANCIAL_ACCOUNT_NOT_FOUND
            );
        }

        return account;
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
        if (!timeCapsuleMapper.existsActiveParentRelation(
                requesterMemberId,
                childId
        )) {
            throw new BusinessException(
                    ErrorCode.CHILD_NOT_FOUND
            );
        }
    }

    // [JMG] CAPSULE-2 상태 필터 문자열을 ERD 상태값으로 변환한다.
    private TimeCapsuleStatus parseStatus(String statusValue) {
        if (statusValue == null || statusValue.isBlank()) {
            return null;
        }

        return TimeCapsuleStatus.from(statusValue);
    }

    // [JMG] CAPSULE-2 요청 페이지 크기를 기본값과 최대값 범위로 정규화한다.
    private int normalizePageSize(Integer size) {
        if (size == null) {
            return DEFAULT_PAGE_SIZE;
        }

        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessException(ErrorCode.BADREQUEST);
        }

        return size;
    }

    // [JMG] CAPSULE-2 카드 조회에 캘린더 기간 파라미터가 섞이지 않도록 검증한다.
    private void assertCardQueryHasNoCalendarPeriod(
            Integer year,
            Integer month
    ) {
        if (year != null || month != null) {
            throw new BusinessException(ErrorCode.BADREQUEST);
        }
    }

    // [JMG] CAPSULE-2 캘린더 조회에 필요한 연도와 월을 검증해 월 범위를 생성한다.
    private YearMonth getYearMonthOrThrow(
            Integer year,
            Integer month
    ) {
        if (year == null || month == null) {
            throw new BusinessException(ErrorCode.BADREQUEST);
        }

        try {
            return YearMonth.of(year, month);
        } catch (DateTimeException exception) {
            throw new BusinessException(ErrorCode.BADREQUEST);
        }
    }

    // [JMG] CAPSULE-2 조회 결과를 keyset pagination 응답 형태로 변환한다.
    private TimeCapsuleListResponse createListResponse(
            List<TimeCapsule> timeCapsules,
            int pageSize,
            TimeCapsuleView view
    ) {
        boolean hasNext = timeCapsules.size() > pageSize;
        List<TimeCapsule> pageItems = hasNext
                ? timeCapsules.subList(0, pageSize)
                : timeCapsules;
        String nextCursor = hasNext
                ? TimeCapsuleCursor.encode(
                view,
                pageItems.get(pageItems.size() - 1)
        )
                : null;

        List<TimeCapsuleSummaryResponse> summaries =
                pageItems.stream()
                        .map(timeCapsule ->
                                TimeCapsuleSummaryResponse.from(
                                        timeCapsule,
                                        LocalDate.now()
                                )
                        )
                        .collect(Collectors.toList());

        return new TimeCapsuleListResponse(
                new ArrayList<>(summaries),
                nextCursor,
                hasNext
        );
    }
}
