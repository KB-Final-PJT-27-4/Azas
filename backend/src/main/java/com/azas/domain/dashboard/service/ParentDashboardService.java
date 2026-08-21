package com.azas.domain.dashboard.service;

import com.azas.domain.dashboard.dto.ParentDashboardChecklistRow;
import com.azas.domain.dashboard.dto.ParentDashboardFlowRow;
import com.azas.domain.dashboard.dto.ParentDashboardResponse;
import com.azas.domain.dashboard.dto.ParentDashboardSummaryRow;
import com.azas.domain.dashboard.mapper.ParentDashboardMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ParentDashboardService {

    private static final int CHECKLIST_PREVIEW_SIZE = 3;

    private final ParentDashboardMapper parentDashboardMapper;

    @Transactional(readOnly = true)
    public ParentDashboardResponse getDashboard(
            Long memberId,
            Long childId
    ) {
        validateChildId(childId);

        if (parentDashboardMapper.countActiveChild(childId) == 0) {
            throw new BusinessException(ErrorCode.CHILD_NOT_FOUND);
        }

        if (parentDashboardMapper.countActiveParentAccess(
                memberId,
                childId
        ) == 0) {
            throw new BusinessException(ErrorCode.CHILD_ACCESS_DENIED);
        }

        ParentDashboardSummaryRow summary =
                parentDashboardMapper.findDashboardSummary(
                        memberId,
                        childId
                );

        if (summary == null) {
            throw new BusinessException(ErrorCode.CHILD_NOT_FOUND);
        }

        List<ParentDashboardFlowRow> flowRows = new ArrayList<>(
                parentDashboardMapper.findSixMonthFlow(childId)
        );

        // DB에서는 최신순으로 6개를 가져오고,
        // 차트 표시용 응답은 과거 → 현재 순서로 변경합니다.
        Collections.reverse(flowRows);

        List<ParentDashboardChecklistRow> checklistRows =
                parentDashboardMapper.findChecklistPreview(
                        childId,
                        CHECKLIST_PREVIEW_SIZE
                );

        return ParentDashboardResponse.from(
                summary,
                flowRows,
                checklistRows
        );
    }

    private void validateChildId(Long childId) {
        if (childId == null || childId <= 0) {
            throw new BusinessException(ErrorCode.BADREQUEST);
        }
    }
}
