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
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimeCapsuleService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 50;

    private final TimeCapsuleMapper timeCapsuleMapper;

    @Transactional
    // [JMG] CAPSULE-1 활성 적금 계좌에 연결된 타임캡슐 보관함을 생성한다.
    public TimeCapsuleResponse createTimeCapsule(
            long requesterMemberId,
            long financialAccountId,
            CreateTimeCapsuleRequest request
    ) {
        TimeCapsuleAccount account =
                getTimeCapsuleAccountOrThrow(financialAccountId);

        if (!account.isEligibleSavingsAccount()) {
            throw new BusinessException(
                    ErrorCode.INELIGIBLE_TIME_CAPSULE_ACCOUNT
            );
        }

        assertParentAccess(
                requesterMemberId,
                account.getChildId()
        );

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
                getTimeCapsuleOrThrow(
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
        assertChildExists(childId);
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
                getTimeCapsuleOrThrow(timeCapsuleId);

        assertParentAccess(
                requesterMemberId,
                timeCapsule.getChildId()
        );

        return TimeCapsuleResponse.from(timeCapsule);
    }

    // [JMG] CAPSULE-1 대상 금융 계좌의 타임캡슐 생성 가능 정보를 조회한다.
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

    // [JMG] CAPSULE-1~3 타임캡슐 보관함을 조회하고 존재하지 않으면 예외를 발생시킨다.
    private TimeCapsule getTimeCapsuleOrThrow(long timeCapsuleId) {
        TimeCapsule timeCapsule =
                timeCapsuleMapper.findById(timeCapsuleId);

        if (timeCapsule == null) {
            throw new BusinessException(
                    ErrorCode.TIME_CAPSULE_NOT_FOUND
            );
        }

        return timeCapsule;
    }

    // [JMG] CAPSULE-2 목록 대상 자녀의 존재 여부를 검증한다.
    private void assertChildExists(long childId) {
        if (!timeCapsuleMapper.existsChildById(childId)) {
            throw new BusinessException(ErrorCode.CHILD_NOT_FOUND);
        }
    }

    // [JMG] CAPSULE-1~3 요청 회원이 대상 자녀와 활성 부모 관계인지 검증한다.
    private void assertParentAccess(
            long requesterMemberId,
            Long childId
    ) {
        if (childId == null
                || !timeCapsuleMapper.existsActiveParentRelation(
                requesterMemberId,
                childId
        )) {
            throw new BusinessException(
                    ErrorCode.TIME_CAPSULE_ACCESS_DENIED
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
