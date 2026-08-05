package com.azas.domain.timecapsule.mapper;

import com.azas.domain.timecapsule.entity.TimeCapsuleEntry;
import com.azas.domain.timecapsule.entity.TimeCapsuleEntryTransaction;
import com.azas.domain.timecapsule.entity.TimeCapsuleMediaType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TimeCapsuleEntryMapper {

    // [JMG] CAPSULE-4 삭제되지 않은 타임캡슐 기록과 활성 미디어 개수를 최신순으로 조회한다.
    List<TimeCapsuleEntry> findVisibleEntriesByTimeCapsuleId(
            @Param("timeCapsuleId") long timeCapsuleId
    );

    // [JMG] CAPSULE-14 부모·보호자가 접근 가능한 삭제되지 않은 엔트리 하나를 조회한다.
    TimeCapsuleEntry findAccessibleById(
            @Param("timeCapsuleEntryId") long timeCapsuleEntryId,
            @Param("memberId") long memberId
    );

    // [JMG] CAPSULE-12 작성자 본인에게만 노출되는 엔트리를 잠금 없이 조회한다.
    TimeCapsuleEntry findOwnedById(
            @Param("timeCapsuleEntryId") long timeCapsuleEntryId,
            @Param("memberId") long memberId
    );

    // [JMG] CAPSULE-12 수정·봉인 중 상태 전이를 안전하게 처리하도록 작성자 엔트리 행을 잠근다.
    TimeCapsuleEntry findOwnedByIdForUpdate(
            @Param("timeCapsuleEntryId") long timeCapsuleEntryId,
            @Param("memberId") long memberId
    );

    // [JMG] CAPSULE-13 작성자 초안 엔트리만 삭제 상태로 변경한다.
    int markDraftEntryAsDeleted(
            @Param("timeCapsuleEntryId") long timeCapsuleEntryId
    );

    // [JMG] CAPSULE-5 해당 보관함의 적금 계좌에 실제로 속한 거래만 조회한다.
    TimeCapsuleEntry findByTimeCapsuleAndTransactionId(
            @Param("timeCapsuleId") long timeCapsuleId,
            @Param("accountTransactionId") long accountTransactionId
    );

    // [JMG] CAPSULE-5 대상 적금 계좌의 거래만 조회해 다른 계좌 거래 연결을 차단한다.
    TimeCapsuleEntryTransaction findTransactionByFinancialAccountId(
            @Param("financialAccountId") long financialAccountId,
            @Param("accountTransactionId") long accountTransactionId
    );

    // [JMG] CAPSULE-5 거래 금액과 발생 시각을 스냅샷으로 담은 기록을 저장하고 생성 ID를 채운다.
    int insert(TimeCapsuleEntry entry);

    // [JMG] CAPSULE-12 DRAFT 엔트리의 제목·편지와 수정 횟수를 한 트랜잭션에서 갱신한다.
    int updateDraftContent(
            @Param("timeCapsuleEntryId") long timeCapsuleEntryId,
            @Param("title") String title,
            @Param("message") String message
    );

    // [JMG] CAPSULE-15 DRAFT 엔트리를 봉인 상태로 바꾸고 봉인 시각을 기록한다.
    int sealDraftEntry(
            @Param("timeCapsuleEntryId") long timeCapsuleEntryId
    );

    // [JMG] CAPSULE-15 업로드가 끝나지 않은 미디어가 남아 있는지 확인한다.
    int countPendingMediaByEntryId(
            @Param("timeCapsuleEntryId") long timeCapsuleEntryId
    );

    // [JMG] CAPSULE-15 이미지·영상 엔트리의 활성 미디어 개수를 유형별로 검증한다.
    int countActiveMediaByEntryIdAndType(
            @Param("timeCapsuleEntryId") long timeCapsuleEntryId,
            @Param("mediaType") TimeCapsuleMediaType mediaType
    );

    // [JMG] CAPSULE-5 기록 저장 후 보관함의 기록 수와 가장 최근 기록 시각을 원자적으로 갱신한다.
    int increaseEntryCountAndRefreshLatestEntry(TimeCapsuleEntry entry);
}
