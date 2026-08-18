package com.azas.domain.timecapsule.mapper;

import com.azas.domain.timecapsule.entity.TimeCapsuleMedia;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TimeCapsuleMediaMapper {

    int insert(TimeCapsuleMedia media);

    int countByEntryIdAndSlotNo(
            @Param("timeCapsuleEntryId") long timeCapsuleEntryId,
            @Param("slotNo") int slotNo
    );

    TimeCapsuleMedia findByEntryIdAndIdForUpdate(
            @Param("timeCapsuleEntryId") long timeCapsuleEntryId,
            @Param("timeCapsuleMediaId") long timeCapsuleMediaId
    );

    List<TimeCapsuleMedia> findActiveByEntryId(
            @Param("timeCapsuleEntryId") long timeCapsuleEntryId
    );

    // [JMG] CAPSULE-13 S3 삭제 대상인 활성·대기 미디어를 행 잠금과 함께 조회한다.
    List<TimeCapsuleMedia> findNotDeletedByEntryIdForUpdate(
            @Param("timeCapsuleEntryId") long timeCapsuleEntryId
    );

    int activatePendingMedia(
            @Param("timeCapsuleMediaId") long timeCapsuleMediaId
    );

    int setThumbnailIfAbsent(
            @Param("timeCapsuleEntryId") long timeCapsuleEntryId,
            @Param("thumbnailObjectKey") String thumbnailObjectKey
    );

    int markNotDeletedMediaAsDeleted(
            @Param("timeCapsuleEntryId") long timeCapsuleEntryId
    );

    List<String> findObjectKeysByTimeCapsuleIdForUpdate(
            @Param("timeCapsuleId") long timeCapsuleId
    );

    int deleteByTimeCapsuleId(@Param("timeCapsuleId") long timeCapsuleId);

    int countActiveByEntryId(@Param("timeCapsuleEntryId") long timeCapsuleEntryId);
}
