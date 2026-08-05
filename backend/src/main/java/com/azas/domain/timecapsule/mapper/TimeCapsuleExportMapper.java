package com.azas.domain.timecapsule.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TimeCapsuleExportMapper {

    // [JMG] CAPSULE-6 보관함 삭제 시 결과물 S3 객체 키를 잠금과 함께 조회한다.
    List<String> findOutputObjectKeysByTimeCapsuleIdForUpdate(
            @Param("timeCapsuleId") long timeCapsuleId
    );

    // [JMG] CAPSULE-6 보관함 영구 삭제 전 결과물 생성 이력을 삭제한다.
    int deleteByTimeCapsuleId(@Param("timeCapsuleId") long timeCapsuleId);
}
