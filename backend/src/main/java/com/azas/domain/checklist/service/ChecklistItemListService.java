package com.azas.domain.checklist.service;

import com.azas.domain.checklist.dto.ChecklistChildLifecycleRow;
import com.azas.domain.checklist.dto.ChecklistItemListResult;
import com.azas.domain.checklist.dto.ChecklistItemResult;
import com.azas.domain.checklist.dto.ChecklistItemRow;
import com.azas.domain.checklist.entity.ChecklistItemStatus;
import com.azas.domain.checklist.entity.ChecklistLifecycleStage;
import com.azas.domain.checklist.mapper.ChecklistMapper;
import com.azas.domain.child.entity.BirthStatus;
import com.azas.domain.member.entity.Member;
import com.azas.domain.member.entity.MemberStatus;
import com.azas.domain.member.entity.MemberType;
import com.azas.domain.member.mapper.MemberMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChecklistItemListService {

    private static final ZoneId SERVICE_ZONE =
            ZoneId.of("Asia/Seoul");

    private final MemberMapper memberMapper;
    private final ChecklistMapper checklistMapper;
    private final Clock clock;

    @Autowired
    public ChecklistItemListService(
            MemberMapper memberMapper,
            ChecklistMapper checklistMapper
    ) {
        this(
                memberMapper,
                checklistMapper,
                Clock.system(SERVICE_ZONE)
        );
    }

    ChecklistItemListService(
            MemberMapper memberMapper,
            ChecklistMapper checklistMapper,
            Clock clock
    ) {
        this.memberMapper = memberMapper;
        this.checklistMapper = checklistMapper;
        this.clock = clock;
    }

    @Transactional
    public ChecklistItemListResult getChecklistItems(
            Long memberId,
            Long childId,
            String requestedStage
    ) {
        validateParent(memberId);
        validateChildId(childId);

        ChecklistChildLifecycleRow child =
                checklistMapper.findActiveChildLifecycle(childId);

        if (child == null) {
            throw new BusinessException(ErrorCode.CHILD_NOT_FOUND);
        }

        boolean accessible =
                checklistMapper.countActiveParentAccess(
                        memberId,
                        childId
                ) > 0;

        if (!accessible) {
            throw new BusinessException(
                    ErrorCode.CHILD_ACCESS_DENIED
            );
        }

        ChecklistLifecycleStage lifecycleStage =
                resolveRequestedStage(requestedStage, child);

        checklistMapper.insertMissingItems(
                childId,
                lifecycleStage.name()
        );

        List<ChecklistItemRow> rows =
                checklistMapper.findItems(
                        childId,
                        lifecycleStage.name()
                );

        if (rows == null) {
            rows = Collections.emptyList();
        }

        List<ChecklistItemResult> items =
                rows.stream()
                        .map(ChecklistItemResult::from)
                        .collect(Collectors.toList());

        int totalCount = items.size();

        int completedCount = (int) items.stream()
                .filter(ChecklistItemResult::isCompleted)
                .count();

        int progressPercent = totalCount == 0
                ? 0
                : (int) Math.round(
                completedCount * 100.0 / totalCount
        );

        boolean stageCompleted =
                totalCount > 0
                        && completedCount == totalCount;

        return new ChecklistItemListResult(
                childId,
                lifecycleStage,
                lifecycleStage.getTitle(),
                lifecycleStage.getDescription(),
                totalCount,
                completedCount,
                progressPercent,
                stageCompleted,
                items
        );
    }

    private void validateParent(Long memberId) {
        Member member = memberMapper.findById(memberId);

        if (member == null
                || member.getStatus() != MemberStatus.ACTIVE) {
            throw new BusinessException(
                    ErrorCode.INVALID_ACCESS_TOKEN
            );
        }

        if (member.getMemberType() != MemberType.PARENT) {
            throw new BusinessException(
                    ErrorCode.PARENT_ACCESS_REQUIRED
            );
        }
    }

    private void validateChildId(Long childId) {
        if (childId == null || childId < 1L) {
            throw new BusinessException(ErrorCode.BADREQUEST);
        }
    }

    private ChecklistLifecycleStage resolveRequestedStage(
            String requestedStage,
            ChecklistChildLifecycleRow child
    ) {
        if (requestedStage != null
                && !requestedStage.isBlank()) {
            return ChecklistLifecycleStage.from(
                    requestedStage
            );
        }

        return resolveCurrentStage(child);
    }

    private ChecklistLifecycleStage resolveCurrentStage(
            ChecklistChildLifecycleRow child
    ) {
        if (child.getBirthStatus() == BirthStatus.EXPECTED) {
            if (child.getExpectedBirthDate() == null) {
                throw new BusinessException(
                        ErrorCode.CHECKLIST_STAGE_NOT_SUPPORTED
                );
            }

            return ChecklistLifecycleStage.PREGNANCY;
        }

        if (child.getBirthStatus() != BirthStatus.BORN
                || child.getBirthDate() == null) {
            throw new BusinessException(
                    ErrorCode.CHECKLIST_STAGE_NOT_SUPPORTED
            );
        }

        LocalDate today = LocalDate.now(clock);

        if (child.getBirthDate().isAfter(today)) {
            throw new BusinessException(
                    ErrorCode.CHECKLIST_STAGE_NOT_SUPPORTED
            );
        }

        int age = Period.between(
                child.getBirthDate(),
                today
        ).getYears();

        if (age <= 1) {
            return ChecklistLifecycleStage.AGE_0_TO_1;
        }

        if (age <= 4) {
            return ChecklistLifecycleStage.AGE_2_TO_4;
        }

        if (age <= 7) {
            return ChecklistLifecycleStage.AGE_5_TO_7;
        }

        if (age <= 10) {
            return ChecklistLifecycleStage.AGE_8_TO_10;
        }

        if (age <= 13) {
            return ChecklistLifecycleStage.AGE_11_TO_13;
        }

        if (age <= 16) {
            return ChecklistLifecycleStage.AGE_14_TO_16;
        }

        if (age <= 19) {
            return ChecklistLifecycleStage.AGE_17_TO_19;
        }

        throw new BusinessException(
                ErrorCode.CHECKLIST_STAGE_NOT_SUPPORTED
        );
    }
}