package com.azas.domain.timecapsule.mapper;

import com.azas.domain.timecapsule.entity.TimeCapsuleEntry;
import com.azas.domain.timecapsule.entity.TimeCapsuleEntryTransaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TimeCapsuleEntryMapper {

    // [JMG] CAPSULE-4 삭제되지 않은 타임캡슐 기록과 활성 미디어 개수를 최신순으로 조회한다.
    List<TimeCapsuleEntry> findVisibleEntriesByTimeCapsuleId(
            @Param("timeCapsuleId") long timeCapsuleId
    );

    // [JMG] CAPSULE-5 해당 보관함의 적금 계좌에 실제로 속한 거래만 조회한다.
    TimeCapsuleEntryTransaction findTransactionByTimeCapsuleAndId(
            @Param("timeCapsuleId") long timeCapsuleId,
            @Param("accountTransactionId") long accountTransactionId
    );

    // [JMG] CAPSULE-5 거래 금액과 발생 시각을 스냅샷으로 담은 기록을 저장하고 생성 ID를 채운다.
    int insert(TimeCapsuleEntry entry);

    // [JMG] CAPSULE-5 기록 저장 후 보관함의 기록 수와 가장 최근 기록 시각을 원자적으로 갱신한다.
    int increaseEntryCountAndRefreshLatestEntry(TimeCapsuleEntry entry);
}
