package com.azas.domain.checklist.mapper;

import com.azas.domain.checklist.dto.ChecklistChildLifecycleRow;
import com.azas.domain.checklist.dto.ChecklistItemRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChecklistMapper {

    ChecklistChildLifecycleRow findActiveChildLifecycle(
            @Param("childId") Long childId
    );

    int countActiveParentAccess(
            @Param("memberId") Long memberId,
            @Param("childId") Long childId
    );

    int insertMissingItems(
            @Param("childId") Long childId,
            @Param("lifecycleStage") String lifecycleStage
    );

    List<ChecklistItemRow> findItems(
            @Param("childId") Long childId,
            @Param("lifecycleStage") String lifecycleStage
    );
}