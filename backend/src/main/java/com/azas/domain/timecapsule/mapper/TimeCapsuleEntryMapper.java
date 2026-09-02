package com.azas.domain.timecapsule.mapper;

import com.azas.domain.timecapsule.entity.TimeCapsuleEntry;
import com.azas.domain.timecapsule.entity.TimeCapsuleEntryTransaction;
import com.azas.domain.timecapsule.entity.TimeCapsuleEntryMediaMode;
import com.azas.domain.timecapsule.entity.TimeCapsuleMediaType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TimeCapsuleEntryMapper {

    List<TimeCapsuleEntry> findSealedEntriesByTimeCapsuleId(
            @Param("timeCapsuleId") long timeCapsuleId
    );

    TimeCapsuleEntry findAccessibleById(
            @Param("timeCapsuleEntryId") long timeCapsuleEntryId,
            @Param("memberId") long memberId
    );

    TimeCapsuleEntry findOwnedById(
            @Param("timeCapsuleEntryId") long timeCapsuleEntryId,
            @Param("memberId") long memberId
    );

    TimeCapsuleEntry findOwnedByIdForUpdate(
            @Param("timeCapsuleEntryId") long timeCapsuleEntryId,
            @Param("memberId") long memberId
    );

    int markEntryAsDeleted(
            @Param("timeCapsuleEntryId") long timeCapsuleEntryId
    );

    int recalculateTimeCapsuleAggregates(
            @Param("timeCapsuleId") long timeCapsuleId
    );

    List<Long> lockByTimeCapsuleId(
            @Param("timeCapsuleId") long timeCapsuleId
    );

    int deleteByTimeCapsuleId(@Param("timeCapsuleId") long timeCapsuleId);

    int countSealedByTimeCapsuleId(
            @Param("timeCapsuleId") long timeCapsuleId
    );

    int countSealedUpToEntry(
            @Param("timeCapsuleId") long timeCapsuleId,
            @Param("contributedAt") java.time.LocalDateTime contributedAt,
            @Param("timeCapsuleEntryId") long timeCapsuleEntryId
    );

    TimeCapsuleEntry findByTimeCapsuleAndTransactionId(
            @Param("timeCapsuleId") long timeCapsuleId,
            @Param("accountTransactionId") long accountTransactionId
    );

    TimeCapsuleEntryTransaction findTransactionByFinancialAccountId(
            @Param("financialAccountId") long financialAccountId,
            @Param("accountTransactionId") long accountTransactionId
    );

    int insert(TimeCapsuleEntry entry);

    int updateDraftMediaModeIfNone(
            @Param("timeCapsuleEntryId") long timeCapsuleEntryId,
            @Param("mediaMode") TimeCapsuleEntryMediaMode mediaMode
    );

    int sealDraftEntry(
            @Param("timeCapsuleEntryId") long timeCapsuleEntryId
    );

    int countPendingMediaByEntryId(
            @Param("timeCapsuleEntryId") long timeCapsuleEntryId
    );

    int countActiveMediaByEntryIdAndType(
            @Param("timeCapsuleEntryId") long timeCapsuleEntryId,
            @Param("mediaType") TimeCapsuleMediaType mediaType
    );

    int increaseEntryAggregates(TimeCapsuleEntry entry);
}
