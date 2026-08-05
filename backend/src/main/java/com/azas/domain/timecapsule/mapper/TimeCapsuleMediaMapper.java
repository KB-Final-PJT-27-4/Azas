package com.azas.domain.timecapsule.mapper;

import com.azas.domain.timecapsule.entity.TimeCapsuleMedia;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TimeCapsuleMediaMapper {

    // [JMG] CAPSULE-7 업로드 대기 미디어를 저장하고 생성된 미디어 ID를 채운다.
    int insert(TimeCapsuleMedia media);

    // [JMG] CAPSULE-7 엔트리에 이미 사용 중인 슬롯이 있는지 확인한다.
    int countByEntryIdAndSlotNo(
            @Param("timeCapsuleEntryId") long timeCapsuleEntryId,
            @Param("slotNo") int slotNo
    );

    // [JMG] CAPSULE-8 지정된 미디어 ID가 해당 엔트리에 속하는지 잠금과 함께 확인한다.
    List<TimeCapsuleMedia> findByEntryIdAndIdsForUpdate(
            @Param("timeCapsuleEntryId") long timeCapsuleEntryId,
            @Param("mediaIds") List<Long> mediaIds
    );

    // [JMG] CAPSULE-8 업로드 검증을 통과한 대기 미디어를 활성 상태로 전환한다.
    int activatePendingMedia(@Param("mediaIds") List<Long> mediaIds);

    // [JMG] CAPSULE-8 엔트리의 첫 이미지 객체 키를 목록용 썸네일로 한 번만 저장한다.
    int setThumbnailIfAbsent(
            @Param("timeCapsuleEntryId") long timeCapsuleEntryId,
            @Param("thumbnailObjectKey") String thumbnailObjectKey
    );

    // [JMG] CAPSULE-8 엔트리의 현재 활성 미디어 개수를 응답과 봉인 검증에 사용한다.
    int countActiveByEntryId(@Param("timeCapsuleEntryId") long timeCapsuleEntryId);
}
